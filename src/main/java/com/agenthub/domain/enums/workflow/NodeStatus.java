package com.agenthub.domain.enums.workflow;

/**
 * 工作流节点状态枚举。
 * 定义节点在执行过程中的各种状态。
 *
 * @author huangdayu
 */
public enum NodeStatus {

    /** 待执行 */
    PENDING("待执行"),

    /** 执行中 */
    EXECUTING("执行中"),

    /** 执行成功 */
    SUCCESS("执行成功"),

    /** 执行失败 */
    FAILED("执行失败"),

    /** 已跳过 */
    SKIPPED("已跳过"),

    /** 等待中（等待上游节点） */
    WAITING("等待中"),

    /** 已取消 */
    CANCELLED("已取消"),

    /** 超时 */
    TIMEOUT("超时");

    private final String description;

    NodeStatus(String description) {
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
     * 判断是否为终态。
     *
     * @return 如果是终态返回true
     */
    public boolean isTerminal() {
        return this == SUCCESS || this == FAILED || this == SKIPPED
            || this == CANCELLED || this == TIMEOUT;
    }

    /**
     * 判断是否可以执行。
     *
     * @return 如果可以执行返回true
     */
    public boolean canExecute() {
        return this == PENDING || this == WAITING;
    }
}
