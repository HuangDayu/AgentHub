package com.agenthub.infrastructure.tools.core_tools.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * HTTP 调用结果 DTO。
 */
@Data
@NoArgsConstructor
public class HttpToolResult {
    private boolean success;
    private int statusCode;
    private String body;
    private long durationMs;
    private String toolName;
}
