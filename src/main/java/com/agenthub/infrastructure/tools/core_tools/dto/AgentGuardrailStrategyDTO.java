package com.agenthub.infrastructure.tools.core_tools.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent护栏策略DTO，仅暴露Agent决策所需的护栏策略信息。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentGuardrailStrategyDTO {
    private String id;
    private String name;
    private String description;
    private boolean inputValidationEnabled;
    private boolean outputValidationEnabled;
    private boolean piiDetectionEnabled;
    private boolean piiMaskingEnabled;
    private boolean promptInjectionDetection;
    private int maxInputLength;
    private int maxOutputLength;
}
