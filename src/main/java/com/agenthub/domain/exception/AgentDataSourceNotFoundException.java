package com.agenthub.domain.exception;

/**
 * 数据源未找到异常
 */
public class AgentDataSourceNotFoundException extends NotFoundException {
    public AgentDataSourceNotFoundException(String id) {
        super("agent data source not found: " + id);
    }
}
