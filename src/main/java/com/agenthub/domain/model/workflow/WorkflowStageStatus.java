package com.agenthub.domain.model.workflow;

import lombok.Getter;

/**
 * 工作流阶段状态枚举。
 */
@Getter
public enum WorkflowStageStatus {

    PENDING("待执行"),
    RUNNING("执行中"),
    COMPLETED("已完成"),
    FAILED("失败"),
    SKIPPED("已跳过");

    private final String displayName;

    WorkflowStageStatus(String displayName) {
        this.displayName = displayName;
    }
}
