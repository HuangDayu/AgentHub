package com.agenthub.domain.model.agent;

import java.time.Instant;

import static com.agenthub.common.utils.RandomUtils.randomId;

/**
 * Agent团队聚合根，管理多Agent协作团队。
 */
public class AgentTeam {
    private String id;
    private String tenantId;
    private String workspaceId;
    private String teamCode;
    private String name;
    private String description;
    private String coordinationMode;
    private String memberConfig;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;

    public AgentTeam() {
    }

    /**
     * 工厂方法所需字段快照。
     */
    public static final class CreationSpec {
        private final String tenantId;
        private final String workspaceId;
        private final String teamCode;
        private final String name;
        private final String description;
        private final String coordinationMode;
        private final String memberConfig;

        public CreationSpec(String tenantId, String workspaceId, String teamCode,
                                String name, String description, String coordinationMode,
                                String memberConfig) {
            this.tenantId = tenantId;
            this.workspaceId = workspaceId;
            this.teamCode = teamCode;
            this.name = name;
            this.description = description;
            this.coordinationMode = coordinationMode;
            this.memberConfig = memberConfig;
        }
    }

    public static AgentTeam create(CreationSpec spec) {
        AgentTeam team = new AgentTeam();
        Instant now = Instant.now();
        applySpecToTeam(team, spec);
        team.id = randomId();
        team.status = "DRAFT";
        team.createdAt = now;
        team.updatedAt = now;
        return team;
    }

    private static void applySpecToTeam(AgentTeam team, CreationSpec spec) {
        team.tenantId = spec.tenantId;
        team.workspaceId = spec.workspaceId;
        team.teamCode = spec.teamCode;
        team.name = spec.name;
        team.description = spec.description;
        team.coordinationMode = spec.coordinationMode;
        team.memberConfig = spec.memberConfig;
    }

    public void update(String name, String description, String coordinationMode, String memberConfig) {
        this.name = name;
        this.description = description;
        this.coordinationMode = coordinationMode;
        this.memberConfig = memberConfig;
        this.updatedAt = Instant.now();
    }

    public void activate() {
        this.status = "ACTIVE";
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        this.status = "INACTIVE";
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

    public String getTeamCode() {
        return teamCode;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCoordinationMode() {
        return coordinationMode;
    }

    public String getMemberConfig() {
        return memberConfig;
    }

    public String getStatus() {
        return status;
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

    public void setTeamCode(String teamCode) {
        this.teamCode = teamCode;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCoordinationMode(String coordinationMode) {
        this.coordinationMode = coordinationMode;
    }

    public void setMemberConfig(String memberConfig) {
        this.memberConfig = memberConfig;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
