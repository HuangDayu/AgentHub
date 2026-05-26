package com.agenthub.infrastructure.agents.alibaba;

import com.agenthub.domain.enums.AgentTeamType;
import com.agenthub.domain.model.agent.ReActAgentContext;
import com.agenthub.domain.model.agent.AbstractTeamAgent;
import com.agenthub.application.factory.TeamAgentFactory;
import com.agenthub.infrastructure.factory.SpringShareObjectFactory;
import com.alibaba.cloud.ai.graph.agent.Agent;
import com.alibaba.cloud.ai.graph.agent.AgentTool;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.LlmRoutingAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.ParallelAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.SequentialAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.SupervisorAgent;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

import static com.agenthub.domain.enums.AgentTeamType.*;

/**
 * @author huangdayu
 */
@Component
@RequiredArgsConstructor
public class AlibabaTeamAgentFactory implements TeamAgentFactory {

    private final AlibabaReActAgentFactory alibabaReActAgentFactory;
    private final SpringShareObjectFactory springShareObjectFactory;

    @Override
    public AbstractTeamAgent create(AgentTeamType agentTeamType, ReActAgentContext leader, ReActAgentContext... followers) {
        List<AlibabaReActAgent> followersAgents = new LinkedList<>();
        for (ReActAgentContext follower : followers) {
            followersAgents.add((AlibabaReActAgent) alibabaReActAgentFactory.create(follower));
        }
        return switch (agentTeamType) {
            case SEQUENTIAL_AGENT_TEAMS -> createAliSequentialAgentTeam(leader, followersAgents);
            case PARALLEL_AGENT_TEAMS -> createAliParallelAgentTeam(leader, followersAgents);
            case ROUTING_AGENT_TEAMS -> createAliRoutingAgentTeam(leader, followersAgents);
            case SUPERVISOR_AGENT_TEAMS -> createAliSupervisorAgentTeam(leader, followersAgents);
            case SUB_AGENT_TEAMS -> createAliSubAgentTeam(leader, followersAgents);
        };
    }

    private AbstractTeamAgent createAliSubAgentTeam(ReActAgentContext leader, List<AlibabaReActAgent> followersAgents) {
        List<ToolCallback> subAgents = followersAgents.stream().map(v -> AgentTool.getFunctionToolCallback(v.getAgent())).toList();
        ChatModel chatModel = getChatModel(leader);
        ReactAgent reactAgent = ReactAgent.builder().name(leader.getAgent().getName())
                .description(leader.getAgent().getDescription())
                .model(chatModel).tools(subAgents).build();
        return new AlibabaTeamAgent(SUB_AGENT_TEAMS, leader, reactAgent, followersAgents);
    }

    private AlibabaTeamAgent createAliSupervisorAgentTeam(ReActAgentContext leader, List<AlibabaReActAgent> followersAgents) {
        List<Agent> agents = followersAgents.stream().map(v -> (Agent) v.getAgent()).toList();
        ChatModel chatModel = getChatModel(leader);
        SupervisorAgent supervisorAgent = SupervisorAgent.builder().name(leader.getAgent().getName())
                .description(leader.getAgent().getDescription())
                .model(chatModel).subAgents(agents).build();
        return new AlibabaTeamAgent(SUPERVISOR_AGENT_TEAMS, leader, supervisorAgent, followersAgents);
    }

    private AlibabaTeamAgent createAliRoutingAgentTeam(ReActAgentContext leader, List<AlibabaReActAgent> followersAgents) {
        List<Agent> agents = followersAgents.stream().map(v -> (Agent) v.getAgent()).toList();
        ChatModel chatModel = getChatModel(leader);
        LlmRoutingAgent routingAgent = LlmRoutingAgent.builder().name(leader.getAgent().getName())
                .description(leader.getAgent().getDescription())
                .model(chatModel).subAgents(agents).build();
        return new AlibabaTeamAgent(ROUTING_AGENT_TEAMS, leader, routingAgent, followersAgents);
    }

    private AlibabaTeamAgent createAliParallelAgentTeam(ReActAgentContext leader, List<AlibabaReActAgent> followersAgents) {
        List<Agent> agents = followersAgents.stream().map(v -> (Agent) v.getAgent()).toList();
        ParallelAgent parallelAgent = ParallelAgent.builder().name(leader.getAgent().getName())
                .description(leader.getAgent().getDescription())
                .subAgents(agents).build();
        return new AlibabaTeamAgent(PARALLEL_AGENT_TEAMS, leader, parallelAgent, followersAgents);
    }

    private AlibabaTeamAgent createAliSequentialAgentTeam(ReActAgentContext leader, List<AlibabaReActAgent> followersAgents) {
        List<Agent> agents = followersAgents.stream().map(v -> (Agent) v.getAgent()).toList();
        SequentialAgent sequentialAgent = SequentialAgent.builder().name(leader.getAgent().getName())
                .description(leader.getAgent().getDescription())
                .subAgents(agents).build();
        return new AlibabaTeamAgent(SEQUENTIAL_AGENT_TEAMS, leader, sequentialAgent, followersAgents);
    }

    private ChatModel getChatModel(ReActAgentContext context) {
        String chatModelId = context.getChatModelId();
        if (chatModelId == null) return null;
        return springShareObjectFactory.getChatModelByConfigId(chatModelId);
    }
}
