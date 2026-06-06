package com.agenthub.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAgentDataSourceRequest {
    private String description;
    private String endpointUri;
    private String propertiesJson;
    private String permissionPolicyId;
    private String schemaId;
}
