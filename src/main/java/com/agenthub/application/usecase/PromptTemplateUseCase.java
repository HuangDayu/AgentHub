package com.agenthub.application.usecase;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.application.command.PromptTemplateInfoCommand;
import com.agenthub.application.dto.PromptTemplateOutput;
import com.agenthub.application.dto.VariableOutput;
import com.agenthub.application.port.out.repositories.PromptTemplateRepository;
import com.agenthub.domain.exception.NotFoundException;
import com.agenthub.domain.model.PromptTemplateInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PromptTemplateUseCase {
    private final PromptTemplateRepository repository;

    public PromptTemplateOutput create(PromptTemplateInfoCommand command) {
        List<PromptTemplateInfo.Variable> vars = toVariables(command.getVariables());
        PromptTemplateInfo promptTemplateInfo = BeanUtil.copyProperties(command, PromptTemplateInfo.class);
        promptTemplateInfo.setVariables(vars);
        return toResult(repository.save(promptTemplateInfo));
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

    public PromptTemplateOutput update(String id, PromptTemplateInfoCommand command) {
        List<PromptTemplateInfo.Variable> vars = toVariables(command.getVariables());
        PromptTemplateInfo promptTemplateInfo = BeanUtil.copyProperties(command, PromptTemplateInfo.class);
        promptTemplateInfo.setId(id);
        promptTemplateInfo.setVariables(vars);
        return toResult(repository.update(promptTemplateInfo));
    }

    public void delete(String id) {
        repository.deleteById(id);
    }

    private List<PromptTemplateInfo.Variable> toVariables(List<VariableOutput> results) {
        if (results == null) return List.of();
        return results.stream().map(v -> BeanUtil.copyProperties(v, PromptTemplateInfo.Variable.class)).toList();
    }

    private PromptTemplateOutput toResult(PromptTemplateInfo template) {
        List<PromptTemplateOutput.VariableResult> vars = template.getVariables() != null
                ? template.getVariables().stream().map(v -> BeanUtil.copyProperties(v,PromptTemplateOutput.VariableResult.class)).toList()
                : List.of();
        PromptTemplateOutput promptTemplateOutput = BeanUtil.copyProperties(template, PromptTemplateOutput.class);
        promptTemplateOutput.setVariables(vars);
        return promptTemplateOutput;
    }
}
