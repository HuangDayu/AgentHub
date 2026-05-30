package com.agenthub.domain.model.plan;

import lombok.Getter;

/**
 * 执行计划状态枚举。
 */
@Getter
public enum PlanStatus {

    PLANNING("规划中"),
    EXECUTING("执行中"),
    COMPLETED("已完成"),
    FAILED("失败"),
    CANCELLED("已取消");

    private final String displayName;

    PlanStatus(String displayName) {
        this.displayName = displayName;
    }
}
