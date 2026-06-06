package com.agenthub.application.port.out;

import com.agenthub.application.command.RateLimitCheckCommand;

/**
 * 权限校验端口 - 组合 ACL/限流
 */
public interface DataSourcePermissionPort {
    /**
     * 检查速率限制
     */
    void checkRateLimit(RateLimitCheckCommand cmd);

    /**
     * 解析用户角色
     */
    String getUserRole(String userId, String workspaceId);
}
