package com.agenthub.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

import static com.agenthub.common.utils.RandomUtils.randomId;

/**
 * 护栏策略聚合根。
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class GuardrailStrategy {
    private String id;
    private String tenantId;
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
    private Instant createdAt;
    private Instant updatedAt;

    private GuardrailStrategy(String id, String workspaceId, Instant createdAt) {
        this.id = id;
        this.tenantId = null; // tenantId由MyBatis-Plus拦截器自动填充
        this.workspaceId = workspaceId;
        this.createdAt = createdAt;
        this.maxInputLength = 10000;
        this.maxOutputLength = 4000;
    }

    public static GuardrailStrategy create(String workspaceId, String name) {
        GuardrailStrategy strategy = new GuardrailStrategy(randomId(), workspaceId, Instant.now());
        strategy.name = name;
        strategy.updatedAt = strategy.createdAt;
        return strategy;
    }

    /**
     * 从持久化层重建对象（保留所有原始数据）。
     */
    public static GuardrailStrategy rebuild(
            String id, String workspaceId, String name, String description,
            boolean inputValidationEnabled, boolean outputValidationEnabled,
            boolean piiDetectionEnabled, boolean piiMaskingEnabled,
            boolean promptInjectionDetection,
            int maxInputLength, int maxOutputLength,
            Instant createdAt, Instant updatedAt) {
        GuardrailStrategy strategy = new GuardrailStrategy(id, workspaceId, createdAt);
        strategy.name = name;
        strategy.description = description;
        strategy.inputValidationEnabled = inputValidationEnabled;
        strategy.outputValidationEnabled = outputValidationEnabled;
        strategy.piiDetectionEnabled = piiDetectionEnabled;
        strategy.piiMaskingEnabled = piiMaskingEnabled;
        strategy.promptInjectionDetection = promptInjectionDetection;
        strategy.maxInputLength = maxInputLength;
        strategy.maxOutputLength = maxOutputLength;
        strategy.updatedAt = updatedAt;
        return strategy;
    }

    public void updateBasicInfo(String name, String description) {
        this.name = name;
        this.description = description;
        this.updatedAt = Instant.now();
    }

    public void enableInputValidation() {
        this.inputValidationEnabled = true;
        this.updatedAt = Instant.now();
    }

    public void disableInputValidation() {
        this.inputValidationEnabled = false;
        this.updatedAt = Instant.now();
    }

    public void enableOutputValidation() {
        this.outputValidationEnabled = true;
        this.updatedAt = Instant.now();
    }

    public void disableOutputValidation() {
        this.outputValidationEnabled = false;
        this.updatedAt = Instant.now();
    }

    public void enablePiiDetection(boolean masking) {
        this.piiDetectionEnabled = true;
        this.piiMaskingEnabled = masking;
        this.updatedAt = Instant.now();
    }

    public void disablePiiDetection() {
        this.piiDetectionEnabled = false;
        this.piiMaskingEnabled = false;
        this.updatedAt = Instant.now();
    }

    public void enablePromptInjectionDetection() {
        this.promptInjectionDetection = true;
        this.updatedAt = Instant.now();
    }

    public void disablePromptInjectionDetection() {
        this.promptInjectionDetection = false;
        this.updatedAt = Instant.now();
    }

    public void setLengthLimits(int maxInput, int maxOutput) {
        this.maxInputLength = maxInput;
        this.maxOutputLength = maxOutput;
        this.updatedAt = Instant.now();
    }

}
