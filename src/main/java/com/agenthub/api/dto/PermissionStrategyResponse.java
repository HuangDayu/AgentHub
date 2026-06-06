package com.agenthub.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionStrategyResponse {
    private String id;
    private String tenantId;
    private String workspaceId;
    private String name;
    private String description;
    private Set<String> allowedRoles;
    private Set<String> allowedOperations;
    private Set<String> protocolBlocklist;
    private boolean dangerousSqlBlock;
    private Set<String> requireApprovalFor;
    private Map<String, Set<String>> tablePermissions;
    private int rateLimitPerMinute;
    private int rateLimitPerHour;
    private boolean auditLogEnabled;
    private int auditLogRetentionDays;
    private boolean piiMaskingOnResult;
    private Instant createdAt;
    private Instant updatedAt;
}
