package com.agenthub.application.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新 Agent 数据源命令
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAgentDataSourceCommand {
    private String description;
    private String endpointUri;
    private String propertiesJson;
    private String permissionPolicyId;
    private String schemaId;
    private String updatedBy;
}
