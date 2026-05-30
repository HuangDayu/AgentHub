package com.agenthub.application.port.out.tools;

import com.agenthub.domain.enums.AgentToolType;
import com.agenthub.domain.model.agent.AgentToolInfo;

import java.util.List;
import java.util.Set;

/**
 * 工具回调解析端口，用于将工具配置解析为可执行的工具回调。
 */
public interface ToolCallbackResolverPort {

    Set<Object> resolveToolCallbacks(AgentToolType toolType, List<AgentToolInfo> toolIds);

    /**
     * 根据工具名称解析工具回调。
     *
     * @param toolName 工具名称
     * @return 工具回调（可能为空）
     */
    java.util.Optional<Object> resolveByName(String toolName);
}
