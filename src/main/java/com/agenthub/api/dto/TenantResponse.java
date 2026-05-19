package com.agenthub.api.dto;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.domain.model.auth.Tenant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 租户响应DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantResponse {
    /** 租户ID */
    private String id;
    /** 租户编码 */
    private String tenantCode;
    /** 租户名称 */
    private String name;
    /** 套餐编码 */
    private String planCode;
    /** 隔离级别 */
    private String isolationLevel;
    /** 租户状态 */
    private String status;
    /** 区域 */
    private String region;
    /** 创建时间 */
    private Instant createdAt;
    /** 更新时间 */
    private Instant updatedAt;

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
