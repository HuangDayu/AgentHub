package com.agenthub.application.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * 计划步骤输出。
 */
@Data
@NoArgsConstructor
public class PlanStepOutput {
    private String id;
    private String planId;
    private int order;
    private String description;
    private String toolName;
    private String toolInput;
    private String status;
    private String output;
    private String subagentId;
    private String subsessionId;
    private List<String> dependencyIds;
    private Instant createdAt;
    private Instant updatedAt;
}
