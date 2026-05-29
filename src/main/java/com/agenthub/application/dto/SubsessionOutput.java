package com.agenthub.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 子会话输出DTO。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubsessionOutput {
    private String id;
    private String parentSessionId;
    private String subagentId;
    private String name;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
}
