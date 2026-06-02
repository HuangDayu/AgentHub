package com.agenthub.domain.model.skill;

import java.time.Instant;

import static com.agenthub.common.utils.RandomUtils.randomId;

/**
 * 技能聚合根，管理Agent可调用的技能定义。
 */
public class Skill {
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

    public Skill() {
    }

    /**
     * 创建同步技能。
     */
    public static Skill createSynced(String tenantId, String workspaceId,
                                      String skillCode, String name,
                                      String description, String skillPath) {
        Skill skill = new Skill();
        skill.id = randomId();
        skill.tenantId = tenantId;
        skill.workspaceId = workspaceId;
        skill.skillCode = skillCode;
        skill.name = name;
        skill.description = description;
        skill.skillType = "SYNCED";
        skill.source = "LOCAL";
        skill.sourcePath = skillPath;
        skill.skillPath = skillPath;
        skill.enabled = true;
        skill.createdAt = Instant.now();
        skill.updatedAt = Instant.now();
        return skill;
    }

    /**
     * 创建从 URL 上传的技能。
     */
    public static Skill createFromUrl(String tenantId, String workspaceId,
                                       String skillCode, String name,
                                       String description, String zipUrl) {
        Skill skill = new Skill();
        skill.id = randomId();
        skill.tenantId = tenantId;
        skill.workspaceId = workspaceId;
        skill.skillCode = skillCode;
        skill.name = name;
        skill.description = description;
        skill.skillType = "UPLOADED";
        skill.source = "URL";
        skill.sourcePath = zipUrl;
        skill.enabled = true;
        skill.createdAt = Instant.now();
        skill.updatedAt = Instant.now();
        return skill;
    }

    /**
     * 创建从文件上传的技能。
     */
    public static Skill createFromUpload(String tenantId, String workspaceId,
                                          String skillCode, String name,
                                          String description) {
        Skill skill = new Skill();
        skill.id = randomId();
        skill.tenantId = tenantId;
        skill.workspaceId = workspaceId;
        skill.skillCode = skillCode;
        skill.name = name;
        skill.description = description;
        skill.skillType = "UPLOADED";
        skill.source = "UPLOAD";
        skill.enabled = true;
        skill.createdAt = Instant.now();
        skill.updatedAt = Instant.now();
        return skill;
    }

    /**
     * 更新文件统计。
     */
    public void updateFileStats(int fileCount, long totalSize) {
        this.fileCount = fileCount;
        this.totalSize = totalSize;
        this.updatedAt = Instant.now();
    }

    /**
     * 标记同步时间。
     */
    public void markSynced() {
        this.lastSyncAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * 更新基本信息。
     */
    public void update(String name, String description) {
        this.name = name;
        this.description = description;
        this.updatedAt = Instant.now();
    }

    public void enable() {
        this.enabled = true;
        this.updatedAt = Instant.now();
    }

    public void disable() {
        this.enabled = false;
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(String workspaceId) { this.workspaceId = workspaceId; }
    public String getSkillCode() { return skillCode; }
    public void setSkillCode(String skillCode) { this.skillCode = skillCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSkillType() { return skillType; }
    public void setSkillType(String skillType) { this.skillType = skillType; }
    public String getSkillPath() { return skillPath; }
    public void setSkillPath(String skillPath) { this.skillPath = skillPath; }
    public String getSkillFilesTree() { return skillFilesTree; }
    public void setSkillFilesTree(String skillFilesTree) { this.skillFilesTree = skillFilesTree; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getSourcePath() { return sourcePath; }
    public void setSourcePath(String sourcePath) { this.sourcePath = sourcePath; }
    public String getZipStoragePath() { return zipStoragePath; }
    public void setZipStoragePath(String zipStoragePath) { this.zipStoragePath = zipStoragePath; }
    public String getConfigId() { return configId; }
    public void setConfigId(String configId) { this.configId = configId; }
    public int getFileCount() { return fileCount; }
    public void setFileCount(int fileCount) { this.fileCount = fileCount; }
    public long getTotalSize() { return totalSize; }
    public void setTotalSize(long totalSize) { this.totalSize = totalSize; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public Instant getLastSyncAt() { return lastSyncAt; }
    public void setLastSyncAt(Instant lastSyncAt) { this.lastSyncAt = lastSyncAt; }
}
