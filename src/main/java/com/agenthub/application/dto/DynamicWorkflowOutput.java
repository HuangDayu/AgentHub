package com.agenthub.application.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * 动态工作流输出。
 */
@Data
@NoArgsConstructor
public class DynamicWorkflowOutput {
    private String id;
    private String agentId;
    private String sessionId;
    private String task;
    private String pattern;
    private String status;
    private List<WorkflowStageOutput> stages;
    private String result;
    private int maxConcurrentAgents;
    private int totalTokensUsed;
    private int progressPercent;
    private int stageCount;
    private int completedStageCount;
    private Instant createdAt;
    private Instant updatedAt;
}
