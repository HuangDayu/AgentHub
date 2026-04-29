package com.agenthub.api.dto;

/**
 * 索引版本项响应DTO。
 */
public record IndexVersionItemResponse(
        String indexVersion,
        boolean active
) {
}
