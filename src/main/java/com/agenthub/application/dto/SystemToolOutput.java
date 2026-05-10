package com.agenthub.application.dto;

import java.time.Instant;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemToolOutput {
    private String id;
    private String tenantId;
    private String toolClassName;
    private String toolName;
    private String description;
    private String category;
    private int methodCount;
    private boolean enabled;
    private boolean systemTool;
    private Instant createdAt;
    private Instant updatedAt;
}
