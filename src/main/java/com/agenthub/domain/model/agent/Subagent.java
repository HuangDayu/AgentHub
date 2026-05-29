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
     * 创建新的子智能体实例。
     *
     * @param tenantId      租户ID
     * @param workspaceId   工作空间ID
     * @param parentAgentId 父Agent ID
     * @param name          子Agent名称
     * @param description   子Agent描述
     * @param systemPrompt  系统提示词
     * @param modelConfigId 模型配置ID
     * @return 新创建的子Agent对象
     */
    public static Subagent create(String tenantId, String workspaceId, String parentAgentId,
                                  String name, String description, String systemPrompt,
                                  String modelConfigId) {
        Subagent subagent = new Subagent();
        subagent.tenantId = tenantId;
        subagent.workspaceId = workspaceId;
        subagent.parentAgentId = parentAgentId;
        subagent.name = name;
        subagent.description = description;
        subagent.systemPrompt = systemPrompt;
        subagent.modelConfigId = modelConfigId;
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
