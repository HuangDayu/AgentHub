package com.agenthub.infrastructure.agents.aliyun;

import com.agenthub.application.factory.TeamAgentFactory;
import com.agenthub.domain.enums.AgentTeamType;
import com.agenthub.domain.model.agent.AbstractReActAgent;
import com.agenthub.domain.model.agent.AbstractTeamAgent;
import com.agenthub.domain.model.agent.ReActAgentContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * AgentScope Harness 的多 Agent 团队工厂。
 * <p>
 * 根据 {@link AgentTeamType} 创建对应的 {@link AgentScopeTeamAgent} 实例，
 * 使用 {@link AgentScopeReActAgentFactory} 创建团队中的 follower agent。
 */
@Primary
@RequiredArgsConstructor
@Component
public class AgentScopeTeamAgentFactory implements TeamAgentFactory {

    private final AgentScopeReActAgentFactory agentFactory;

    @Override
    public AbstractTeamAgent create(AgentTeamType agentTeamType, ReActAgentContext leader,
                                    ReActAgentContext... followers) {
        List<AbstractReActAgent> followersAgents = new LinkedList<>();
        for (var ctx : followers) {
            followersAgents.add(agentFactory.create(ctx));
        }
        return new AgentScopeTeamAgent(
                agentTeamType, leader, leader.getAgent().getName(), followersAgents);
    }

}
