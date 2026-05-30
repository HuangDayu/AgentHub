package com.agenthub.application.command;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 创建执行计划命令。
 */
@Data
@NoArgsConstructor
public class CreatePlanCommand {
    private String agentId;
    private String sessionId;
    private String goal;
    private List<PlanStepInput> steps;
}
