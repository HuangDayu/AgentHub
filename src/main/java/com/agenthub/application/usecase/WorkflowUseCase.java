package com.agenthub.application.usecase;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.application.command.WorkflowCommand;
import com.agenthub.application.dto.WorkflowOutput;
import com.agenthub.application.port.out.repositories.WorkflowRepository;
import com.agenthub.domain.exception.NotFoundException;
import com.agenthub.domain.model.Workflow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WorkflowUseCase {
    private final WorkflowRepository repository;

    public WorkflowOutput create(WorkflowCommand command) {
        Workflow workflow = BeanUtil.copyProperties(command, Workflow.class);
        return toOutput(repository.save(workflow));
    }

    public WorkflowOutput get(String workflowId) {
        return toOutput(findById(workflowId));
    }

    public List<WorkflowOutput> list() {
        return repository.findAll().stream().map(this::toOutput).toList();
    }

    public List<WorkflowOutput> listByTenantAndWorkspace(String tenantId, String workspaceId) {
        return repository.findByTenantIdAndWorkspaceId(tenantId, workspaceId)
                .stream().map(this::toOutput).toList();
    }

    public WorkflowOutput update(String workflowId, WorkflowCommand command) {
        Workflow workflow = BeanUtil.copyProperties(command, Workflow.class);
        workflow.setWorkspaceId(workflowId);
        return toOutput(repository.save(workflow));
    }

    public WorkflowOutput publish(String workflowId) {
        Workflow workflow = findById(workflowId);
        workflow.publish();
        return toOutput(repository.save(workflow));
    }

    public WorkflowOutput unpublish(String workflowId) {
        Workflow workflow = findById(workflowId);
        workflow.unpublish();
        return toOutput(repository.save(workflow));
    }

    public void delete(String workflowId) {
        findById(workflowId);
        repository.deleteById(workflowId);
    }

    private Workflow findById(String workflowId) {
        return repository.findById(workflowId)
                .orElseThrow(() -> new NotFoundException("Workflow not found: " + workflowId));
    }

    private WorkflowOutput toOutput(Workflow workflow) {
        return new WorkflowOutput(workflow.getId(), workflow.getTenantId(), workflow.getWorkspaceId(),
                workflow.getWorkflowCode(), workflow.getName(), workflow.getDescription(),
                workflow.getGraphDefinition(), workflow.getStatus(),
                workflow.getCreatedAt(), workflow.getUpdatedAt());
    }
}
