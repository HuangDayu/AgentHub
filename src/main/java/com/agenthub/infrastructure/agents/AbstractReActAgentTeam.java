package com.agenthub.infrastructure.agents;

import org.springframework.ai.chat.messages.Message;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Agent团队运行时，管理多Agent协作。
 */
public class AbstractReActAgentTeam {

    private final String teamName;
    private final Map<String, AbstractReActAgent> members;

    public AbstractReActAgentTeam(String teamName) {
        this.teamName = teamName;
        this.members = new ConcurrentHashMap<>();
    }

    public String getTeamName() {
        return teamName;
    }

    public void addMember(AbstractReActAgent agent) {
        members.put(agent.getName(), agent);
    }

    public void removeMember(String agentName) {
        members.remove(agentName);
    }

    public AbstractReActAgent getMember(String agentName) {
        return members.get(agentName);
    }

    public List<String> getMemberNames() {
        return List.copyOf(members.keySet());
    }

    public Flux<Message> delegateTo(String agentName, String userMessage) {
        AbstractReActAgent agent = getMember(agentName);
        validateAgent(agent, agentName);
        return agent.streamMessages(userMessage);
    }

    private void validateAgent(AbstractReActAgent agent, String name) {
        if (agent == null) {
            throw new IllegalArgumentException("Agent not found: " + name);
        }
    }
}
