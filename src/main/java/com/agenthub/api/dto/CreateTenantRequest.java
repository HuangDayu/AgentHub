package com.agenthub.api.dto;

/**
 * 创建租户请求DTO.
 */
public record CreateTenantRequest(
        /** 租户编码 */String tenantCode,
        /** 租户名称 */String name,
        /** 套餐编码 */String planCode,
        /** 隔离级别 */String isolationLevel,
        /** 区域 */String region
) {
}
