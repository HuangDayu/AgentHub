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
     * 工厂方法所需字段快照。
     */
    public static final class CreationSpec {
        private final String messageId;
        private final String runId;
        private final String role;
        private final String content;
        private final String metadata;
        private final Instant timestamp;

        public CreationSpec(String messageId, String runId, String role,
                               String content, String metadata, Instant timestamp) {
            this.messageId = messageId;
            this.runId = runId;
            this.role = role;
            this.content = content;
            this.metadata = metadata;
            this.timestamp = timestamp;
        }
    }

    /**
     * 创建消息推送.
     */
    public static MessagePush create(CreationSpec spec) {
        MessagePush push = new MessagePush();
        applySpec(push, spec);
        return push;
    }

    private static void applySpec(MessagePush push, CreationSpec spec) {
        push.messageId = spec.messageId;
        push.runId = spec.runId;
        push.role = spec.role;
        push.content = spec.content;
        push.metadata = spec.metadata;
        push.timestamp = spec.timestamp;
        push.createdAt = Instant.now();
    }
}
