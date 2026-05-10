package com.agenthub.api.dto;

import java.time.Instant;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentTeamResponse {
    private String id;
    private String tenantId;
    private String workspaceId;
    private String teamCode;
    private String name;
    private String description;
    private String coordinationMode;
    private String memberConfig;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
}
