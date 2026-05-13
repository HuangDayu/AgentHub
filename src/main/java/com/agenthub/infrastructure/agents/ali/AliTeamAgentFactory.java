package com.agenthub.infrastructure.agents.ali;

import com.agenthub.domain.model.MultiAgentTeamType;
import com.agenthub.domain.model.ReActAgentContext;
import com.agenthub.infrastructure.agents.AbstractTeamAgent;
import com.agenthub.infrastructure.agents.TeamAgentFactory;
import com.agenthub.infrastructure.factory.SpringShareObjectFactory;
import com.alibaba.cloud.ai.graph.agent.Agent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.LlmRoutingAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.ParallelAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.SequentialAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.SupervisorAgent;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

/**
 * @author huangdayu
 */
@Component
@RequiredArgsConstructor
public class AliTeamAgentFactory implements TeamAgentFactory {

    private final AliReActAgentFactory aliReActAgentFactory;
    private final SpringShareObjectFactory springShareObjectFactory;

    @Override
    public AbstractTeamAgent create(MultiAgentTeamType agentTeamType, ReActAgentContext leader, ReActAgentContext... followers) {
        List<AliReActAgent> followersAgents = new LinkedList<>();
        for (ReActAgentContext follower : followers) {
            followersAgents.add((AliReActAgent) aliReActAgentFactory.create(follower));
        }
        return switch (agentTeamType) {
            case SEQUENTIAL_AGENT_TEAMS -> createAliSequentialAgentTeam(leader, followersAgents);
            case PARALLEL_AGENT_TEAMS -> createAliParallelAgentTeam(leader, followersAgents);
            case ROUTING_AGENT_TEAMS -> createAliRoutingAgentTeam(leader, followersAgents);
            case SUPERVISOR_AGENT_TEAMS -> createAliSupervisorAgentTeam(leader, followersAgents);
        };
    }

    private AliTeamAgent createAliSupervisorAgentTeam(ReActAgentContext leader, List<AliReActAgent> followersAgents) {
        List<Agent> agents = followersAgents.stream().map(v -> (Agent) v.getAgent()).toList();
        ChatModel chatModel = getChatModel(leader);
        SupervisorAgent supervisorAgent = SupervisorAgent.builder().name(leader.getAgentName()).model(chatModel).subAgents(agents).build();
        return new AliTeamAgent(leader, supervisorAgent, followersAgents);
    }

    private AliTeamAgent createAliRoutingAgentTeam(ReActAgentContext leader, List<AliReActAgent> followersAgents) {
        List<Agent> agents = followersAgents.stream().map(v -> (Agent) v.getAgent()).toList();
        ChatModel chatModel = getChatModel(leader);
        LlmRoutingAgent routingAgent = LlmRoutingAgent.builder().name(leader.getAgentName()).model(chatModel).subAgents(agents).build();
        return new AliTeamAgent(leader, routingAgent, followersAgents);
    }

    private AliTeamAgent createAliParallelAgentTeam(ReActAgentContext leader, List<AliReActAgent> followersAgents) {
        List<Agent> agents = followersAgents.stream().map(v -> (Agent) v.getAgent()).toList();
        ParallelAgent parallelAgent = ParallelAgent.builder().name(leader.getAgentName()).subAgents(agents).build();
        return new AliTeamAgent(leader, parallelAgent, followersAgents);
    }

    private AliTeamAgent createAliSequentialAgentTeam(ReActAgentContext leader, List<AliReActAgent> followersAgents) {
        List<Agent> agents = followersAgents.stream().map(v -> (Agent) v.getAgent()).toList();
        SequentialAgent sequentialAgent = SequentialAgent.builder().name(leader.getAgentName()).subAgents(agents).build();
        return new AliTeamAgent(leader, sequentialAgent, followersAgents);
    }

    private ChatModel getChatModel(ReActAgentContext context) {
        String chatModelId = context.getChatModelId();
        if (chatModelId == null) return null;
        return springShareObjectFactory.getChatModelByConfigId(chatModelId);
    }
}
