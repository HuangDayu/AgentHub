package com.agenthub.application.dto;

import java.time.Instant;

/**
 * 会话输出DTO。
 */
public record SessionOutput(
        /** 会话ID */String id,
        /** 智能体ID */String agentId,
        /** 创建时间 */Instant createdAt
) {
}
