package com.agenthub.application.command;

/**
 * 向执行计划添加步骤的命令。
 */
public final class AddStepCommand {
    private final String planId;
    private final String description;
    private final String toolName;
    private final String toolInput;

    public AddStepCommand(String planId, String description, String toolName, String toolInput) {
        this.planId = planId;
        this.description = description;
        this.toolName = toolName;
        this.toolInput = toolInput;
    }

    public String getPlanId() {
        return planId;
    }

    public String getDescription() {
        return description;
    }

    public String getToolName() {
        return toolName;
    }

    public String getToolInput() {
        return toolInput;
    }
}
