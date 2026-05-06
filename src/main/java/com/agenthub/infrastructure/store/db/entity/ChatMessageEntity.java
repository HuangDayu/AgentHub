package com.agenthub.infrastructure.store.db.entity;
import lombok.Data;

import com.baomidou.mybatisplus.annotation.*;

import java.time.Instant;

/**
 * 聊天消息持久化对象。
 */
@Data
@TableName("app.chat_message")
public class ChatMessageEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String sessionId;
    private String role;
    private String content;
    @TableField(value = "created_at",fill = FieldFill.INSERT)
    private Instant createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
