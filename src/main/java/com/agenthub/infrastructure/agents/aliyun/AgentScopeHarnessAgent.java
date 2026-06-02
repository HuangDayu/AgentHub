package com.agenthub.infrastructure.agents.aliyun;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.agenthub.application.factory.TeamAgentFactory;
import com.agenthub.domain.enums.AgentLifecycleState;
import com.agenthub.domain.enums.AgentTeamType;
import com.agenthub.domain.model.agent.AbstractReActAgent;
import com.agenthub.domain.model.agent.AbstractTeamAgent;
import com.agenthub.domain.model.agent.AgentMessage;
import com.agenthub.domain.model.agent.ReActAgentContext;
import io.agentscope.core.agent.Event;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.agent.StreamOptions;
import io.agentscope.core.message.*;
import io.agentscope.harness.agent.HarnessAgent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.ai.util.json.JsonParser.toJson;

/**
 * 基于 AgentScope Harness 框架的 Agent 运行时实现。
 * 封装 {@link HarnessAgent} 的创建与执行，适配项目的 {@link AbstractReActAgent} 接口。
 */
@RequiredArgsConstructor
public class AgentScopeHarnessAgent extends AbstractReActAgent {

    private final ReActAgentContext context;
    private final AgentScopeReActAgentConfig config;
    private final TeamAgentFactory teamAgentFactory;
    @Getter
    private final HarnessAgent agent;
    private final List<AbstractTeamAgent> teams = new LinkedList<>();
    private AgentLifecycleState state = AgentLifecycleState.CREATED;

    @Override
    @SneakyThrows
    public void init() {
        state = AgentLifecycleState.STARTING;
        state = AgentLifecycleState.RUNNING;
    }

    @Override
    public String getName() {
        return context.getAgent().getName();
    }

    @Override
    public Object getNativeAgent() {
        return agent;
    }

    @Override
    public AgentLifecycleState getState() {
        return state;
    }

    @Override
    public ReActAgentContext getContext() {
        return context;
    }

    @Override
    public Flux<AgentMessage> streamMessages(List<AgentMessage> messages) {
        beforeInference(messages);
        RuntimeContext ctx = buildRuntimeContext();
        StreamOptions streamOptions = StreamOptions.defaults();
        var events = agent.stream(toMsgs(messages), streamOptions, ctx);
        return events.map(event -> toAgentMessage(event, messages));
    }

    @Override
    @SneakyThrows
    public AgentMessage call(List<AgentMessage> messages) {
        beforeInference(messages);
        RuntimeContext ctx = buildRuntimeContext();
        Msg response = agent.call(toMsgs(messages), ctx).block();
        AgentMessage result = toAgentMessage(response);
        return afterInference(messages, result);
    }

    /** 构建运行时上下文。 */
    private RuntimeContext buildRuntimeContext() {
        return RuntimeContext.builder()
                .sessionId(context.getSessionId())
                .build();
    }

    private List<Msg> toMsgs(List<AgentMessage> messages) {
        return messages.stream()
                .map(message -> Msg.builder()
                        .role(MsgRole.valueOf(message.getMessageType().name()))
                        .textContent(message.getText())
                        .build())
                .toList();
    }

    @Override
    public void interrupt() {
        state = AgentLifecycleState.STOPPING;
        agent.interrupt();
        state = AgentLifecycleState.STOPPED;
    }

    @Override
    public List<AbstractTeamAgent> teams() {
        return teams;
    }

    @Override
    public void createTeam(AgentTeamType agentTeamType,
                           ReActAgentContext leader,
                           ReActAgentContext... followers) {
        teams.add(teamAgentFactory.create(agentTeamType, leader, followers));
    }

    /** 将原生 Msg 转换为 AgentMessage。 */
    private AgentMessage toAgentMessage(Msg msg) {
        if (msg == null) {
            return new AgentMessage(AgentMessage.MessageType.SYSTEM,
                    "系统出错，大模型没有回复");
        }
        return new AgentMessage(AgentMessage.MessageType.ASSISTANT,
                msg.getTextContent());
    }

    /** 将 Event 转换为 AgentMessage（含生命周期钩子）。 */
    private AgentMessage toAgentMessage(Event event, List<AgentMessage> messages) {
        Msg msg = event.getMessage();
        AgentMessage.MessageType messageType = AgentMessage.MessageType
                .valueOf(msg.getRole().name());
        if (event.isLast() && msg.getChatUsage() != null) {
            return buildUsageMessage(msg, messageType, event);
        }
        return buildContentMessage(msg, messageType, event);
    }

    /** 构建含 token 使用量的消息。 */
    private AgentMessage buildUsageMessage(Msg msg,
                                           AgentMessage.MessageType messageType,
                                           Event event) {
        AgentMessage.ChatUsage chatUsage = BeanUtil.copyProperties(
                msg.getChatUsage(), AgentMessage.ChatUsage.class);
        chatUsage.setTotalTokens(msg.getChatUsage().getTotalTokens());
        return new AgentMessage(messageType, chatUsage,
                toMetadata(event, "STOP"));
    }

    /** 构建含内容的消息。 */
    private AgentMessage buildContentMessage(Msg msg,
                                             AgentMessage.MessageType messageType,
                                             Event event) {
        List<AgentMessage.ToolCall> toolCalls = toToolCalls(msg);
        String finishReason = !toolCalls.isEmpty()
                ? "TOOL_CALLS" : event.getType().name();
        AgentMessage agentMessage = new AgentMessage(messageType,
                event.isLast() ? "" : msg.getTextContent(),
                toMetadata(event, finishReason));
        agentMessage.setToolCalls(toolCalls);
        agentMessage.setResponses(toToolResults(msg));
        return agentMessage;
    }

    private Map<String, Object> toMetadata(Event event, String finishReason) {
        AgentMessage.MessageType messageType = AgentMessage.MessageType
                .valueOf(event.getMessage().getRole().name());
        Map<String, Object> metadata = event.getMessage().getMetadata();
        metadata.put("role", messageType);
        metadata.put("finishReason", finishReason);
        metadata.put("messageId", event.getMessageId());
        metadata.put("source", event.getSource());
        metadata.put("messageType", messageType);
        return metadata;
    }

    private List<AgentMessage.ToolCall> toToolCalls(Msg msg) {
        if (!msg.hasContentBlocks(ToolUseBlock.class)) {
            return new ArrayList<>();
        }
        return msg.getContentBlocks(ToolUseBlock.class).stream()
                .filter(v -> !isFragmentTool(v.getName()))
                .map(this::toToolCall)
                .toList();
    }

    /** 转换单个工具调用。 */
    private AgentMessage.ToolCall toToolCall(ToolUseBlock block) {
        String args = CollUtil.isNotEmpty(block.getInput())
                ? toJson(block.getInput()) : block.getContent();
        return new AgentMessage.ToolCall(block.getId(),
                "function", block.getName(), args);
    }

    private boolean isFragmentTool(String name) {
        return "fragment".equals(name) || "__fragment__".equals(name);
    }

    private List<AgentMessage.ToolResult> toToolResults(Msg msg) {
        if (!msg.hasContentBlocks(ToolResultBlock.class)) {
            return new ArrayList<>();
        }
        return msg.getContentBlocks(ToolResultBlock.class).stream()
                .map(this::toToolResult)
                .toList();
    }

    /** 转换单个工具结果。 */
    private AgentMessage.ToolResult toToolResult(ToolResultBlock block) {
        String output = block.getOutput().stream()
                .filter(TextBlock.class::isInstance)
                .map(TextBlock.class::cast)
                .map(TextBlock::getText)
                .collect(Collectors.joining("\n"));
        return new AgentMessage.ToolResult(block.getId(),
                block.getName(), output);
    }
}
