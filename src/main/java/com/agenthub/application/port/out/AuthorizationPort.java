package com.agenthub.application.port.out;


/**
 * 权限校验端口.
 * <p>
 * 定义权限校验的领域接口，遵循整洁架构的Port定义。
 * </p>
 */
public interface AuthorizationPort {
    
    /**
     * 检查用户是否有指定权限.
     *
     * @param userId     用户ID
     * @param tenantId   租户ID
     * @param workspaceId 工作空间ID（可为null表示租户级权限）
     * @param permission 权限标识（如 "agent:create", "knowledge:delete"）
     * @return 如果有权限返回true
     */
    boolean hasPermission(String userId, String tenantId, String workspaceId, String permission);
    
    /**
     * 检查用户是否是工作空间的所有者或管理员.
     *
     * @param userId      用户ID
     * @param workspaceId 工作空间ID
     * @return 如果是所有者或管理员返回true
     */
    boolean isOwnerOrAdmin(String userId, String workspaceId);
}
