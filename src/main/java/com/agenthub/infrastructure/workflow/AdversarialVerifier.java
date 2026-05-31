package com.agenthub.infrastructure.workflow;

import com.agenthub.application.command.RunSubagentCommand;
import com.agenthub.application.dto.SubagentRunOutput;
import com.agenthub.application.dto.SubagentRuntimeOutput;
import com.agenthub.application.usecase.SubagentRuntimeUseCase;
import com.agenthub.domain.model.agent.ReActAgentContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 对抗验证器，用独立Agent挑战工作流结果。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdversarialVerifier {

    private final SubagentRuntimeUseCase subagentUseCase;
    private static final int VERIFY_TIMEOUT_SECONDS = 300;

    /**
     * 执行对抗验证。
     *
     * @param originalResult 原始结果
     * @param task           原始任务
     * @param parentContext   父Agent上下文
     * @return 验证意见
     */
    public String verify(String originalResult, String task, ReActAgentContext parentContext) {
        String prompt = buildVerificationPrompt(originalResult, task);
        RunSubagentCommand command = buildVerifyCommand(prompt, parentContext);
        SubagentRunOutput output = subagentUseCase.run(command);
        SubagentRuntimeOutput result = awaitVerification(output);
        return result.getResult();
    }

    private String buildVerificationPrompt(String originalResult, String task) {
        return """
                你是一个严格的审查员。请审查以下结果，找出其中的：
                1. 事实错误
                2. 遗漏的关键信息
                3. 逻辑矛盾
                4. 可能的改进点

                原始任务: %s

                待审查结果:
                %s

                请给出你的审查意见，如果结果质量高则确认，如果发现问题则指出具体问题。
                """.formatted(task, originalResult);
    }

    private RunSubagentCommand buildVerifyCommand(String prompt, ReActAgentContext parentContext) {
        RunSubagentCommand command = new RunSubagentCommand();
        command.setParentContext(parentContext);
        command.setName("adversarial-verifier");
        command.setSystemPrompt("你是一个严谨的审查员，专注于发现问题和错误。");
        command.setTask(prompt);
        return command;
    }

    private SubagentRuntimeOutput awaitVerification(SubagentRunOutput output) {
        long deadline = System.currentTimeMillis() + (VERIFY_TIMEOUT_SECONDS * 1000L);
        while (System.currentTimeMillis() < deadline) {
            SubagentRuntimeOutput status = subagentUseCase.status(
                    output.getSubagentId(), output.getSubsessionId());
            if (isTerminal(status.getStatus())) {
                return subagentUseCase.result(
                        output.getSubagentId(), output.getSubsessionId());
            }
            sleepBriefly();
        }
        return subagentUseCase.result(output.getSubagentId(), output.getSubsessionId());
    }

    private boolean isTerminal(String status) {
        return "COMPLETED".equals(status) || "FAILED".equals(status);
    }

    private void sleepBriefly() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
