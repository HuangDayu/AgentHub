package com.agenthub.api.dto;

/**
 * 更新租户请求DTO.
 */
public record PatchTenantRequest(
        /** 租户名称 */String name
) {
}
