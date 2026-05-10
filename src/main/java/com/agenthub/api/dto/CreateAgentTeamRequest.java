package com.agenthub.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAgentTeamRequest {
    private String tenantId;
    private String workspaceId;
    private String teamCode;
    private String name;
    private String description;
    private String coordinationMode;
    private String memberConfig;
}
