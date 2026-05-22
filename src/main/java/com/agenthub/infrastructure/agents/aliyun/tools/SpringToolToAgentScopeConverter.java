package com.agenthub.infrastructure.agents.aliyun.tools;

import io.agentscope.core.tool.Toolkit;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Spring AI 工具到 AgentScope 工具的转换器。
 */
@Component
public class SpringToolToAgentScopeConverter {

    public Toolkit convertToToolkit(Set<ToolCallback> springTools) {
        Toolkit toolkit = new Toolkit();
        for (ToolCallback springTool : springTools) {
            registerToolToToolkit(toolkit, springTool);
        }
        return toolkit;
    }

    private void registerToolToToolkit(Toolkit toolkit, ToolCallback springTool) {
        Object agentScopeTool = createAgentScopeTool(springTool);
        toolkit.registerTool(agentScopeTool);
    }

    private Object createAgentScopeTool(ToolCallback springTool) {
        return new SpringToolAdapter(springTool);
    }
}
