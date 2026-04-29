package com.agenthub.application.dto;

import java.time.Instant;
import java.util.List;

public record PromptTemplateOutput(
        String id, String name, String description, String category,
        String content, List<VariableResult> variables,
        boolean isActive, Instant createdAt, Instant updatedAt
) {
    public record VariableResult(String name, String description, String defaultValue, boolean required) {}
}
