package com.agenthub.infrastructure.agents.aliyun;

import com.agenthub.domain.enums.AgentTeamType;
import com.agenthub.domain.model.agent.AbstractReActAgent;
import com.agenthub.domain.model.agent.AbstractTeamAgent;
import com.agenthub.domain.model.agent.AgentMessage;
import com.agenthub.domain.model.agent.ReActAgentContext;
import io.agentscope.harness.agent.HarnessAgent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 基于 AgentScope Harness 的多 Agent 团队运行时。
 * <p>
 * 封装 {@link HarnessAgent} 的团队协作，适配项目的 {@link AbstractTeamAgent} 接口。
 */
@RequiredArgsConstructor
public class AgentScopeTeamAgent extends AbstractTeamAgent {

    @Getter
    private final AgentTeamType agentTeamType;
    private final ReActAgentContext context;
    private final String name;
    private final List<AbstractReActAgent> followers;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ReActAgentContext getContext() {
        return context;
    }

    @Override
    public Flux<AgentMessage> streamMessages(String userMessage) {
        return switch (agentTeamType) {
            case SEQUENTIAL_AGENT_TEAMS -> streamSequential(userMessage);
            case PARALLEL_AGENT_TEAMS -> streamParallel(userMessage);
            case SUB_AGENT_TEAMS, ROUTING_AGENT_TEAMS, SUPERVISOR_AGENT_TEAMS ->
                    streamWithLeader(userMessage);
        };
    }

    @Override
    public List<AbstractReActAgent> followers() {
        return followers;
    }

    private Flux<AgentMessage> streamSequential(String userMessage) {
        if (followers.isEmpty()) return Flux.empty();
        Flux<AgentMessage> chain = Flux.empty();
        for (AbstractReActAgent follower : followers) {
            chain = chain.switchIfEmpty(
                    follower.streamMessages("team-session", userMessage));
        }
        return chain;
    }

    private Flux<AgentMessage> streamParallel(String userMessage) {
        if (followers.isEmpty()) return Flux.empty();
        return Flux.merge(followers.stream()
                .map(f -> f.streamMessages("team-session", userMessage))
                .toList());
    }

    private Flux<AgentMessage> streamWithLeader(String userMessage) {
        if (followers.isEmpty()) return Flux.empty();
        return followers.get(0).streamMessages("team-session", userMessage);
    }

}
