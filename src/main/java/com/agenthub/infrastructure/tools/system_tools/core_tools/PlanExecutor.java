package com.agenthub.infrastructure.tools.system_tools.core_tools;

import com.agenthub.application.dto.PlanStepOutput;
import com.agenthub.application.usecase.ExecutionPlanUseCase;
import com.agenthub.application.port.out.tools.ToolCallbackResolverPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * 计划自动执行引擎，读取计划并自动调度工具调用。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlanExecutor {

    private final ExecutionPlanUseCase executionPlanUseCase;
    private final ToolCallbackResolverPort toolCallbackResolver;
    private static final int MAX_STEPS = 1000;

    public String executePlan(String planId) {
        executionPlanUseCase.startExecution(planId);
        int stepCount = 0;
        int completedCount = 0;

        while (stepCount < MAX_STEPS) {
            List<PlanStepOutput> steps = executionPlanUseCase.getNextSteps(planId);
            if (steps.isEmpty()) break;

            for (PlanStepOutput step : steps) {
                stepCount++;
                executionPlanUseCase.updateStep(planId, step.getId(), "RUNNING", null);
                String result = executeStep(step);
                String status = isErrorResult(result) ? "FAILED" : "COMPLETED";
                executionPlanUseCase.updateStep(planId, step.getId(), status, result);
                if ("COMPLETED".equals(status)) completedCount++;
            }
        }

        if (stepCount >= MAX_STEPS) {
            executionPlanUseCase.failPlan(planId, "超过最大步骤数限制");
            return "执行失败: 超过最大步骤数限制 " + MAX_STEPS;
        }

        String summary = "自动执行完成: " + completedCount + "/" + stepCount + " 个步骤成功";
        executionPlanUseCase.completePlan(planId, summary);
        return summary;
    }

    private String executeStep(PlanStepOutput step) {
        if (step.getToolName() == null || step.getToolName().isBlank()) {
            return "步骤无工具指定，已跳过";
        }
        return toolCallbackResolver.resolveByName(step.getToolName())
                .map(cb -> callTool(cb, step.getToolInput()))
                .orElse("工具不存在: " + step.getToolName());
    }

    private String callTool(Object callback, String input) {
        if (callback instanceof ToolCallback tc) {
            try {
                return tc.call(input != null ? input : "{}");
            } catch (Exception e) {
                log.error("工具调用失败", e);
                return "执行失败: " + e.getMessage();
            }
        }
        return "工具类型不支持";
    }

    private boolean isErrorResult(String result) {
        return result != null && (result.startsWith("工具不存在") || result.startsWith("执行失败"));
    }
}
