package com.agenthub.application.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 动态工作流摘要。
 */
@Data
@NoArgsConstructor
public class DynamicWorkflowSummary {
    private String id;
    private String task;
    private String pattern;
    private String status;
    private int progressPercent;
    private int stageCount;
    private int completedStageCount;
    private Instant createdAt;
}
