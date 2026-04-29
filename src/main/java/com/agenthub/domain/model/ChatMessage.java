package com.agenthub.domain.model;

import java.time.Instant;

import static com.agenthub.common.utils.RandomUtils.randomId;

/**
 * 聊天消息实体，表示用户或助手的单条消息。
 */
public class ChatMessage {
    private final String id;
    private final String sessionId;
    private final String role; // "user" or "assistant"
    private final String content;
    private final Instant createdAt;

    public ChatMessage(String id, String sessionId, String role, String content, Instant createdAt) {
        this.id = id;
        this.sessionId = sessionId;
        this.role = role;
        this.content = content;
        this.createdAt = createdAt;
    }

    public ChatMessage(String role, String content) {
        this(randomId(), null, role, content, Instant.now());
    }

    public static ChatMessage message(String role, String content) {
        return new ChatMessage(randomId(), null, role, content, Instant.now());
    }

    /**
     * 创建用户消息。
     *
     * @param content 消息内容
     * @return 用户消息对象
     */
    public static ChatMessage user(String content) {
        return new ChatMessage(randomId(), null, "user", content, Instant.now());
    }

    /**
     * 创建助手回复消息。
     *
     * @param content 回复内容
     * @return 助手消息对象
     */
    public static ChatMessage assistant(String content) {
        return new ChatMessage(randomId(), null, "assistant", content, Instant.now());
    }

    public static ChatMessage system(String content) {
        return new ChatMessage(randomId(), null, "system", content, Instant.now());
    }

    /**
     * 设置会话ID。
     */
    public ChatMessage withSessionId(String sessionId) {
        return new ChatMessage(this.id, sessionId, this.role, this.content, this.createdAt);
    }

    public String getId() {
        return id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
