package com.agenthub.infrastructure.tools.system_tools;

import com.agenthub.domain.model.ReActAgentContext;
import com.agenthub.domain.model.ReActAgentWorkspace;
import org.springframework.ai.chat.model.ToolContext;

import static com.agenthub.common.constants.AgentConstants.AGENT_CONTEXT_KEY;

/**
 * @author huangdayu
 */
public class SystemToolsUtils {

    public static ReActAgentWorkspace getWorkspace(ToolContext toolContext) {
        return ((ReActAgentContext) toolContext.getContext().get(AGENT_CONTEXT_KEY)).getWorkspace();
    }

}
