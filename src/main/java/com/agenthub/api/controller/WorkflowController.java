package com.agenthub.api.controller;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.api.dto.CreateWorkflowRequest;
import com.agenthub.api.dto.UpdateWorkflowRequest;
import com.agenthub.api.dto.WorkflowResponse;
import com.agenthub.application.command.WorkflowCommand;
import com.agenthub.application.dto.WorkflowOutput;
import com.agenthub.application.usecase.WorkflowUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 工作流管理控制器。
 * 提供工作流的CRUD和发布管理API。
 *
 * @author huangdayu
 */
@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/workflows")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowUseCase useCase;

    /**
     * 创建工作流。
     *
     * @param request 创建请求
     * @return 工作流响应
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkflowResponse create(@RequestBody CreateWorkflowRequest request) {
        WorkflowCommand cmd = BeanUtil.copyProperties(request, WorkflowCommand.class);
        return toResponse(useCase.create(cmd));
    }

    /**
     * 获取工作流列表（按工作空间过滤）。
     *
     * @param workspaceId 工作空间ID
     * @param tenantId 租户ID
     * @return 工作流列表
     */
    @GetMapping
    public List<WorkflowResponse> list(@PathVariable String workspaceId,
                                        @RequestHeader("X-Tenant-Id") String tenantId) {
        return useCase.list(tenantId, workspaceId).stream().map(this::toResponse).toList();
    }

    /**
     * 获取工作流详情。
     *
     * @param workflowId 工作流ID
     * @return 工作流响应
     */
    @GetMapping("/{workflowId}")
    public WorkflowResponse get(@PathVariable String workflowId) {
        return toResponse(useCase.get(workflowId));
    }

    /**
     * 更新工作流。
     *
     * @param workflowId 工作流ID
     * @param request 更新请求
     * @return 工作流响应
     */
    @PutMapping("/{workflowId}")
    public WorkflowResponse update(@PathVariable String workflowId,
                                   @RequestBody UpdateWorkflowRequest request) {
        WorkflowCommand cmd = BeanUtil.copyProperties(request, WorkflowCommand.class);
        return toResponse(useCase.update(workflowId, cmd));
    }

    /**
     * 发布工作流。
     *
     * @param workflowId 工作流ID
     * @return 工作流响应
     */
    @PostMapping("/{workflowId}/publish")
    public WorkflowResponse publish(@PathVariable String workflowId) {
        return toResponse(useCase.publish(workflowId));
    }

    /**
     * 取消发布工作流。
     *
     * @param workflowId 工作流ID
     * @return 工作流响应
     */
    @PostMapping("/{workflowId}/unpublish")
    public WorkflowResponse unpublish(@PathVariable String workflowId) {
        return toResponse(useCase.unpublish(workflowId));
    }

    /**
     * 删除工作流。
     *
     * @param workflowId 工作流ID
     */
    @DeleteMapping("/{workflowId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String workflowId) {
        useCase.delete(workflowId);
    }

    /**
     * 转换为响应DTO。
     *
     * @param result 输出对象
     * @return 响应DTO
     */
    private WorkflowResponse toResponse(WorkflowOutput result) {
        return BeanUtil.copyProperties(result, WorkflowResponse.class);
    }
}
