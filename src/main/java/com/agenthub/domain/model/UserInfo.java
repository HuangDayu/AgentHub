package com.agenthub.domain.model;

import java.util.List;

/**
 * 用户信息领域对象.
 * <p>
 * 包含当前登录用户的基本信息。
 * </p>
 */
public record UserInfo(
        /** 用户ID */ String id,
        /** 用户名 */ String username,
        /** 租户ID */ String tenantId,
        /** 用户角色列表 */ List<String> roles
) {
}
