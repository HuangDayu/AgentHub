package com.agenthub.api.dto;

import java.time.Instant;

/**
 * 错误响应DTO。
 * <p>
 * 用于统一返回API错误信息。
 * </p>
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String message,
        String messageId
) {
}