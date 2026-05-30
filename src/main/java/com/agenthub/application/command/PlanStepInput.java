package com.agenthub.application.command;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 计划步骤输入。
 */
@Data
@NoArgsConstructor
public class PlanStepInput {
    private String description;
    private String toolName;
    private String toolInput;
    private List<String> dependsOn;
}
