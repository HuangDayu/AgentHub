package com.agenthub.domain.model;

import java.time.Instant;

/**
 * 模型配置领域记录，表示租户级别的模型配置。
 */
public record ModelConfig(
        String id,
        String name,
        ModelType type,
        ModelSupplier supplier,
        String apiKey,
        String baseUrl,
        String model,
        Boolean enabled,
        Instant createdAt,
        Instant updatedAt,
        String createdBy
) {
    public ModelConfig {
        if (enabled == null) {
            enabled = true;
        }
    }
}