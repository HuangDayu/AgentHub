package com.agenthub.domain.model;

import java.time.Instant;

/**
 * 运行时消息记录，表示会话中的单条消息。
 *
 * @param id        消息ID
 * @param sessionId 所属会话ID
 * @param role      消息角色（user/assistant）
 * @param content   消息内容
 * @param createdAt 创建时间
 */
public record RuntimeMessage(
        String id,
        String sessionId,
        String role,
        String content,
        Instant createdAt
) {
}

