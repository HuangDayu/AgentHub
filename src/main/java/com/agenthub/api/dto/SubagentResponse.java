package com.agenthub.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 子智能体响应DTO。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubagentResponse {
    private String id;
    private String tenantId;
    private String workspaceId;
    private String parentAgentId;
    
    private String name;
    private String description;
    private String systemPrompt;
    private String modelConfigId;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
}
