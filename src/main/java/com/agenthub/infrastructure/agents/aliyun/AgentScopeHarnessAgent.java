package com.agenthub.infrastructure.agents.aliyun;

import cn.hutool.core.bean.BeanUtil;
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
    public Flux<AgentMessage> streamMessages(String sessionId, String userMessage) {
        Msg msg = Msg.builder().role(MsgRole.USER).textContent(userMessage).build();
        RuntimeContext ctx = RuntimeContext.builder().sessionId(sessionId).build();
        StreamOptions streamOptions = StreamOptions.defaults();
        var events = agent.stream(List.of(msg), streamOptions, ctx);
        return events.map(this::toAgentMessage);
    }

    @Override
    @SneakyThrows
    public AgentMessage call(String sessionId, String userMessage) {
        Msg msg = Msg.builder().role(MsgRole.USER).textContent(userMessage).build();
        RuntimeContext ctx = RuntimeContext.builder().sessionId(sessionId).build();
        Msg response = agent.call(msg, ctx).block();
        return toAgentMessage(response);
    }

    @Override
    public void interrupt() {
        state = AgentLifecycleState.STOPPING;
        state = AgentLifecycleState.STOPPED;
    }

    @Override
    public List<AbstractTeamAgent> teams() {
        return teams;
    }

    @Override
    public void createTeam(AgentTeamType agentTeamType, ReActAgentContext leader,
                           ReActAgentContext... followers) {
        teams.add(teamAgentFactory.create(agentTeamType, leader, followers));
    }

    private AgentMessage toAgentMessage(Msg msg) {
        return new AgentMessage(AgentMessage.MessageType.ASSISTANT, msg.getTextContent());
    }


    private AgentMessage toAgentMessage(Event event) {
        Msg msg = event.getMessage();
        AgentMessage.MessageType messageType = AgentMessage.MessageType.valueOf(msg.getRole().name());
        if (event.isLast() && msg.getChatUsage() != null) {
            AgentMessage.ChatUsage chatUsage = BeanUtil.copyProperties(msg.getChatUsage(), AgentMessage.ChatUsage.class);
            chatUsage.setTotalTokens(msg.getChatUsage().getTotalTokens());
            return new AgentMessage(messageType, chatUsage, toMetadata(event, "STOP"));
        }
        List<AgentMessage.ToolCall> toolCalls = toToolCalls(msg);
        String finishReason = !toolCalls.isEmpty() ? "TOOL_CALLS" : event.getType().name();
        AgentMessage agentMessage = new AgentMessage(messageType, event.isLast() ? "" : msg.getTextContent(), toMetadata(event, finishReason));
        agentMessage.setToolCalls(toolCalls);
        agentMessage.setResponses(toToolResults(msg));
        return agentMessage;
    }

    private Map<String, Object> toMetadata(Event event, String finishReason) {
        AgentMessage.MessageType messageType = AgentMessage.MessageType.valueOf(event.getMessage().getRole().name());
        Map<String, Object> metadata = event.getMessage().getMetadata();
        metadata.put("role", messageType);
        metadata.put("finishReason", finishReason);
        metadata.put("messageId", event.getMessageId());
        metadata.put("source", event.getSource());
        metadata.put("messageType", messageType);
        return metadata;
    }

    private List<AgentMessage.ToolCall> toToolCalls(Msg msg) {
        return msg.hasContentBlocks(ToolUseBlock.class) ? msg.getContentBlocks(ToolUseBlock.class).stream()
                .map(v -> new AgentMessage.ToolCall(v.getId(), "tool_call", v.getName(), toJson(v.getInput())))
                .toList() : new ArrayList<>();
    }

    private List<AgentMessage.ToolResult> toToolResults(Msg msg) {
        return msg.hasContentBlocks(ToolResultBlock.class) ? msg.getContentBlocks(ToolResultBlock.class).stream()
                .map(v -> new AgentMessage.ToolResult(v.getId(), v.getName(), v.getOutput().stream()
                        .filter(TextBlock.class::isInstance)
                        .map(TextBlock.class::cast)
                        .map(TextBlock::getText)
                        .collect(Collectors.joining("\n"))))
                .toList() : new ArrayList<>();
    }

}
