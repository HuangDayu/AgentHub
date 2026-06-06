package com.agenthub.domain.exception;

/**
 * 数据源参数校验异常
 */
public class AgentDataSourceValidationException extends ValidationException {
    public AgentDataSourceValidationException(String message) {
        super(message);
    }
}
