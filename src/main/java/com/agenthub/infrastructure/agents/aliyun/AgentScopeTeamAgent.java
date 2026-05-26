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
        return Flux.empty();
    }

    @Override
    public List<AbstractReActAgent> followers() {
        return followers;
    }


}
