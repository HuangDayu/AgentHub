package com.agenthub.infrastructure.tools;

import com.agenthub.domain.model.agent.ReActAgentContext;
import com.agenthub.domain.model.agent.ReActAgentWorkspace;
import org.springframework.ai.chat.model.ToolContext;

import static com.agenthub.common.constants.AgentConstants.AGENT_CONTEXT_KEY;

/**
 * @author huangdayu
 */
public class SystemToolsUtils {

    public static ReActAgentWorkspace getWorkspace(ToolContext toolContext) {
        return getAgentContext(toolContext).getWorkspace();
    }

    public static ReActAgentContext getAgentContext(ToolContext toolContext) {
        return (ReActAgentContext) toolContext.getContext().get(AGENT_CONTEXT_KEY);
    }

}
