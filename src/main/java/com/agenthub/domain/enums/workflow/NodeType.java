package com.agenthub.domain.enums.workflow;

/**
 * 工作流节点类型枚举。
 * 定义工作流中支持的各种节点类型。
 *
 * @author huangdayu
 */
public enum NodeType {

    /** 大语言模型节点 */
    LLM("大语言模型节点"),

    /** API调用节点 */
    API("API调用节点"),

    /** 条件判断节点 */
    CONDITION("条件判断节点"),

    /** 循环节点 */
    LOOP("循环节点"),

    /** 并行执行节点 */
    PARALLEL("并行执行节点"),

    /** 变量设置节点 */
    VARIABLE("变量设置节点"),

    /** 代码执行节点 */
    CODE("代码执行节点"),

    /** 工具调用节点 */
    TOOL("工具调用节点"),

    /** 子工作流节点 */
    SUBWORKFLOW("子工作流节点"),

    /** 开始节点 */
    START("开始节点"),

    /** 结束节点 */
    END("结束节点");

    private final String description;

    NodeType(String description) {
        this.description = description;
    }

    /**
     * 获取节点类型描述。
     *
     * @return 节点类型描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 判断是否为控制流节点。
     *
     * @return 如果是控制流节点返回true
     */
    public boolean isControlFlow() {
        return this == CONDITION || this == LOOP || this == PARALLEL;
    }

    /**
     * 判断是否为执行节点。
     *
     * @return 如果是执行节点返回true
     */
    public boolean isExecutable() {
        return this != START && this != END;
    }
}
