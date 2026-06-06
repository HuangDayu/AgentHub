package com.agenthub.domain.model;

import com.agenthub.domain.enums.AgentDataSourceProtocol;
import com.agenthub.domain.enums.OperationLevel;
import com.agenthub.domain.enums.TableOperation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

/**
 * 权限策略 - 第 5 个策略
 * <p>细粒度 CRUD 授权，区别于传统层级 READ_ONLY/READ_WRITE/ADMIN。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionStrategy {
    private String id;
    private String tenantId;
    private String workspaceId;
    private String name;
    private String description;
    private Set<String> allowedRoles;
    private Set<OperationLevel> allowedOperations;
    private Set<AgentDataSourceProtocol> protocolBlocklist;
    private boolean dangerousSqlBlock;
    private Set<TableOperation> requireApprovalFor;
    private Map<String, Set<TableOperation>> tablePermissions;
    private int rateLimitPerMinute;
    private int rateLimitPerHour;
    private boolean auditLogEnabled;
    private int auditLogRetentionDays;
    private boolean piiMaskingOnResult;
    private Instant createdAt;
    private Instant updatedAt;
}
