package com.agenthub.api.dto;

/**
 * 模型测试响应。
 */
public record ModelTestResponse(
        boolean success,
        String message,
        String details
) {}
