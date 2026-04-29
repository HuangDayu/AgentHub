package com.agenthub.api.dto;

/**
 * 路由预览请求记录。
 *
 * @param tenantId 租户标识
 * @param model    模型名称
 */
public record RoutePreviewRequest(
        /** 租户标识 */
        String tenantId,
        /** 模型名称 */
        String model
) {
}
