package com.agenthub.domain.model.agent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

import static com.agenthub.common.utils.RandomUtils.randomId;

/**
 * 聊天消息实体，表示用户或助手的单条消息。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String id;
    private String sessionId;
    private String role;
    private String content;
    private Instant createdAt;

    public static ChatMessage user(String sessionId, String content) {
        return new ChatMessage(randomId(), sessionId, "USER", content, Instant.now());
    }


    public static ChatMessage assistant(String sessionId, String content) {
        return new ChatMessage(randomId(), sessionId, "ASSISTANT", content, Instant.now());
    }

    public static ChatMessage system(String sessionId, String content) {
        return new ChatMessage(randomId(), sessionId, "SYSTEM", content, Instant.now());
    }

    public static ChatMessage tool(String sessionId, String content) {
        return new ChatMessage(randomId(), sessionId, "TOOL", content, Instant.now());
    }


}
