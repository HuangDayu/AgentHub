package com.agenthub.application.service;

import com.agenthub.domain.model.data_source.AgentDataSource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据源调用上下文 - 打包用户/会话/数据源/请求/响应信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceInvokeContext {
    private String userId;
    private String agentId;
    private String sessionId;
    private AgentDataSource source;
    private Object request;
    private Object response;
    private long elapsedMs;
    private String status;
    private String errorMessage;
}
