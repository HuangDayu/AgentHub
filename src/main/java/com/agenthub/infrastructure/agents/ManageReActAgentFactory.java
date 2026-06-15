package com.agenthub.infrastructure.agents;

import com.agenthub.application.factory.ReActAgentFactory;
import com.agenthub.domain.model.agent.AbstractReActAgent;
import com.agenthub.domain.model.agent.ReActAgentContext;
import com.agenthub.infrastructure.agents.alibaba.AlibabaReActAgentFactory;
import com.agenthub.infrastructure.agents.aliyun.AgentScopeHarnessAgentFactory;
import com.agenthub.infrastructure.agents.spring.SpringReActAgentFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * @author huangdayu
 */
@Primary
@Component
@RequiredArgsConstructor
public class ManageReActAgentFactory implements ReActAgentFactory {

    private final SpringReActAgentFactory springReActAgentFactory;
    private final AgentScopeHarnessAgentFactory agentScopeHarnessAgentFactory;
    private final AlibabaReActAgentFactory alibabaReActAgentFactory;

    @Override
    public AbstractReActAgent create(ReActAgentContext reActAgentContext) {
        if (reActAgentContext.getAgent().getRuntimeCategory() == null) {
            return agentScopeHarnessAgentFactory.create(reActAgentContext);
        }
        return switch (reActAgentContext.getAgent().getRuntimeCategory()) {
            case ALIBABA_AGENT -> alibabaReActAgentFactory.create(reActAgentContext);
            case SPRING_AGENT -> springReActAgentFactory.create(reActAgentContext);
            case AGENT_SCOPE -> agentScopeHarnessAgentFactory.create(reActAgentContext);
        };
    }

}
