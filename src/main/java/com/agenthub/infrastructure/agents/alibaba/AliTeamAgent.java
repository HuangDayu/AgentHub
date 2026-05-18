package com.agenthub.infrastructure.agents.alibaba;

import com.agenthub.domain.enums.AgentTeamType;
import com.agenthub.domain.model.ReActAgentContext;
import com.agenthub.infrastructure.agents.AbstractReActAgent;
import com.agenthub.infrastructure.agents.AbstractTeamAgent;
import com.alibaba.cloud.ai.graph.agent.Agent;
import lombok.SneakyThrows;
import org.springframework.ai.chat.messages.Message;
import reactor.core.publisher.Flux;

import java.util.LinkedList;
import java.util.List;

/**
 * 多Agent运行时，封装FlowAgent的创建与执行。
 */
public class AliTeamAgent extends AbstractTeamAgent {

    private final ReActAgentContext context;
    private final Agent leader;
    private final AgentTeamType agentTeamType;
    private final List<AbstractReActAgent> followers = new LinkedList<>();

    public AliTeamAgent(AgentTeamType agentTeamType, ReActAgentContext context, Agent leader, List<AliReActAgent> followers) {
        this.context = context;
        this.leader = leader;
        this.followers.addAll(followers);
        this.agentTeamType = agentTeamType;
    }

    public String getName() {
        return context.getAgent().getName();
    }

    @Override
    public AgentTeamType getAgentTeamType() {
        return agentTeamType;
    }

    @Override
    public ReActAgentContext getContext() {
        return context;
    }

    @SneakyThrows
    @Override
    public Flux<Message> streamMessages(String userMessage) {
        return leader.streamMessages(userMessage);
    }

    @Override
    public List<AbstractReActAgent> followers() {
        return followers;
    }


}
