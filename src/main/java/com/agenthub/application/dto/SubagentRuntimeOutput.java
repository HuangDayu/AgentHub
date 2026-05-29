package com.agenthub.application.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 子Agent运行时输出。
 */
@Data
@NoArgsConstructor
public class SubagentRuntimeOutput {
    private String subagentId;
    private String subsessionId;
    private String name;
    private String description;
    private String status;
    private String message;
    private String result;
    private Instant createdAt;
}
