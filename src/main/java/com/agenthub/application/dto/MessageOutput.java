package com.agenthub.application.dto;

import java.time.Instant;

/**
 * 消息输出DTO。
 */
public record MessageOutput(
        /** 消息ID */String id,
        /** 会话ID */String sessionId,
        /** 角色 */String role,
        /** 内容 */String content,
        /** 创建时间 */Instant createdAt
) {
}
