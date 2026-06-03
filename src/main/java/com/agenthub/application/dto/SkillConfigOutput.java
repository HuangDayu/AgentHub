package com.agenthub.application.dto;

import java.time.Instant;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 技能配置输出。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillConfigOutput {
    private String id;
    private String tenantId;
    private String workspaceId;
    private String name;
    private String description;
    private List<String> skillPaths;
    private boolean syncEnabled;
    private int syncInterval;
    private boolean autoSync;
    private boolean enabled;
    private Instant lastSyncAt;
    private Instant createdAt;
    private Instant updatedAt;
}
