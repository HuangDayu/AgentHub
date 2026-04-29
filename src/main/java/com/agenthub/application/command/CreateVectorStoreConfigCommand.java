package com.agenthub.application.command;

import com.agenthub.domain.model.VectorStoreType;

/**
 * 创建向量库配置命令。
 */
public record CreateVectorStoreConfigCommand(
        String name,
        VectorStoreType type,
        String host,
        Integer port,
        String apiKey,
        String collectionName,
        String extraParams
) {
    /**
     * 校验命令参数。
     */
    public void validate() {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name must not be blank");
        if (type == null) throw new IllegalArgumentException("type must not be null");
        if (host == null || host.isBlank()) throw new IllegalArgumentException("host must not be blank");
        if (port == null || port < 1 || port > 65535) throw new IllegalArgumentException("port must be between 1 and 65535");
        if (collectionName == null || collectionName.isBlank()) throw new IllegalArgumentException("collectionName must not be blank");
    }
}
