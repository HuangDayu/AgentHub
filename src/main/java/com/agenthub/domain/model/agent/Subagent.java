package com.agenthub.domain.model.agent;

import lombok.Data;

import java.time.Instant;

import static com.agenthub.common.utils.RandomUtils.randomId;

/**
 * 子智能体聚合根，表示在父Agent下创建的独立子Agent。
 */
@Data
public class Subagent {
    private String id;
    private String tenantId;
    private String workspaceId;
    private String parentAgentId;
    private String name;
    private String description;
    private String systemPrompt;
    private String modelConfigId;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;

    public Subagent() {
        this.id = randomId();
        this.status = "ACTIVE";
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * 工厂方法所需字段快照。
     */
    public static final class CreationSpec {
        private final String tenantId;
        private final String workspaceId;
        private final String parentAgentId;
        private final String name;
        private final String description;
        private final String systemPrompt;
        private final String modelConfigId;

        public CreationSpec(String tenantId, String workspaceId, String parentAgentId,
                                String name, String description, String systemPrompt,
                                String modelConfigId) {
            this.tenantId = tenantId;
            this.workspaceId = workspaceId;
            this.parentAgentId = parentAgentId;
            this.name = name;
            this.description = description;
            this.systemPrompt = systemPrompt;
            this.modelConfigId = modelConfigId;
        }
    }

    /**
     * 创建新的子智能体实例。
     */
    public static Subagent create(CreationSpec spec) {
        Subagent subagent = new Subagent();
        subagent.tenantId = spec.tenantId;
        subagent.workspaceId = spec.workspaceId;
        subagent.parentAgentId = spec.parentAgentId;
        subagent.name = spec.name;
        subagent.description = spec.description;
        subagent.systemPrompt = spec.systemPrompt;
        subagent.modelConfigId = spec.modelConfigId;
        return subagent;
    }

    /**
     * 更新子Agent信息。
     *
     * @param name          新名称
     * @param description   新描述
     * @param systemPrompt  新系统提示词
     * @param modelConfigId 新模型配置ID
     * @return 更新后的子Agent对象
     */
    public Subagent update(String name, String description, String systemPrompt, String modelConfigId) {
        this.name = name;
        this.description = description;
        this.systemPrompt = systemPrompt;
        this.modelConfigId = modelConfigId;
        this.updatedAt = Instant.now();
        return this;
    }

    /**
     * 标记为无效。
     *
     * @return 更新后的子Agent对象
     */
    public Subagent deactivate() {
        this.status = "INACTIVE";
        this.updatedAt = Instant.now();
        return this;
    }
}
