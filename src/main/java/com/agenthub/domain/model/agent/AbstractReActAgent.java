package com.agenthub.domain.model.agent;

import com.agenthub.domain.enums.AgentLifecycleState;
import com.agenthub.domain.enums.AgentTeamType;
import com.agenthub.domain.exception.ValidationException;
import com.agenthub.domain.model.strategy.GuardrailStrategy;
import com.agenthub.domain.model.strategy.ValidationResult;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * ReAct Agent 抽象基类，定义生命周期与策略钩子。
 *
 * @author huangdayu
 */
public abstract class AbstractReActAgent {

    /** 初始化 Agent。 */
    public abstract void init();

    /** 获取 Agent 名称。 */
    public abstract String getName();

    /** 获取底层原生 Agent 实例。 */
    public abstract Object getNativeAgent();

    /** 获取生命周期状态。 */
    public abstract AgentLifecycleState getState();

    /** 获取 Agent 上下文。 */
    public abstract ReActAgentContext getContext();

    /** 流式发送消息。 */
    public abstract Flux<AgentMessage> streamMessages(List<AgentMessage> messages);

    /** 同步调用。 */
    public abstract AgentMessage call(List<AgentMessage> messages);

    /** 中断执行。 */
    public abstract void interrupt();

    /** 获取团队列表。 */
    public abstract List<AbstractTeamAgent> teams();

    /** 创建团队。 */
    public abstract void createTeam(AgentTeamType agentTeamType,
                                    ReActAgentContext leader,
                                    ReActAgentContext... followers);

    /**
     * 推理前生命周期钩子。
     *
     * @param messages 输入消息列表
     */
    protected void beforeInference(List<AgentMessage> messages) {
        validateInputGuardrail(messages);
        ReActAgentContext ctx = getContext();
        ctx.getModelStrategy().beforeInference(ctx, messages);
    }

    /**
     * 推理后生命周期钩子。
     *
     * @param messages 输入消息列表
     * @param response 模型响应
     * @return 处理后的响应
     */
    protected AgentMessage afterInference(List<AgentMessage> messages,
                                          AgentMessage response) {
        ReActAgentContext ctx = getContext();
        AgentMessage processed = ctx.getModelStrategy().afterInference(ctx, messages, response);
        return validateOutputGuardrail(processed);
    }

    /**
     * 工具调用前生命周期钩子。
     *
     * @param toolName 工具名称
     * @param arguments 调用参数
     */
    protected void beforeToolCall(String toolName, String arguments) {
        ReActAgentContext ctx = getContext();
        ctx.getToolStrategy().beforeToolCall(ctx, toolName, arguments);
    }

    /**
     * 工具调用后生命周期钩子。
     *
     * @param toolName 工具名称
     * @param result 执行结果
     * @return 处理后的结果
     */
    protected String afterToolCall(String toolName, String result) {
        ReActAgentContext ctx = getContext();
        return ctx.getToolStrategy().afterToolCall(ctx, toolName, result);
    }

    /**
     * 检索前生命周期钩子。
     *
     * @param query 原始查询
     * @return 处理后的查询
     */
    protected String beforeRetrieval(String query) {
        ReActAgentContext ctx = getContext();
        return ctx.getRetrievalStrategy().beforeRetrieval(ctx, query);
    }

    /**
     * 检索后生命周期钩子。
     *
     * @param query 查询
     * @param results 检索结果
     * @return 处理后的结果
     */
    protected List<?> afterRetrieval(String query, List<?> results) {
        ReActAgentContext ctx = getContext();
        return ctx.getRetrievalStrategy().afterRetrieval(ctx, query, results);
    }

    /** 验证输入护栏。 */
    private void validateInputGuardrail(List<AgentMessage> messages) {
        GuardrailStrategy strategy = getContext().getGuardrailStrategy();
        if (!isInputValidationEnabled(strategy)) return;
        String text = extractUserText(messages);
        ValidationResult result = strategy.validateInput(text);
        throwIfInvalid(result);
    }

    /** 验证输出护栏。 */
    private AgentMessage validateOutputGuardrail(AgentMessage response) {
        GuardrailStrategy strategy = getContext().getGuardrailStrategy();
        if (!isOutputValidationEnabled(strategy)) return response;
        ValidationResult result = strategy.validateOutput(response.getText());
        throwIfInvalid(result);
        return response;
    }

    /** 判断输入验证是否启用。 */
    private boolean isInputValidationEnabled(GuardrailStrategy strategy) {
        return strategy != null && strategy.isInputValidationEnabled();
    }

    /** 判断输出验证是否启用。 */
    private boolean isOutputValidationEnabled(GuardrailStrategy strategy) {
        return strategy != null && strategy.isOutputValidationEnabled();
    }

    /** 从消息列表中提取用户文本。 */
    private String extractUserText(List<AgentMessage> messages) {
        return messages.stream()
                .filter(m -> m.getMessageType() == AgentMessage.MessageType.USER)
                .map(AgentMessage::getText)
                .reduce((a, b) -> a + "\n" + b)
                .orElse("");
    }

    /** 验证不通过时抛出异常。 */
    private void throwIfInvalid(ValidationResult result) {
        if (!result.isValid()) {
            throw new ValidationException(String.join("; ", result.getViolations()));
        }
    }
}
