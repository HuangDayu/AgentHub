package com.agenthub.infrastructure.tools.mcp_tools;

import com.agenthub.infrastructure.tools.AbstractToolsFactory;
import com.agenthub.domain.model.AgentToolInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

import java.util.Set;

import static com.agenthub.domain.model.AgentToolType.MCP_TOOLS;

/**
 * MCP工具工厂，负责提供MCP工具的ToolCallback。
 * 
 * @author huangdayu
 */
@Component
@RequiredArgsConstructor
public class McpToolsFactory implements AbstractToolsFactory {

    private final McpToolCallbackProvider mcpToolCallbackProvider;

    @Override
    public AgentToolInfo getToolInfo() {
        return new AgentToolInfo(MCP_TOOLS);
    }

    @Override
    public Set<ToolCallback> getToolCallbacks() {

        return mcpToolCallbackProvider.getToolCallbacks();
    }
    

}
