package com.agenthub.api.dto;

import lombok.Data;

import java.util.Map;

/**
 * 创建 Alert 请求 DTO.
 */
@Data
public class CreateAlertRequest {
    private String alertLevel;
    private String alertType;

    private String title;
    private String message;

    private String runId;
    private String agentId;
    private String traceId;

    private Map<String, Object> metadata;
}
