package com.agenthub.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建子智能体请求DTO。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubagentRequest {
    private String name;
    private String description;
    private String systemPrompt;
    private String modelConfigId;
    
    private String sessionId;
}
