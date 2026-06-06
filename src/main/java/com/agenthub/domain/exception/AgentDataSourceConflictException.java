package com.agenthub.domain.exception;

/**
 * 数据源冲突异常（如名称重复）
 */
public class AgentDataSourceConflictException extends ConflictException {
    public AgentDataSourceConflictException(String message) {
        super(message);
    }
}
