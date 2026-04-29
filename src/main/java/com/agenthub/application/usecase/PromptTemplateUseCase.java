package com.agenthub.application.usecase;

import com.agenthub.application.port.out.repositories.PromptTemplateRepository;
import com.agenthub.application.dto.PromptTemplateOutput;
import com.agenthub.application.dto.VariableOutput;
import com.agenthub.domain.model.PromptTemplate;
import com.agenthub.common.exception.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PromptTemplateUseCase {
    private final PromptTemplateRepository repository;

    public PromptTemplateUseCase(PromptTemplateRepository repository) {
        this.repository = repository;
    }

    public PromptTemplateOutput create(String workspaceId, String tenantId, String name, String description,
                                       String category, String content, List<VariableOutput> variables,
                                       Boolean isActive) {
        List<PromptTemplate.Variable> vars = toVariables(variables);
        PromptTemplate template = PromptTemplate.create(null, tenantId, workspaceId, name, description,
                category, content, vars, isActive != null ? isActive : true);
        return toResult(repository.save(template));
    }

    public List<PromptTemplateOutput> list(String workspaceId) {
        return repository.findByWorkspaceId(workspaceId).stream().map(this::toResult).toList();
    }

    public List<PromptTemplateOutput> listByCategory(String workspaceId, String category) {
        return repository.findByWorkspaceIdAndCategory(workspaceId, category).stream().map(this::toResult).toList();
    }

    public PromptTemplateOutput get(String id) {
        return toResult(repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Prompt Template not found: " + id)));
    }

    public PromptTemplateOutput update(String id, String name, String description, String category,
                                       String content, List<VariableOutput> variables, Boolean isActive) {
        List<PromptTemplate.Variable> vars = toVariables(variables);
        PromptTemplate existing = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Prompt Template not found: " + id));
        PromptTemplate updated = existing.patch(name, description, category, content, vars, isActive);
        return toResult(repository.update(updated));
    }

    public void delete(String id) {
        repository.deleteById(id);
    }

    private List<PromptTemplate.Variable> toVariables(List<VariableOutput> results) {
        if (results == null) return List.of();
        return results.stream().map(v -> new PromptTemplate.Variable(v.name(), v.description(), v.defaultValue(), v.required())).toList();
    }

    private PromptTemplateOutput toResult(PromptTemplate template) {
        List<PromptTemplateOutput.VariableResult> vars = template.variables() != null
                ? template.variables().stream().map(v -> new PromptTemplateOutput.VariableResult(v.name(), v.description(), v.defaultValue(), v.required())).toList()
                : List.of();
        return new PromptTemplateOutput(template.id(), template.name(), template.description(),
                template.category(), template.content(), vars, template.isActive(),
                template.createdAt(), template.updatedAt());
    }
}
