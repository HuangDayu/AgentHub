package com.agenthub.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTenantRequest {
    private /** 租户编码 */String tenantCode;
    private /** 租户名称 */String name;
    private /** 套餐编码 */String planCode;
    private /** 隔离级别 */String isolationLevel;
    private /** 区域 */String region;
}
