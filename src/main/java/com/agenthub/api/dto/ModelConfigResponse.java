package com.agenthub.api.dto;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * 模型配置响应 DTO。
 */
public record ModelConfigResponse(
        String id,
        String name,
        String type,
        String supplier,
        String apiKey,
        String baseUrl,
        String model,
        Boolean enabled,
        Instant createdAt,
        Instant updatedAt,
        String createdBy
) {
}