package com.agenthub.domain.model.plan;

import lombok.Getter;

/**
 * 计划步骤状态枚举。
 */
@Getter
public enum PlanStepStatus {

    PENDING("待执行"),
    RUNNING("执行中"),
    COMPLETED("已完成"),
    FAILED("失败"),
    SKIPPED("已跳过");

    private final String displayName;

    PlanStepStatus(String displayName) {
        this.displayName = displayName;
    }
}
