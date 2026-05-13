package com.agenthub.infrastructure.agents.ali;

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
    @Getter
    private final ReactAgent agent;
    private final List<AbstractTeamAgent> teams = new LinkedList<>();

    public AliReActAgent(ReActAgentContext context, ReactAgent agent) {
        this.context = context;
        this.agent = agent;
    }

    public String getName() {
        return context.getAgentName();
    }

    @Override
    public ReActAgentContext getContext() {
        return context;
    }

    @SneakyThrows
    @Override
    public Flux<Message> streamMessages(String userMessage) {
        return agent.streamMessages(userMessage);
    }

    @SneakyThrows
    @Override
    public AssistantMessage call(String userMessage) {
        return agent.call(userMessage);
    }

    @Override
    public void interrupt() {
        agent.interrupt(RunnableConfig.builder().build());
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
