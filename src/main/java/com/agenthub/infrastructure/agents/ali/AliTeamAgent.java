package com.agenthub.infrastructure.agents.ali;

import com.agenthub.domain.model.MultiAgentTeamType;
import com.agenthub.domain.model.ReActAgentContext;
import com.agenthub.infrastructure.agents.AbstractReActAgent;
import com.agenthub.infrastructure.agents.AbstractTeamAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.FlowAgent;
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
    private final FlowAgent leader;
    private final List<AbstractReActAgent> followers = new LinkedList<>();

    public AliTeamAgent(ReActAgentContext context, FlowAgent leader, List<AliReActAgent> followers) {
        this.context = context;
        this.leader = leader;
        this.followers.addAll(followers);
    }

    public String getName() {
        return context.getAgentName();
    }

    @Override
    public MultiAgentTeamType getAgentTeamType() {
        return null;
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
