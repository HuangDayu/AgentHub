package com.agenthub.application.command;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateGuardrailStrategyCommand {
    private String workspaceId;
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
