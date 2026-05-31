package com.agenthub.api.controller;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.api.dto.AddNodeRequest;
import com.agenthub.api.dto.NodeResponse;
import com.agenthub.application.command.AddNodeCommand;
import com.agenthub.application.dto.workflow.DagNodeOutput;
import com.agenthub.application.usecase.DagWorkflowNodeUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 工作流节点管理控制器。
 *
 * @author huangdayu
 */
@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/dag-workflows/{workflowId}/nodes")
@RequiredArgsConstructor
public class DagWorkflowNodeController {

    private final DagWorkflowNodeUseCase nodeUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NodeResponse addNode(@PathVariable String workflowId,
                                @RequestBody AddNodeRequest request) {
        DagNodeOutput nodeOutput = nodeUseCase.addNode(workflowId, BeanUtil.copyProperties(request, AddNodeCommand.class));
        return BeanUtil.copyProperties(nodeOutput, NodeResponse.class);
    }

    @GetMapping
    public List<NodeResponse> listNodes(@PathVariable String workflowId) {
        return nodeUseCase.listNodes(workflowId).stream()
                .map(nodeOutput -> BeanUtil.copyProperties(nodeOutput, NodeResponse.class))
                .toList();
    }

    @GetMapping("/{nodeId}")
    public NodeResponse getNode(@PathVariable String workflowId,
                                @PathVariable String nodeId) {
        DagNodeOutput nodeOutput = nodeUseCase.getNode(workflowId, nodeId);
        return BeanUtil.copyProperties(nodeOutput, NodeResponse.class);
    }

    @DeleteMapping("/{nodeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNode(@PathVariable String workflowId,
                           @PathVariable String nodeId) {
        nodeUseCase.deleteNode(workflowId, nodeId);
    }
}
