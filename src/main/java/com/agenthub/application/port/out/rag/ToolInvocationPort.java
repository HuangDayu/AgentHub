package com.agenthub.application.port.out.rag;

import com.agenthub.domain.model.ToolInvokeView;
import com.agenthub.application.command.InvokeToolCommand;

/**
 * 工具调用端口 - 执行工具调用
 */
public interface ToolInvocationPort {
    ToolInvokeView invokeTool(String toolId, InvokeToolCommand command);
}
