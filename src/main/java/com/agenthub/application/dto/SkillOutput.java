package com.agenthub.application.dto;

import java.time.Instant;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillOutput {
    private String id;
    private String tenantId;
    private String workspaceId;
    private String skillCode;
    private String name;
    private String description;
    private String skillType;
    private String skillPath;
    private String skillFilesTree;
    private boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;
}
