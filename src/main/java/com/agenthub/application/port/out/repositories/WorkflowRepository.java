package com.agenthub.application.port.out.repositories;

import com.agenthub.domain.model.Workflow;

import java.util.List;
import java.util.Optional;

/**
 * 工作流仓储接口，定义工作流的持久化操作。
 */
public interface WorkflowRepository {

    Workflow save(Workflow workflow);

    Optional<Workflow> findById(String workflowId);

    List<Workflow> findAll();

    List<Workflow> findByTenantIdAndWorkspaceId(String tenantId, String workspaceId);

    void deleteById(String workflowId);
}
