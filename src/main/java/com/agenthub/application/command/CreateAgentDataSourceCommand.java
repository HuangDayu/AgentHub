package com.agenthub.application.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建 Agent 数据源命令
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAgentDataSourceCommand {
    private String tenantId;
    private String workspaceId;
    private String name;
    private String description;
    private String protocol;
    private String endpointUri;
    private String propertiesJson;
    private String permissionPolicyId;
    private String schemaId;
    private String createdBy;
}
