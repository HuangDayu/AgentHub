package com.agenthub.application.usecase;

import com.agenthub.application.command.SubAgentChatCommand;
import com.agenthub.application.command.SubsessionCommand;
import com.agenthub.application.dto.SubsessionOutput;
import com.agenthub.application.port.out.agent.SubagentExecutionPort;
import com.agenthub.application.port.out.repositories.SubsessionRepository;
import com.agenthub.domain.exception.NotFoundException;
import com.agenthub.domain.model.agent.AgentMessage;
import com.agenthub.domain.model.agent.ChatMessage;
import com.agenthub.domain.model.agent.Subsession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 子会话用例，处理子会话的创建、查询和关闭。
 */
@Component
@RequiredArgsConstructor
public class SubsessionUseCase {

    private final SubsessionRepository subsessionRepository;
    private final SubagentExecutionPort subagentExecutionPort;

    /**
     * 创建子会话。Subagent 由运行时引擎创建，此处不校验 Subagent 存在性。
     *
     * @param command 创建命令
     * @return 子会话输出DTO
     */
    public SubsessionOutput create(SubsessionCommand command) {
        Subsession subsession = Subsession.create(
                command.getParentSessionId(), command.getSubagentId(), command.getName());
        Subsession saved = subsessionRepository.save(subsession);
        return toOutput(saved);
    }

    /**
     * 根据ID获取子会话。
     *
     * @param id 子会话ID
     * @return 子会话输出DTO
     */
    public SubsessionOutput get(String id) {
        Subsession subsession = subsessionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Subsession not found: " + id));
        return toOutput(subsession);
    }

    /**
     * 根据父会话ID列出所有子会话。
     *
     * @param parentSessionId 父会话ID
     * @return 子会话输出DTO列表
     */
    public List<SubsessionOutput> listByParentSession(String parentSessionId) {
        return subsessionRepository.findByParentSessionId(parentSessionId).stream()
                .map(this::toOutput)
                .toList();
    }

    /**
     * 关闭子会话。
     *
     * @param id 子会话ID
     */
    public void close(String id) {
        Subsession subsession = subsessionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Subsession not found: " + id));
        subsession.close();
        subsessionRepository.save(subsession);
    }

    /**
     * 获取子会话的消息列表。
     *
     * @param subsessionId 子会话ID
     * @return 消息列表
     */
    public List<ChatMessage> getMessages(String subsessionId) {
        return subsessionRepository.findByIdWithMessages(subsessionId)
                .map(Subsession::getMessages)
                .orElse(List.of());
    }

    /**
     * 流式发送消息到Subsession。
     *
     * @return 消息流
     */
    public Flux<AgentMessage> streamMessage(SubAgentChatCommand subAgentChatCommand) {
        Subsession subsession = subsessionRepository.findById(subAgentChatCommand.getSubSessionId())
                .orElseThrow(() -> new NotFoundException("Subsession not found: " + subAgentChatCommand.getSubSessionId()));
        subAgentChatCommand.setSubAgentId(subsession.getSubagentId());
        return subagentExecutionPort.stream(subAgentChatCommand);
    }

    /**
     * 将领域模型转换为输出DTO。
     */
    private SubsessionOutput toOutput(Subsession subsession) {
        return new SubsessionOutput(subsession.getId(), subsession.getParentSessionId(),
                subsession.getSubagentId(), subsession.getName(),
                subsession.getStatus(), subsession.getCreatedAt(),
                subsession.getUpdatedAt());
    }
}
