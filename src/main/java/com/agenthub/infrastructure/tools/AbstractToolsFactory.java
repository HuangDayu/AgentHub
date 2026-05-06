package com.agenthub.infrastructure.tools;

import com.agenthub.domain.model.AgentToolInfo;
import org.springframework.ai.tool.ToolCallback;

import java.util.Set;

/**
 * @author huangdayu
 */
public interface AbstractToolsFactory {


    AgentToolInfo getToolInfo();

    Set<ToolCallback> getToolCallbacks();

}
