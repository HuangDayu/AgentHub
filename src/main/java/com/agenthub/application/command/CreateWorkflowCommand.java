package com.agenthub.application.command;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 创建动态工作流命令。
 */
@Data
@NoArgsConstructor
public class CreateWorkflowCommand {
    private String agentId;
    private String sessionId;
    private String task;
    private String pattern;
    private List<StageInput> stages;

    /**
     * 阶段输入。
     */
    @Data
    @NoArgsConstructor
    public static class StageInput {
        private String name;
        private String stageType;
        private String systemPrompt;
        private String taskTemplate;
        private List<String> dependsOn;
        private List<TaskInput> tasks;
    }

    /**
     * 任务输入。
     */
    @Data
    @NoArgsConstructor
    public static class TaskInput {
        private String taskDescription;
        private String modelConfigId;
        private List<String> toolNames;
    }
}
