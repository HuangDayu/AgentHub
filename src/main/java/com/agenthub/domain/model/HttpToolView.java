package com.agenthub.domain.model;

/** 工具视图对象，用于 API 响应。 */
public record HttpToolView(
        /** 工具唯一标识 */
        String id,
        /** 工具名称 */
        String name,
        /** 工具描述 */
        String description,
        /** 是否启用 */
        boolean enabled) {
}
