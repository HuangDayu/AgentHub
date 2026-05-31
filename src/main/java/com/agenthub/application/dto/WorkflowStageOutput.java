package com.agenthub.application.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * 工作流阶段输出。
 */
@Data
@NoArgsConstructor
public class WorkflowStageOutput {
    private String id;
    private String workflowId;
    private int order;
    private String name;
    private String stageType;
    private String systemPrompt;
    private String taskTemplate;
    private List<AgentTaskOutput> tasks;
    private List<String> dependencyIds;
    private String status;
    private String output;
    private int completedTaskCount;
    private int totalTaskCount;
    private Instant createdAt;
    private Instant updatedAt;
}
