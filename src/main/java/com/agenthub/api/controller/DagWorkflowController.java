package com.agenthub.api.controller;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.api.dto.CreateDagWorkflowRequest;
import com.agenthub.api.dto.UpdateDagWorkflowRequest;
import com.agenthub.api.dto.DagWorkflowResponse;
import com.agenthub.application.command.DagWorkflowCommand;
import com.agenthub.application.dto.DagWorkflowOutput;
import com.agenthub.application.usecase.DagWorkflowUseCase;
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
@RequestMapping("/api/v1/workspaces/{workspaceId}/dag-workflows")
@RequiredArgsConstructor
public class DagWorkflowController {

    private final DagWorkflowUseCase useCase;

    /**
     * 创建工作流。
     *
     * @param request 创建请求
     * @return 工作流响应
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DagWorkflowResponse create(@RequestBody CreateDagWorkflowRequest request) {
        DagWorkflowCommand cmd = BeanUtil.copyProperties(request, DagWorkflowCommand.class);
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
    public List<DagWorkflowResponse> list(@PathVariable String workspaceId,
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
    public DagWorkflowResponse get(@PathVariable String workflowId) {
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
    public DagWorkflowResponse update(@PathVariable String workflowId,
                                   @RequestBody UpdateDagWorkflowRequest request) {
        DagWorkflowCommand cmd = BeanUtil.copyProperties(request, DagWorkflowCommand.class);
        return toResponse(useCase.update(workflowId, cmd));
    }

    /**
     * 发布工作流。
     *
     * @param workflowId 工作流ID
     * @return 工作流响应
     */
    @PostMapping("/{workflowId}/publish")
    public DagWorkflowResponse publish(@PathVariable String workflowId) {
        return toResponse(useCase.publish(workflowId));
    }

    /**
     * 取消发布工作流。
     *
     * @param workflowId 工作流ID
     * @return 工作流响应
     */
    @PostMapping("/{workflowId}/unpublish")
    public DagWorkflowResponse unpublish(@PathVariable String workflowId) {
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
    private DagWorkflowResponse toResponse(DagWorkflowOutput result) {
        return BeanUtil.copyProperties(result, DagWorkflowResponse.class);
    }
}
