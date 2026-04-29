package com.agenthub.domain.model;

import java.time.Instant;

/**
 * 运行时会话记录，表示一个对话会话。
 *
 * @param id        会话ID
 * @param title     会话标题
 * @param createdAt 创建时间
 */
public record RuntimeSession(
        String id,
        String title,
        Instant createdAt
) {
}
