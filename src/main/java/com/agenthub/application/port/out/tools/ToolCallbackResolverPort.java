package com.agenthub.application.port.out.tools;

import com.agenthub.domain.enums.AgentToolType;
import com.agenthub.domain.model.agent.AgentToolInfo;

import java.util.List;
import java.util.Set;

/**
 * 工具回调解析端口，用于将工具配置解析为可执行的工具回调。
 */
public interface ToolCallbackResolverPort {

    /**
     * 根据工具类型和工具信息列表解析工具回调。
     *
     * @param toolType 工具类型
     * @param toolIds  工具信息列表
     * @return 工具回调集合
     */
    Set<Object> resolveToolCallbacks(AgentToolType toolType, List<AgentToolInfo> toolIds);
}
