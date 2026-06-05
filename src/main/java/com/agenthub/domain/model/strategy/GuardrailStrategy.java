package com.agenthub.domain.model.strategy;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.agenthub.common.utils.RandomUtils.randomId;

/**
 * 护栏策略聚合根。
 * <p>
 * 控制输入输出安全，包括内容验证、PII 检测、提示注入防护等。
 * </p>
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

    /**
     * 持久化状态快照，用于在 rebuild 时一次性传入所有字段。
     */
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class State {
        private String id;
        private String workspaceId;
        private String name;
        private String description;
        private Boolean inputValidationEnabled;
        private Boolean outputValidationEnabled;
        private Boolean piiDetectionEnabled;
        private Boolean piiMaskingEnabled;
        private Boolean promptInjectionDetection;
        private Integer maxInputLength;
        private Integer maxOutputLength;
        private Instant createdAt;
        private Instant updatedAt;
    }

    private GuardrailStrategy(String id, String workspaceId, Instant createdAt) {
        this.id = id;
        this.tenantId = null;
        this.workspaceId = workspaceId;
        this.createdAt = createdAt;
        this.maxInputLength = 10000;
        this.maxOutputLength = 4000;
    }

    /**
     * 创建护栏策略实例。
     *
     * @param workspaceId 工作空间ID
     * @param name 策略名称
     * @return 策略实例
     */
    public static GuardrailStrategy create(String workspaceId, String name) {
        GuardrailStrategy strategy = new GuardrailStrategy(randomId(), workspaceId, Instant.now());
        strategy.name = name;
        strategy.updatedAt = strategy.createdAt;
        return strategy;
    }

    /**
     * 从持久化层重建对象。
     */
    public static GuardrailStrategy rebuild(State state) {
        GuardrailStrategy strategy = new GuardrailStrategy(state.getId(), state.getWorkspaceId(), state.getCreatedAt());
        strategy.name = state.getName();
        strategy.description = state.getDescription();
        strategy.inputValidationEnabled = Boolean.TRUE.equals(state.getInputValidationEnabled());
        strategy.outputValidationEnabled = Boolean.TRUE.equals(state.getOutputValidationEnabled());
        strategy.piiDetectionEnabled = Boolean.TRUE.equals(state.getPiiDetectionEnabled());
        strategy.piiMaskingEnabled = Boolean.TRUE.equals(state.getPiiMaskingEnabled());
        strategy.promptInjectionDetection = Boolean.TRUE.equals(state.getPromptInjectionDetection());
        strategy.maxInputLength = state.getMaxInputLength() != null ? state.getMaxInputLength() : 10000;
        strategy.maxOutputLength = state.getMaxOutputLength() != null ? state.getMaxOutputLength() : 4000;
        strategy.updatedAt = state.getUpdatedAt();
        return strategy;
    }

    /**
     * 验证用户输入。
     *
     * @param input 用户输入
     * @return 验证结果
     */
    public ValidationResult validateInput(String input) {
        List<String> violations = new ArrayList<>();
        if (input == null || input.isEmpty()) {
            return ValidationResult.pass();
        }
        validateInputLength(input, violations);
        validateHarmfulContent(input, violations);
        validatePii(input, violations);
        validatePromptInjection(input, violations);
        return toResult(violations);
    }

    /**
     * 验证模型输出。
     *
     * @param output 模型输出
     * @return 验证结果
     */
    public ValidationResult validateOutput(String output) {
        List<String> violations = new ArrayList<>();
        if (output == null || output.isEmpty()) {
            return ValidationResult.pass();
        }
        validateOutputLength(output, violations);
        validateHarmfulContent(output, violations);
        validatePii(output, violations);
        return toResult(violations);
    }

    /** 转换为验证结果。 */
    private ValidationResult toResult(List<String> violations) {
        if (violations.isEmpty()) return ValidationResult.pass();
        return ValidationResult.fail(violations);
    }

    /** 验证输入长度。 */
    private void validateInputLength(String input, List<String> violations) {
        if (!inputValidationEnabled) return;
        if (input.length() > maxInputLength) {
            violations.add("输入长度超过限制: " + maxInputLength);
        }
    }

    /** 验证输出长度。 */
    private void validateOutputLength(String output, List<String> violations) {
        if (!outputValidationEnabled) return;
        if (output.length() > maxOutputLength) {
            violations.add("输出长度超过限制: " + maxOutputLength);
        }
    }

    /** 验证有害内容。 */
    private void validateHarmfulContent(String text, List<String> violations) {
        if (!inputValidationEnabled) return;
        if (containsHarmfulContent(text)) {
            violations.add("内容包含有害信息");
        }
    }

    /** 验证 PII 信息。 */
    private void validatePii(String text, List<String> violations) {
        if (!piiDetectionEnabled) return;
        if (containsPii(text)) {
            violations.add("内容包含PII信息");
        }
    }

    /** 验证提示注入。 */
    private void validatePromptInjection(String text, List<String> violations) {
        if (!promptInjectionDetection) return;
        if (containsPromptInjection(text)) {
            violations.add("检测到Prompt注入攻击");
        }
    }

    /** 检查有害内容。 */
    private boolean containsHarmfulContent(String text) {
        return Stream.of("暴力", "色情", "赌博", "毒品")
                .anyMatch(text::contains);
    }

    /** 检查 PII 信息。 */
    private boolean containsPii(String text) {
        Pattern phonePattern = Pattern.compile("\\d{11}");
        Pattern emailPattern = Pattern.compile("\\w+@\\w+\\.\\w+");
        Pattern idCardPattern = Pattern.compile("\\d{17}[\\dXx]");
        return phonePattern.matcher(text).find()
                || emailPattern.matcher(text).find()
                || idCardPattern.matcher(text).find();
    }

    /** 检查提示注入。 */
    private boolean containsPromptInjection(String text) {
        return Stream.of("ignore previous instructions",
                        "disregard all above", "system:", "override:")
                .anyMatch(pattern -> text.toLowerCase().contains(pattern));
    }

    /**
     * 更新基本信息。
     *
     * @param name 策略名称
     * @param description 策略描述
     */
    public void updateBasicInfo(String name, String description) {
        this.name = name;
        this.description = description;
        this.updatedAt = Instant.now();
    }

    /** 启用输入验证。 */
    public void enableInputValidation() {
        this.inputValidationEnabled = true;
        this.updatedAt = Instant.now();
    }

    /** 禁用输入验证。 */
    public void disableInputValidation() {
        this.inputValidationEnabled = false;
        this.updatedAt = Instant.now();
    }

    /** 启用输出验证。 */
    public void enableOutputValidation() {
        this.outputValidationEnabled = true;
        this.updatedAt = Instant.now();
    }

    /** 禁用输出验证。 */
    public void disableOutputValidation() {
        this.outputValidationEnabled = false;
        this.updatedAt = Instant.now();
    }

    /**
     * 启用 PII 检测。
     *
     * @param masking 是否脱敏
     */
    public void enablePiiDetection(boolean masking) {
        this.piiDetectionEnabled = true;
        this.piiMaskingEnabled = masking;
        this.updatedAt = Instant.now();
    }

    /** 禁用 PII 检测。 */
    public void disablePiiDetection() {
        this.piiDetectionEnabled = false;
        this.piiMaskingEnabled = false;
        this.updatedAt = Instant.now();
    }

    /** 启用提示注入检测。 */
    public void enablePromptInjectionDetection() {
        this.promptInjectionDetection = true;
        this.updatedAt = Instant.now();
    }

    /** 禁用提示注入检测。 */
    public void disablePromptInjectionDetection() {
        this.promptInjectionDetection = false;
        this.updatedAt = Instant.now();
    }

    /**
     * 设置长度限制。
     *
     * @param maxInput 最大输入长度
     * @param maxOutput 最大输出长度
     */
    public void setLengthLimits(int maxInput, int maxOutput) {
        this.maxInputLength = maxInput;
        this.maxOutputLength = maxOutput;
        this.updatedAt = Instant.now();
    }
}
