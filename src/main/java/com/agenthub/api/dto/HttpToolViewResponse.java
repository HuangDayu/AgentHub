package com.agenthub.api.dto;

/**
 * 工具视图响应DTO。
 */
public record HttpToolViewResponse(
        String id,
        String name,
        String description,
        boolean enabled) {
}
