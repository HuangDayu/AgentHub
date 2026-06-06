package com.agenthub.application.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 调用 Agent 数据源命令
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvokeAgentDataSourceCommand {
    private String userId;
    private String agentId;
    private String sessionId;
    private String body;
    private java.util.Map<String, Object> headers;
}
