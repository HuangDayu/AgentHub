package com.agenthub.application.factory;

import com.agenthub.domain.model.ReActAgentContext;

/**
 * @author huangdayu
 */
public interface AgentContextFactory {
    ReActAgentContext buildContext(String agentId, String sessionId);
}
