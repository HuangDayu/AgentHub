package com.agenthub.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentDataSourceResponse {
    private String id;
    private String tenantId;
    private String workspaceId;
    private String name;
    private String description;
    private String protocol;
    private String endpointUri;
    private String propertiesJson;
    private boolean enabled;
    private String status;
    private String lastErrorMessage;
    private Instant lastCheckedAt;
    private String permissionPolicyId;
    private String schemaId;
    private Instant createdAt;
    private Instant updatedAt;
}
