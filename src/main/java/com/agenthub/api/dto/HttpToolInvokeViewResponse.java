package com.agenthub.api.dto;

import java.util.Map;

/**
 * 工具调用结果响应DTO。
 */
public record HttpToolInvokeViewResponse(
        String toolId,
        String status,
        Map<String, Object> output) {
}
