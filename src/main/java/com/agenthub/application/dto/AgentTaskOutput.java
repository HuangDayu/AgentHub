package com.agenthub.application.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * 工作流任务输出。
 */
@Data
@NoArgsConstructor
public class AgentTaskOutput {
    private String id;
    private String stageId;
    private String workflowId;
    private String taskDescription;
    private String subagentId;
    private String subsessionId;
    private String status;
    private String result;
    private String modelConfigId;
    private List<String> toolNames;
    private Instant createdAt;
    private Instant updatedAt;
}
