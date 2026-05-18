package com.agenthub.domain.enums;

/**
 * 入库流水线状态转换枚举，定义各阶段的流转顺序。
 */
public enum JobPhase {
    PARSING,
    CHUNKING,
    VECTORIZING,
    COMPLETED
}
