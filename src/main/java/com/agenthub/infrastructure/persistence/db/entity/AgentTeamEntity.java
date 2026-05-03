package com.agenthub.infrastructure.persistence.db.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.Instant;

@TableName("app.agent_team")
public class AgentTeamEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    @TableField(value = "tenant_id", fill = FieldFill.INSERT)
    private String tenantId;
    @TableField(value = "workspace_id", fill = FieldFill.INSERT)
    private String workspaceId;
    @TableField(value = "team_code")
    private String teamCode;
    private String name;
    private String description;
    @TableField(value = "coordination_mode")
    private String coordinationMode;
    @TableField(value = "member_config")
    private String memberConfig;
    private String status;
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;
    @TableField(value = "updated_at", fill = FieldFill.INSERT)
    private Instant updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(String workspaceId) { this.workspaceId = workspaceId; }
    public String getTeamCode() { return teamCode; }
    public void setTeamCode(String teamCode) { this.teamCode = teamCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCoordinationMode() { return coordinationMode; }
    public void setCoordinationMode(String coordinationMode) { this.coordinationMode = coordinationMode; }
    public String getMemberConfig() { return memberConfig; }
    public void setMemberConfig(String memberConfig) { this.memberConfig = memberConfig; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
