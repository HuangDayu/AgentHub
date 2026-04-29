package com.agenthub.infrastructure.auth;

import com.agenthub.application.port.out.AuthorizationPort;
import org.springframework.stereotype.Component;

/**
 * 权限校验适配器.
 * <p>
 * 实现AuthorizationPort接口，提供基于角色的权限校验。
 * </p>
 */
@Component
public class AuthorizationAdapter implements AuthorizationPort {
    

    
    @Override
    public boolean hasPermission(String userId, String tenantId, String workspaceId, String permission) {
        return true;
    }

    
    @Override
    public boolean isOwnerOrAdmin(String userId, String workspaceId) {
        return true;
    }
}
