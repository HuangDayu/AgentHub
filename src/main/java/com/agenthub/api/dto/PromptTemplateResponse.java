package com.agenthub.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromptTemplateResponse {

    private String id;
    private String tenantId;
    private String workspaceId;
    private String name;
    private String description;
    private String category;
    private String content;
    private List<VariableDto> variables;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    @Getter
    public enum Category {SYSTEM, USER, ASSISTANT, GENERAL}

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VariableDto {
        private String name;
        private String description;
        private String defaultValue;
        private boolean required;
    }
}