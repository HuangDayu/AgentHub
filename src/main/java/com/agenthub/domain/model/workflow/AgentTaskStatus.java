package com.agenthub.domain.model.workflow;

import lombok.Getter;

/**
 * 工作流任务状态枚举。
 */
@Getter
public enum AgentTaskStatus {

    PENDING("待执行"),
    RUNNING("执行中"),
    COMPLETED("已完成"),
    FAILED("失败");

    private final String displayName;

    AgentTaskStatus(String displayName) {
        this.displayName = displayName;
    }
}
