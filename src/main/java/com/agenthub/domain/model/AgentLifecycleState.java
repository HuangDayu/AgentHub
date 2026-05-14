package com.agenthub.domain.model;

/**
 * Agent生命周期状态枚举
 * @author huangdayu
 */
public enum AgentLifecycleState {

    CREATED("已创建，未启动"),
    STARTING("正在启动"),
    RUNNING("运行中"),
    PAUSED("已暂停"),
    STOPPING("正在停止"),
    STOPPED("已停止"),
    ERROR("错误状态");

    private final String description;

    AgentLifecycleState(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}
