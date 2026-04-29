package com.agenthub.domain.model;

/**
 * 向量数据库供应商类型枚举。
 */
public enum VectorStoreType {
    QDRANT,
    MILVUS,
    WEAVIATE,
    CHROMA,
    PGVECTOR,
    REDIS
}
