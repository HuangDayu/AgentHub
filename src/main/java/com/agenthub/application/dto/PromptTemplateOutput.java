package com.agenthub.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromptTemplateOutput {

    private String id;
    private String tenantId;
    private String workspaceId;
    private String name;
    private String description;
    private String category;
    private String content;
    private List<VariableResult> variables;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    @Getter
    public enum Category {SYSTEM, USER, ASSISTANT, GENERAL}

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VariableResult {
        private String name;
        private String description;
        private String defaultValue;
        private boolean required;
    }
}
