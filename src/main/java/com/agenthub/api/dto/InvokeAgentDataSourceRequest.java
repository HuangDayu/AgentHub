package com.agenthub.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 调用数据源请求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvokeAgentDataSourceRequest {
    private String body;
    private Map<String, Object> headers;
    private String userId;
    private String agentId;
    private String sessionId;
}
