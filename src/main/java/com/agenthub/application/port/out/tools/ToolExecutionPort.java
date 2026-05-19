package com.agenthub.application.port.out.tools;

import java.util.Map;

/**
 * 工具执行端口接口.
 */
public interface ToolExecutionPort {

    /**
     * 执行工具.
     *
     * @param toolName 工具名称
     * @param parameters 工具参数
     * @return 执行结果
     */
    Object execute(String toolName, Map<String, Object> parameters);
}
