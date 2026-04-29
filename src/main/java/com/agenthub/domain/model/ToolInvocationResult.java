package com.agenthub.domain.model;

import java.util.Map;

/** 工具调用结果领域模型，封装工具执行后的返回数据。 */
public record ToolInvocationResult(
        /** 工具标识 */
        String toolId,
        /** 调用状态（如 SUCCESS、FAILED） */
        String status,
        /** 调用输出数据 */
        Map<String, Object> output) {
}
