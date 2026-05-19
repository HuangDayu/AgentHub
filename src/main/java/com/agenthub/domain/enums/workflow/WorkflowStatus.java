package com.agenthub.domain.enums.workflow;

/**
 * 工作流状态枚举。
 * 定义工作流执行的生命周期状态。
 *
 * @author huangdayu
 */
public enum WorkflowStatus {

    /** 草稿状态 */
    DRAFT("草稿"),

    /** 已发布 */
    PUBLISHED("已发布"),

    /** 已归档 */
    ARCHIVED("已归档"),

    /** 执行中 */
    EXECUTING("执行中"),

    /** 执行成功 */
    SUCCESS("执行成功"),

    /** 执行失败 */
    FAILED("执行失败"),

    /** 已暂停 */
    PAUSED("已暂停"),

    /** 已取消 */
    CANCELLED("已取消");

    private final String description;

    WorkflowStatus(String description) {
        this.description = description;
    }

    /**
     * 获取状态描述。
     *
     * @return 状态描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 判断是否可以执行。
     *
     * @return 如果可以执行返回true
     */
    public boolean canExecute() {
        return this == PUBLISHED || this == PAUSED;
    }

    /**
     * 判断是否为执行中状态。
     *
     * @return 如果是执行中状态返回true
     */
    public boolean isRunning() {
        return this == EXECUTING || this == PAUSED;
    }

    /**
     * 判断是否为终态。
     *
     * @return 如果是终态返回true
     */
    public boolean isTerminal() {
        return this == SUCCESS || this == FAILED || this == CANCELLED;
    }
}
