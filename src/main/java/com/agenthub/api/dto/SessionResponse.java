package com.agenthub.api.dto;

import java.time.Instant;

/**
 * 会话响应体。
 */
public record SessionResponse(String id, String agentId, Instant createdAt) {
}
