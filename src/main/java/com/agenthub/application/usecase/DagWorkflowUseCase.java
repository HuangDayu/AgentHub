package com.agenthub.application.usecase;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.application.command.DagWorkflowCommand;
import com.agenthub.application.dto.DagWorkflowOutput;
import com.agenthub.application.port.out.repositories.DagWorkflowRepository;
import com.agenthub.domain.exception.NotFoundException;
import com.agenthub.domain.model.workflow.DagWorkflow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 工作流管理用例。
 * 负责工作流的创建、更新、发布和删除。
 *
 * @author huangdayu
 */
@Component
@RequiredArgsConstructor
public class DagWorkflowUseCase {

    private final DagWorkflowRepository repository;

    /**
     * 创建工作流。
     *
     * @param command 工作流命令
     * @return 工作流输出
     */
    public DagWorkflowOutput create(DagWorkflowCommand command) {
        DagWorkflow workflow = BeanUtil.copyProperties(command, DagWorkflow.class);
        return toOutput(repository.save(workflow));
    }

    /**
     * 获取工作流详情。
     *
     * @param workflowId 工作流ID
     * @return 工作流输出
     */
    public DagWorkflowOutput get(String workflowId) {
        return toOutput(findById(workflowId));
    }

    /**
     * 按租户和工作空间获取工作流列表。
     *
     * @param tenantId 租户ID
     * @param workspaceId 工作空间ID
     * @return 工作流列表
     */
    public List<DagWorkflowOutput> list(String tenantId, String workspaceId) {
        return repository.findByTenantIdAndWorkspaceId(tenantId, workspaceId)
                .stream().map(this::toOutput).toList();
    }

    /**
     * 按租户和工作空间获取工作流列表。
     *
     * @param tenantId 租户ID
     * @param workspaceId 工作空间ID
     * @return 工作流列表
     */
    public List<DagWorkflowOutput> listByTenantAndWorkspace(String tenantId, String workspaceId) {
        return repository.findByTenantIdAndWorkspaceId(tenantId, workspaceId)
                .stream().map(this::toOutput).toList();
    }

    /**
     * 更新工作流。
     *
     * @param workflowId 工作流ID
     * @param command 工作流命令（仅携带需要更新的非空字段）
     * @return 工作流输出
     */
    public DagWorkflowOutput update(String workflowId, DagWorkflowCommand command) {
        DagWorkflow existing = findById(workflowId);
        // 仅更新允许修改的字段
        if (command.getName() != null) existing.setName(command.getName());
        if (command.getDescription() != null) existing.setDescription(command.getDescription());
        if (command.getGraphDefinition() != null) existing.setGraphDefinition(command.getGraphDefinition());
        if (command.getWorkflowCode() != null) existing.setWorkflowCode(command.getWorkflowCode());
        return toOutput(repository.save(existing));
    }

    /**
     * 发布工作流。
     *
     * @param workflowId 工作流ID
     * @return 工作流输出
     */
    public DagWorkflowOutput publish(String workflowId) {
        DagWorkflow workflow = findById(workflowId);
        workflow.publish();
        return toOutput(repository.save(workflow));
    }

    /**
     * 取消发布工作流。
     *
     * @param workflowId 工作流ID
     * @return 工作流输出
     */
    public DagWorkflowOutput unpublish(String workflowId) {
        DagWorkflow workflow = findById(workflowId);
        workflow.unpublish();
        return toOutput(repository.save(workflow));
    }

    /**
     * 删除工作流。
     *
     * @param workflowId 工作流ID
     */
    public void delete(String workflowId) {
        findById(workflowId);
        repository.deleteById(workflowId);
    }

    /**
     * 根据ID查找工作流。
     *
     * @param workflowId 工作流ID
     * @return 工作流实体
     */
    private DagWorkflow findById(String workflowId) {
        return repository.findById(workflowId)
                .orElseThrow(() -> new NotFoundException("Workflow not found: " + workflowId));
    }

    /**
     * 转换为输出对象。
     *
     * @param workflow 工作流实体
     * @return 工作流输出
     */
    private DagWorkflowOutput toOutput(DagWorkflow workflow) {
        return new DagWorkflowOutput(workflow.getId(), workflow.getTenantId(), workflow.getWorkspaceId(),
                workflow.getWorkflowCode(), workflow.getName(), workflow.getDescription(),
                workflow.getGraphDefinition(), workflow.getStatus(),
                workflow.getCreatedAt(), workflow.getUpdatedAt());
    }
}
