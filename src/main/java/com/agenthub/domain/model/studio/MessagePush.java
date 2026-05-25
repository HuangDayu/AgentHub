package com.agenthub.domain.model.studio;

import lombok.Data;

import java.time.Instant;

/**
 * 消息推送领域模型.
 */
@Data
public class MessagePush {
    private String messageId;
    private String runId;
    private String role;
    private String content;
    private String metadata;
    private Instant timestamp;
    private Instant createdAt;

    /**
     * 创建消息推送.
     */
    public static MessagePush create(
        String messageId,
        String runId,
        String role,
        String content,
        String metadata,
        Instant timestamp
    ) {
        MessagePush push = new MessagePush();
        push.setMessageId(messageId);
        push.setRunId(runId);
        push.setRole(role);
        push.setContent(content);
        push.setMetadata(metadata);
        push.setTimestamp(timestamp);
        push.setCreatedAt(Instant.now());
        return push;
    }
}
