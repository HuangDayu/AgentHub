package com.agenthub.api.dto;

/**
 * 向量存储测试响应。
 */
public record VectorStoreTestResponse(
        boolean success,
        String message,
        String details
) {}
