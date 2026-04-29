package com.agenthub.api.dto;

import com.agenthub.domain.model.VectorStoreConfig;
import com.agenthub.domain.model.VectorStoreType;

import java.time.Instant;

/**
 * 向量库配置响应 DTO。
 */
public record VectorStoreConfigResponse(
        String id,
        String name,
        VectorStoreType type,
        String host,
        Integer port,
        boolean hasApiKey,
        String collectionName,
        String extraParams,
        Boolean enabled,
        Instant createdAt,
        Instant updatedAt
        // 注意: tenantId 不暴露到响应中
) {
    /**
     * 从领域对象构建响应。
     */
    public static VectorStoreConfigResponse from(VectorStoreConfig config) {
        return new VectorStoreConfigResponse(
                config.id(),
                config.name(),
                config.type(),
                config.host(),
                config.port(),
                config.apiKey() != null && !config.apiKey().isEmpty(),
                config.collectionName(),
                config.extraParams(),
                config.enabled(),
                config.createdAt(),
                config.updatedAt()
        );
    }
}
