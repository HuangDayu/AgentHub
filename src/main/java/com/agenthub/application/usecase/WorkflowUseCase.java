package com.agenthub.application.usecase;

import com.agenthub.common.exception.NotFoundException;
import com.agenthub.application.dto.WorkflowOutput;
import com.agenthub.application.port.out.repositories.WorkflowRepository;
import com.agenthub.domain.model.Workflow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WorkflowUseCase {
    private final WorkflowRepository repository;

    public WorkflowOutput create(String tenantId, String workspaceId, String workflowCode,
                                 String name, String description, String graphDefinition) {
        Workflow workflow = Workflow.create(tenantId, workspaceId, workflowCode,
                name, description, graphDefinition);
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

    public WorkflowOutput update(String workflowId, String name, String description,
                                 String graphDefinition) {
        Workflow workflow = findById(workflowId);
        workflow.update(name, description, graphDefinition);
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
