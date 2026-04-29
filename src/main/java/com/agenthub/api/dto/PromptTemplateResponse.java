package com.agenthub.api.dto;

import java.time.Instant;
import java.util.List;

public record PromptTemplateResponse(
        String id, String name, String description, String category,
        String content, List<VariableDto> variables,
        boolean isActive, Instant createdAt, Instant updatedAt
) {
    public record VariableDto(String name, String description, String defaultValue, boolean required) {}
}
