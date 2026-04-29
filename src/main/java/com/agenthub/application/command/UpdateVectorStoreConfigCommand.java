package com.agenthub.application.command;

import java.util.UUID;

/**
 * 更新向量库配置命令。
 */
public record UpdateVectorStoreConfigCommand(
        String id,
        String name,
        String host,
        Integer port,
        String apiKey,
        String collectionName,
        String extraParams,
        Boolean enabled
) {
}
