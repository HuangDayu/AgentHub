package com.agenthub.domain.model;

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
    private String definition;
    private String parameters;
    private boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;

    public Skill() {
    }

    private Skill(String id, String tenantId, String workspaceId, String skillCode,
                  String name, String description, String skillType,
                  String definition, String parameters, Instant createdAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.workspaceId = workspaceId;
        this.skillCode = skillCode;
        this.name = name;
        this.description = description;
        this.skillType = skillType;
        this.definition = definition;
        this.parameters = parameters;
        this.enabled = false;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public static Skill create(String tenantId, String workspaceId, String skillCode,
                               String name, String description, String skillType,
                               String definition, String parameters) {
        return new Skill(randomId(), tenantId, workspaceId, skillCode,
                name, description, skillType, definition, parameters, Instant.now());
    }

    public void update(String name, String description, String definition, String parameters) {
        this.name = name;
        this.description = description;
        this.definition = definition;
        this.parameters = parameters;
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

    public String getId() {
        return id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public String getSkillCode() {
        return skillCode;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getSkillType() {
        return skillType;
    }

    public String getDefinition() {
        return definition;
    }

    public String getParameters() {
        return parameters;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public void setSkillCode(String skillCode) {
        this.skillCode = skillCode;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSkillType(String skillType) {
        this.skillType = skillType;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
