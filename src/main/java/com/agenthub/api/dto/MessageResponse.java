package com.agenthub.api.dto;

import java.time.Instant;

/**
 * 消息响应体。
 */
public record MessageResponse(String id, String sessionId, String role, String content, Instant createdAt) {
}
