package com.agenthub.infrastructure.tool;

import com.agenthub.application.command.InvokeToolCommand;
import com.agenthub.application.port.out.rag.ToolInvocationPort;
import com.agenthub.domain.model.ToolInvokeView;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 测试用的工具调用端口Mock.
 */
@Component
public class McpToolInvocationPort implements ToolInvocationPort {
    
    @Override
    public ToolInvokeView invokeTool(String toolId, InvokeToolCommand command) {
        return new ToolInvokeView(toolId, "SUCCESS", Map.of("result", "mocked"));
    }
}
