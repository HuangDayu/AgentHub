package com.agenthub.application.dto;

import com.agenthub.domain.model.PermissionStrategy;
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
 * 权限策略输出
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionStrategyOutput {
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

    public static PermissionStrategyOutput from(PermissionStrategy p) {
        if (p == null) return null;
        PermissionStrategyOutput o = new PermissionStrategyOutput();
        cn.hutool.core.bean.BeanUtil.copyProperties(p, o);
        return o;
    }
}
