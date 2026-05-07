package com.agenthub.infrastructure.tools;

import com.agenthub.domain.model.AgentToolInfo;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;
import java.util.Set;

/**
 * @author huangdayu
 */
public interface AbstractToolsFactory {


    AgentToolInfo getToolInfo();

    Set<ToolCallback> getAllToolCallbacks();

    Set<ToolCallback> getToolCallbacks(String name);

    Set<ToolCallback> getToolCallbacks(List<String> toolIds);
}
