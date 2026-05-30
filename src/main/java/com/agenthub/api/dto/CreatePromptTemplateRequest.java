package com.agenthub.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePromptTemplateRequest {
    private String name;
    private String description;
    private String category;
    private String content;
    private List<VariableDto> variables;
    private Boolean active;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VariableDto {
        private String name;
        private String description;
        private String defaultValue;
        private boolean required;
    }
}
