package com.agenthub.api.dto;

/** 创建工具请求对象。 */
public record CreateToolRequest(
        /** 工具名称 */
        String name,
        /** 工具描述 */
        String description,
        /** 是否启用 */
        Boolean enabled) {
}
