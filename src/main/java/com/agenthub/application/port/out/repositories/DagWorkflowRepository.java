package com.agenthub.application.port.out.repositories;

import com.agenthub.domain.model.workflow.DagWorkflow;

import java.util.List;
import java.util.Optional;

/**
 * 工作流仓储接口，定义工作流的持久化操作。
 */
public interface DagWorkflowRepository {

    DagWorkflow save(DagWorkflow workflow);

    Optional<DagWorkflow> findById(String workflowId);

    List<DagWorkflow> findAll();

    List<DagWorkflow> findByTenantIdAndWorkspaceId(String tenantId, String workspaceId);

    void deleteById(String workflowId);
}
