package com.agenthub.application.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * 执行计划输出。
 */
@Data
@NoArgsConstructor
public class ExecutionPlanOutput {
    private String id;
    private String agentId;
    private String sessionId;
    private String goal;
    private String status;
    private int currentStepIndex;
    private String result;
    private List<PlanStepOutput> steps;
    private int stepCount;
    private int completedStepCount;
    private Instant createdAt;
    private Instant updatedAt;
}
