package com.agenthub.application.dto;

import java.time.Instant;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemoryOutput {
    private String id;
    private String tenantId;
    private String workspaceId;
    private String agentId;
    private String memoryType;
    private String content;
    private String metadata;
    private double importance;
    private Instant expiresAt;
    private Instant createdAt;
    private Instant updatedAt;
}
