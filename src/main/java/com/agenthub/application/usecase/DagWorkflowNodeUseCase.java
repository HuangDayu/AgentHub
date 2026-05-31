package com.agenthub.application.usecase;

import com.agenthub.application.command.AddNodeCommand;
import com.agenthub.application.dto.workflow.DagNodeOutput;
import com.agenthub.domain.enums.workflow.DagNodeType;
import com.agenthub.domain.model.workflow.NodeConfig;
import com.agenthub.domain.model.workflow.DagWorkflowNode;
import org.springframework.stereotype.Component;

import java.util.*;

@Component

public class DagWorkflowNodeUseCase {

    
    private final Map<String, List<DagWorkflowNode>> workflowNodes = new HashMap<>();

    public DagNodeOutput addNode(String workflowId, AddNodeCommand request) {
        DagWorkflowNode node = createNode(request);
        storeNode(workflowId, node);
        return toResponse(node);
    }

    public List<DagNodeOutput> listNodes(String workflowId) {
        List<DagWorkflowNode> nodes = workflowNodes.getOrDefault(workflowId, new ArrayList<>());
        return nodes.stream().map(this::toResponse).toList();
    }

    public DagNodeOutput getNode(String workflowId, String nodeId) {
        List<DagWorkflowNode> nodes = workflowNodes.get(workflowId);
        if (nodes == null) return null;
        return nodes.stream()
            .filter(n -> n.getId().equals(nodeId))
            .findFirst()
            .map(this::toResponse)
            .orElse(null);
    }

    public void deleteNode(String workflowId, String nodeId) {
        List<DagWorkflowNode> nodes = workflowNodes.get(workflowId);
        if (nodes != null) {
            nodes.removeIf(n -> n.getId().equals(nodeId));
        }
    }

    private DagWorkflowNode createNode(AddNodeCommand request) {
        DagNodeType type = DagNodeType.valueOf(request.getType());
        DagWorkflowNode node = DagWorkflowNode.create(type, request.getName());
        if (request.getConfig() != null) {
            node.setConfig(new NodeConfig(request.getConfig(), 30000, 0));
        }
        return node;
    }

    private void storeNode(String workflowId, DagWorkflowNode node) {
        workflowNodes.computeIfAbsent(workflowId, k -> new ArrayList<>()).add(node);
    }

    private DagNodeOutput toResponse(DagWorkflowNode node) {
        DagNodeOutput response = new DagNodeOutput();
        response.setId(node.getId());
        response.setType(node.getType().name());
        response.setName(node.getName());
        response.setStatus(node.getStatus().name());
        return response;
    }
}
