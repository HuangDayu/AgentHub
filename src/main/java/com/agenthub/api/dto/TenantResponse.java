package com.agenthub.api.dto;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.domain.model.Tenant;

import java.time.Instant;

/**
 * 租户响应DTO.
 */
public record TenantResponse(
        /** 租户ID */String id,
        /** 租户编码 */String tenantCode,
        /** 租户名称 */String name,
        /** 套餐编码 */String planCode,
        /** 隔离级别 */String isolationLevel,
        /** 租户状态 */String status,
        /** 区域 */String region,
        /** 创建时间 */Instant createdAt,
        /** 更新时间 */Instant updatedAt
) {
    /**
     * 从租户领域对象转换为响应DTO。
     *
     * @param tenant 租户领域对象
     * @return 租户响应DTO
     */
    public static TenantResponse from(Tenant tenant) {
        return BeanUtil.copyProperties(tenant, TenantResponse.class);
    }
}
