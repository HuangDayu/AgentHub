package com.agenthub.api.dto;

import java.time.Instant;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuardrailStrategyResponse {
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
    private Instant createdAt;
    private Instant updatedAt;
}
