package com.agenthub.infrastructure.agents.aliyun.tools;

import io.agentscope.core.tool.Toolkit;
import org.springframework.stereotype.Component;

/**
 * AgentScope 工具管理工厂。
 */
@Component
public class ToolkitFactory {

    public Toolkit createEmptyToolkit() {
        return new Toolkit();
    }

    public Toolkit createToolkitWithTools(Object... tools) {
        Toolkit toolkit = new Toolkit();
        for (Object tool : tools) {
            toolkit.registerTool(tool);
        }
        return toolkit;
    }
}
