package com.agenthub.application.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 子Agent消息输出。
 */
@Data
@NoArgsConstructor
public class SubagentMessageOutput {
    private String role;
    private String content;
    private Instant createdAt;
}
