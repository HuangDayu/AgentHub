package com.agenthub.application.command;

import com.agenthub.domain.enums.AgentDataSourceProtocol;
import com.agenthub.domain.enums.OperationLevel;
import com.agenthub.domain.enums.TableOperation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

/**
 * 创建/更新权限策略命令
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpsertPermissionStrategyCommand {
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
    private boolean piiMaskingOnResult;
}
