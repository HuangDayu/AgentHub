package com.agenthub.domain.model.workflow;

import lombok.Getter;

/**
 * 动态工作流编排模式枚举。
 */
@Getter
public enum WorkflowPattern {

    FAN_OUT("扇出模式", "多个子Agent并行执行独立任务，结果合并"),
    PIPELINE("管道模式", "顺序执行，前一个阶段的输出作为下一个的输入"),
    JUDGE("评审模式", "多个Agent独立评审，第N+1个综合"),
    LOOP("循环模式", "重复执行直到条件满足或达到最大迭代"),
    TRIAGE("分类模式", "先分类再路由到专家Agent");

    private final String name;
    private final String description;

    WorkflowPattern(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
