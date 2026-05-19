package com.agenthub.domain.model.agent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 会话聚合根，管理会话内的消息历史。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Session {
    private String id;
    private String agentId;
    private String name;
    private String tenantId;
    private String workspaceId;
    private Instant createdAt;
    private List<ChatMessage> messages;

    public Session(String id, String agentId, String name, String tenantId, String workspaceId, Instant createdAt) {
        this.id = id;
        this.agentId = agentId;
        this.name = name;
        this.tenantId = tenantId;
        this.workspaceId = workspaceId;
        this.createdAt = createdAt;
        this.messages = new ArrayList<>();
    }

    /**
     * 创建新的会话实例。
     *
     * @param agentId     所属智能体ID
     * @param name        会话名称
     * @param tenantId    租户ID
     * @param workspaceId 工作空间ID
     * @return 新创建的会话对象
     */
    public static Session create(String agentId, String name, String tenantId, String workspaceId) {
        return new Session(null, agentId, name, tenantId, workspaceId, Instant.now());
    }


}
