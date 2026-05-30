package com.agenthub.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromptTemplateInfo {

    private String id;
    private String tenantId;
    private String workspaceId;
    private String name;
    private String description;
    private Category category;
    private String content;
    private List<Variable> variables;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    @Getter
    public enum Category {SYSTEM, USER, ASSISTANT, GENERAL}

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Variable {
        private String name;
        private String description;
        private String defaultValue;
        private boolean required;
    }

}
