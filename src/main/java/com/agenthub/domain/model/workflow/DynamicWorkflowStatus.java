package com.agenthub.domain.model.workflow;

import lombok.Getter;

/**
 * 动态工作流状态枚举。
 */
@Getter
public enum DynamicWorkflowStatus {

    PLANNING("规划中"),
    EXECUTING("执行中"),
    VERIFYING("验证中"),
    COMPLETED("已完成"),
    FAILED("失败"),
    CANCELLED("已取消");

    private final String displayName;

    DynamicWorkflowStatus(String displayName) {
        this.displayName = displayName;
    }
}
