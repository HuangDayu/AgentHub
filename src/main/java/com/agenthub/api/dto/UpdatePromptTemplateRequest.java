package com.agenthub.api.dto;

import java.util.List;

public record UpdatePromptTemplateRequest(
        String name, String description, String category,
        String content, List<VariableDto> variables, Boolean isActive
) {
    public record VariableDto(String name, String description, String defaultValue, boolean required) {}
}
