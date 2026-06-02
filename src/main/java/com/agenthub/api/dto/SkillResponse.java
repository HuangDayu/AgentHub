package com.agenthub.api.dto;

import java.time.Instant;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 技能响应。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillResponse {
    private String id;
    private String tenantId;
    private String workspaceId;
    private String skillCode;
    private String name;
    private String description;
    private String skillType;
    private String skillPath;
    private String skillFilesTree;
    private String source;
    private String sourcePath;
    private String zipStoragePath;
    private String configId;
    private int fileCount;
    private long totalSize;
    private boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastSyncAt;
}
