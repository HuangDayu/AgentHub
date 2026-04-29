package com.agenthub.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 会话聚合根，管理会话内的消息历史。
 */
public class Session {
    private final String id;
    private final String agentId;
    private final String tenantId;
    private final String workspaceId;
    private final Instant createdAt;
    private final List<ChatMessage> messages;

    public Session(String id, String agentId, String tenantId, String workspaceId, Instant createdAt) {
        this.id = id;
        this.agentId = agentId;
        this.tenantId = tenantId;
        this.workspaceId = workspaceId;
        this.createdAt = createdAt;
        this.messages = new ArrayList<>();
    }

    public Session(String id, String agentId, String tenantId, String workspaceId, Instant createdAt, List<ChatMessage> messages) {
        this.id = id;
        this.agentId = agentId;
        this.tenantId = tenantId;
        this.workspaceId = workspaceId;
        this.createdAt = createdAt;
        this.messages = messages;
    }

    /**
     * 创建新的会话实例。
     *
     * @param agentId     所属智能体ID
     * @param tenantId    租户ID
     * @param workspaceId 工作空间ID
     * @return 新创建的会话对象
     */
    public static Session create(String agentId, String tenantId, String workspaceId) {
        return new Session(null, agentId, tenantId, workspaceId, Instant.now());
    }

    public String getId() {
        return id;
    }

    public String getAgentId() {
        return agentId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<ChatMessage> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    /**
     * 向会话添加用户消息。
     *
     * @param content 消息内容
     */
    public void addUserMessage(String content) {
        messages.add(ChatMessage.user(content).withSessionId(this.id));
    }

    /**
     * 向会话添加助手回复消息。
     *
     * @param content 回复内容
     */
    public void addAssistantMessage(String content) {
        messages.add(ChatMessage.assistant(content).withSessionId(this.id));
    }
}
