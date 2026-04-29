package com.agenthub.domain.model;

import java.time.Instant;

import static com.agenthub.common.utils.RandomUtils.randomId;

/**
 * 向量数据库配置领域对象。
 * <p>
 * 表示租户维度下的向量数据库配置，包含连接信息、集合配置等。
 * </p>
 */
public record VectorStoreConfig(
        /** 配置唯一标识 */
        String id,
        /** 向量库名称 */
        String name,
        /** 向量库类型 */
        VectorStoreType type,
        /** 主机地址 */
        String host,
        /** 端口号 */
        Integer port,
        /** API密钥 */
        String apiKey,
        /** 集合/表名称 */
        String collectionName,
        /** 额外配置参数（JSON） */
        String extraParams,
        /** 是否启用 */
        Boolean enabled,
        /** 创建时间 */
        Instant createdAt,
        /** 更新时间 */
        Instant updatedAt
) {
    /** 创建新的向量库配置（自动生成 ID 和时间） */
    public static VectorStoreConfig create(
            String name,
            VectorStoreType type,
            String host,
            Integer port,
            String apiKey,
            String collectionName,
            String extraParams
    ) {
        Instant now = Instant.now();
        return new VectorStoreConfig(
                randomId(),
                name,
                type,
                host,
                port,
                apiKey,
                collectionName,
                extraParams,
                true,
                now,
                now
        );
    }

    /** 复制并更新时间戳及可选更新字段 */
    public VectorStoreConfig withUpdates(
            String name,
            String host,
            Integer port,
            String apiKey,
            String collectionName,
            String extraParams,
            Boolean enabled
    ) {
        return new VectorStoreConfig(
                this.id,
                name != null ? name : this.name,
                this.type,
                host != null ? host : this.host,
                port != null ? port : this.port,
                apiKey != null ? apiKey : this.apiKey,
                collectionName != null ? collectionName : this.collectionName,
                extraParams != null ? extraParams : this.extraParams,
                enabled != null ? enabled : this.enabled,
                this.createdAt,
                Instant.now()
        );
    }
}
