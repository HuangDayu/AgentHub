package com.agenthub.application.usecase;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.application.command.WorkflowCommand;
import com.agenthub.application.dto.WorkflowOutput;
import com.agenthub.application.port.out.repositories.WorkflowRepository;
import com.agenthub.domain.exception.NotFoundException;
import com.agenthub.domain.model.workflow.Workflow;
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
public class WorkflowUseCase {

    private final WorkflowRepository repository;

    /**
     * 创建工作流。
     *
     * @param command 工作流命令
     * @return 工作流输出
     */
    public WorkflowOutput create(WorkflowCommand command) {
        Workflow workflow = BeanUtil.copyProperties(command, Workflow.class);
        return toOutput(repository.save(workflow));
    }

    /**
     * 获取工作流详情。
     *
     * @param workflowId 工作流ID
     * @return 工作流输出
     */
    public WorkflowOutput get(String workflowId) {
        return toOutput(findById(workflowId));
    }

    /**
     * 获取所有工作流列表。
     *
     * @return 工作流列表
     */
    public List<WorkflowOutput> list() {
        return repository.findAll().stream().map(this::toOutput).toList();
    }

    /**
     * 按租户和工作空间获取工作流列表。
     *
     * @param tenantId 租户ID
     * @param workspaceId 工作空间ID
     * @return 工作流列表
     */
    public List<WorkflowOutput> listByTenantAndWorkspace(String tenantId, String workspaceId) {
        return repository.findByTenantIdAndWorkspaceId(tenantId, workspaceId)
                .stream().map(this::toOutput).toList();
    }

    /**
     * 更新工作流。
     *
     * @param workflowId 工作流ID
     * @param command 工作流命令
     * @return 工作流输出
     */
    public WorkflowOutput update(String workflowId, WorkflowCommand command) {
        Workflow workflow = BeanUtil.copyProperties(command, Workflow.class);
        workflow.setWorkspaceId(workflowId);
        return toOutput(repository.save(workflow));
    }

    /**
     * 发布工作流。
     *
     * @param workflowId 工作流ID
     * @return 工作流输出
     */
    public WorkflowOutput publish(String workflowId) {
        Workflow workflow = findById(workflowId);
        workflow.publish();
        return toOutput(repository.save(workflow));
    }

    /**
     * 取消发布工作流。
     *
     * @param workflowId 工作流ID
     * @return 工作流输出
     */
    public WorkflowOutput unpublish(String workflowId) {
        Workflow workflow = findById(workflowId);
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
    private Workflow findById(String workflowId) {
        return repository.findById(workflowId)
                .orElseThrow(() -> new NotFoundException("Workflow not found: " + workflowId));
    }

    /**
     * 转换为输出对象。
     *
     * @param workflow 工作流实体
     * @return 工作流输出
     */
    private WorkflowOutput toOutput(Workflow workflow) {
        return new WorkflowOutput(workflow.getId(), workflow.getTenantId(), workflow.getWorkspaceId(),
                workflow.getWorkflowCode(), workflow.getName(), workflow.getDescription(),
                workflow.getGraphDefinition(), workflow.getStatus(),
                workflow.getCreatedAt(), workflow.getUpdatedAt());
    }
}
