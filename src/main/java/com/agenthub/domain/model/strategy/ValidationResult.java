package com.agenthub.domain.model.strategy;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 策略验证结果值对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResult {

    private boolean valid;
    private List<String> violations;

    /**
     * 创建验证通过结果。
     */
    public static ValidationResult pass() {
        return new ValidationResult(true, List.of());
    }

    /**
     * 创建验证失败结果。
     */
    public static ValidationResult fail(List<String> violations) {
        return new ValidationResult(false, violations);
    }
}
