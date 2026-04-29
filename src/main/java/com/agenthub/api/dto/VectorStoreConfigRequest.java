package com.agenthub.api.dto;

import com.agenthub.domain.model.VectorStoreType;

/**
 * 向量库配置创建/更新请求 DTO。
 */
public record VectorStoreConfigRequest(
        String name,
        VectorStoreType type,
        String host,
        Integer port,
        String apiKey,
        String collectionName,
        String extraParams,
        Boolean enabled
) {
}
