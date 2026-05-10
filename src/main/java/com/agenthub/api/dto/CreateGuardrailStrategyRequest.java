package com.agenthub.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateGuardrailStrategyRequest {
    private String name;
    private String description;
    private Boolean inputValidationEnabled;
    private Boolean outputValidationEnabled;
    private Boolean piiDetectionEnabled;
    private Boolean piiMaskingEnabled;
    private Boolean promptInjectionDetection;
    private Integer maxInputLength;
    private Integer maxOutputLength;
}
