package com.agenthub.domain.model;

import java.time.Instant;
import java.util.List;

public record PromptTemplate(
        String id, String tenantId, String workspaceId,
        String name, String description, String category,
        String content, List<Variable> variables,
        boolean isActive, Instant createdAt, Instant updatedAt
) {
    public enum Category { SYSTEM, USER, ASSISTANT, GENERAL }

    public record Variable(String name, String description, String defaultValue, boolean required) {}

    public static PromptTemplate create(String id, String tenantId, String workspaceId,
                                        String name, String description, String category,
                                        String content, List<Variable> variables, boolean isActive) {
        Instant now = Instant.now();
        return new PromptTemplate(id, tenantId, workspaceId, name, description,
                category, content, variables, isActive, now, now);
    }

    public PromptTemplate patch(String name, String description, String category,
                                String content, List<Variable> variables, Boolean isActive) {
        return new PromptTemplate(this.id, this.tenantId, this.workspaceId,
                name != null ? name : this.name,
                description != null ? description : this.description,
                category != null ? category : this.category,
                content != null ? content : this.content,
                variables != null ? variables : this.variables,
                isActive != null ? isActive : this.isActive,
                this.createdAt, Instant.now());
    }
}
