package com.agenthub.api.dto;

/**
 * 路由预览响应记录。
 *
 * @param routeId  路由策略标识
 * @param provider 模型提供方
 * @param model    模型名称
 * @param endpoint 调用端点
 * @param priority 优先级
 */
public record RoutePreviewResponse(
        /** 路由策略标识 */
        String routeId,
        /** 模型提供方 */
        String provider,
        /** 模型名称 */
        String model,
        /** 调用端点 */
        String endpoint,
        /** 优先级 */
        int priority
) {
}
