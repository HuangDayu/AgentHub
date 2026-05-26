package com.agenthub.infrastructure.agents.alibaba;

import com.agenthub.application.factory.TeamAgentFactory;
import com.agenthub.domain.enums.AgentLifecycleState;
import com.agenthub.domain.enums.AgentTeamType;
import com.agenthub.domain.model.agent.AbstractReActAgent;
import com.agenthub.domain.model.agent.AbstractTeamAgent;
import com.agenthub.domain.model.agent.AgentMessage;
import com.agenthub.domain.model.agent.ReActAgentContext;
import com.agenthub.infrastructure.converter.AgentMessageConverter;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.ai.chat.messages.Message;
import reactor.core.publisher.Flux;

import java.util.LinkedList;
import java.util.List;

/**
 * 单个Agent运行时，封装ReactAgent的创建与执行。
 */
@RequiredArgsConstructor
public class AlibabaReActAgent extends AbstractReActAgent {

    private final ReActAgentContext context;
    private final AlibabaReActAgentConfig config;
    private final TeamAgentFactory teamAgentFactory;
    @Getter
    private final ReactAgent agent;
    private final List<AbstractTeamAgent> teams = new LinkedList<>();
    private AgentLifecycleState state = AgentLifecycleState.CREATED;

    @SneakyThrows
    @Override
    public void init() {
        state = AgentLifecycleState.STARTING;
        agent.stream("/init");
        state = AgentLifecycleState.RUNNING;
    }

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

    @SneakyThrows
    @Override
    public Flux<AgentMessage> streamMessages(List<AgentMessage> messages) {
        RunnableConfig runnableConfig = RunnableConfig.builder(config.getRunnableConfig()).threadId(context.getSessionId()).build();
        return agent.streamMessages(toMsgs(messages), runnableConfig)
                .map(AgentMessageConverter::fromMessage);
    }

    @SneakyThrows
    @Override
    public AgentMessage call(List<AgentMessage> messages) {
        RunnableConfig runnableConfig = RunnableConfig.builder(config.getRunnableConfig()).threadId(context.getSessionId()).build();
        return AgentMessageConverter.fromMessage(agent.call(toMsgs(messages), runnableConfig));
    }

    private List<Message> toMsgs(List<AgentMessage> messages) {
        return messages.stream()
                .map(AgentMessageConverter::toMessage)
                .toList();
    }

    @Override
    public void interrupt() {
        state = AgentLifecycleState.STOPPING;
        agent.interrupt(RunnableConfig.builder().build());
        state = AgentLifecycleState.STOPPED;
    }

    @Override
    public List<AbstractTeamAgent> teams() {
        return teams;
    }

    @Override
    public void createTeam(AgentTeamType agentTeamType, ReActAgentContext leader, ReActAgentContext... followers) {
        teams.add(teamAgentFactory.create(agentTeamType, leader, followers));
    }

}
