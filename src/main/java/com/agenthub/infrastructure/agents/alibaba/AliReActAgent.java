package com.agenthub.infrastructure.agents.alibaba;

import com.agenthub.domain.enums.AgentLifecycleState;
import com.agenthub.domain.model.ReActAgentContext;
import com.agenthub.infrastructure.agents.AbstractReActAgent;
import com.agenthub.infrastructure.agents.AbstractTeamAgent;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import reactor.core.publisher.Flux;

import java.util.LinkedList;
import java.util.List;

/**
 * 单个Agent运行时，封装ReactAgent的创建与执行。
 */
public class AliReActAgent extends AbstractReActAgent {

    private final ReActAgentContext context;
    private final AliReActAgentConfig config;
    @Getter
    private final ReactAgent agent;
    private final List<AbstractTeamAgent> teams = new LinkedList<>();
    private AgentLifecycleState state = AgentLifecycleState.CREATED;

    public AliReActAgent(ReActAgentContext context, AliReActAgentConfig config, ReactAgent agent) {
        this.context = context;
        this.config = config;
        this.agent = agent;
    }

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
    public Flux<Message> streamMessages(String sessionId, String userMessage) {
        RunnableConfig runnableConfig = RunnableConfig.builder(config.getRunnableConfig()).threadId(sessionId).build();
        return agent.streamMessages(userMessage, runnableConfig);
    }

    @SneakyThrows
    @Override
    public AssistantMessage call(String sessionId, String userMessage) {
        RunnableConfig runnableConfig = RunnableConfig.builder(config.getRunnableConfig()).threadId(sessionId).build();
        return agent.call(userMessage, runnableConfig);
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
    public void addTeam(AbstractTeamAgent teamAgent) {
        teams.add(teamAgent);
    }

}
