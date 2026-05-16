package com.agenthub.application.executor;

import com.agenthub.application.dto.ValidationOutput;
import com.agenthub.domain.model.GuardrailStrategy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * 护栏策略执行器 - 执行输入输出验证
 */
@Component
public class GuardrailStrategyExecutor {

    public ValidationOutput validateInput(GuardrailStrategy strategy, String input) {
        List<String> violations = new ArrayList<>();
        if (input == null || input.isEmpty()) {
            return valid();
        }
        validateInputLength(strategy, input, violations);
        validateHarmfulContent(strategy, input, violations);
        validatePii(strategy, input, violations);
        validatePromptInjection(strategy, input, violations);
        return new ValidationOutput(violations.isEmpty(), violations);
    }

    public ValidationOutput validateOutput(GuardrailStrategy strategy, String output) {
        List<String> violations = new ArrayList<>();
        if (output == null || output.isEmpty()) {
            return valid();
        }
        validateOutputLength(strategy, output, violations);
        validateHarmfulContent(strategy, output, violations);
        validatePii(strategy, output, violations);
        return new ValidationOutput(violations.isEmpty(), violations);
    }

    private void validateInputLength(GuardrailStrategy s, String input, List<String> violations) {
        if (!s.isInputValidationEnabled()) return;
        if (input.length() > s.getMaxInputLength()) {
            violations.add("输入长度超过限制: " + s.getMaxInputLength());
        }
    }

    private void validateOutputLength(GuardrailStrategy s, String output, List<String> violations) {
        if (!s.isOutputValidationEnabled()) return;
        if (output.length() > s.getMaxOutputLength()) {
            violations.add("输出长度超过限制: " + s.getMaxOutputLength());
        }
    }

    private void validateHarmfulContent(GuardrailStrategy s, String text, List<String> violations) {
        if (!s.isInputValidationEnabled()) return;
        if (containsHarmfulContent(text)) {
            violations.add("内容包含有害信息");
        }
    }

    private void validatePii(GuardrailStrategy s, String text, List<String> violations) {
        if (!s.isPiiDetectionEnabled()) return;
        if (containsPii(text)) {
            violations.add("内容包含PII信息");
        }
    }

    private void validatePromptInjection(GuardrailStrategy s, String text, List<String> violations) {
        if (!s.isPromptInjectionDetection()) return;
        if (containsPromptInjection(text)) {
            violations.add("检测到Prompt注入攻击");
        }
    }

    private boolean containsHarmfulContent(String text) {
        return Stream.of("暴力", "色情", "赌博", "毒品").anyMatch(text::contains);
    }

    private boolean containsPii(String text) {
        Pattern phonePattern = Pattern.compile("\\d{11}");
        Pattern emailPattern = Pattern.compile("\\w+@\\w+\\.\\w+");
        Pattern idCardPattern = Pattern.compile("\\d{17}[\\dXx]");
        return phonePattern.matcher(text).find()
                || emailPattern.matcher(text).find()
                || idCardPattern.matcher(text).find();
    }

    private boolean containsPromptInjection(String text) {
        return Stream.of("ignore previous instructions", "disregard all above", "system:", "override:")
                .anyMatch(pattern -> text.toLowerCase().contains(pattern));
    }

    public static ValidationOutput valid() {
        return new ValidationOutput(true, List.of());
    }


}
