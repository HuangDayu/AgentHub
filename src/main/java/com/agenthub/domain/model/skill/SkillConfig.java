package com.agenthub.domain.model.skill;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.agenthub.common.utils.RandomUtils.randomId;

/**
 * 技能配置，管理技能同步路径和策略。
 */
public class SkillConfig {
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

    public SkillConfig() {
        this.skillPaths = new ArrayList<>();
    }

    /**
     * 创建配置。
     */
    public static SkillConfig create(String tenantId, String workspaceId,
                                      String name, List<String> skillPaths) {
        SkillConfig config = new SkillConfig();
        config.id = randomId();
        config.tenantId = tenantId;
        config.workspaceId = workspaceId;
        config.name = name;
        config.skillPaths = skillPaths != null ? new ArrayList<>(skillPaths) : new ArrayList<>();
        config.syncEnabled = true;
        config.syncInterval = 3600;
        config.autoSync = false;
        config.enabled = true;
        config.createdAt = Instant.now();
        config.updatedAt = Instant.now();
        return config;
    }

    /**
     * 添加技能路径。
     */
    public void addSkillPath(String path) {
        if (!this.skillPaths.contains(path)) {
            this.skillPaths.add(path);
            this.updatedAt = Instant.now();
        }
    }

    /**
     * 移除技能路径。
     */
    public void removeSkillPath(String path) {
        if (this.skillPaths.remove(path)) {
            this.updatedAt = Instant.now();
        }
    }

    /**
     * 更新配置。
     */
    public void update(String name, String description, List<String> skillPaths,
                       boolean syncEnabled, int syncInterval, boolean autoSync) {
        this.name = name;
        this.description = description;
        this.skillPaths = skillPaths != null ? new ArrayList<>(skillPaths) : this.skillPaths;
        this.syncEnabled = syncEnabled;
        this.syncInterval = syncInterval;
        this.autoSync = autoSync;
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(String workspaceId) { this.workspaceId = workspaceId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<String> getSkillPaths() { return skillPaths; }
    public void setSkillPaths(List<String> skillPaths) { this.skillPaths = skillPaths; }
    public boolean isSyncEnabled() { return syncEnabled; }
    public void setSyncEnabled(boolean syncEnabled) { this.syncEnabled = syncEnabled; }
    public int getSyncInterval() { return syncInterval; }
    public void setSyncInterval(int syncInterval) { this.syncInterval = syncInterval; }
    public boolean isAutoSync() { return autoSync; }
    public void setAutoSync(boolean autoSync) { this.autoSync = autoSync; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    /**
     * 标记已同步。
     */
    public void markSynced() {
        this.lastSyncAt = Instant.now();
    }

    public Instant getLastSyncAt() { return lastSyncAt; }
    public void setLastSyncAt(Instant lastSyncAt) { this.lastSyncAt = lastSyncAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
