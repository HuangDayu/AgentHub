package com.agenthub.application.usecase;

import com.agenthub.application.command.AddNodeCommand;
import com.agenthub.application.dto.workflow.NodeOutput;
import com.agenthub.domain.enums.workflow.NodeType;
import com.agenthub.domain.model.workflow.NodeConfig;
import com.agenthub.domain.model.workflow.WorkflowNode;
import org.springframework.stereotype.Component;

import java.util.*;

@Component

public class WorkflowNodeUseCase {

    
    private final Map<String, List<WorkflowNode>> workflowNodes = new HashMap<>();

    public NodeOutput addNode(String workflowId, AddNodeCommand request) {
        WorkflowNode node = createNode(request);
        storeNode(workflowId, node);
        return toResponse(node);
    }

    public List<NodeOutput> listNodes(String workflowId) {
        List<WorkflowNode> nodes = workflowNodes.getOrDefault(workflowId, new ArrayList<>());
        return nodes.stream().map(this::toResponse).toList();
    }

    public NodeOutput getNode(String workflowId, String nodeId) {
        List<WorkflowNode> nodes = workflowNodes.get(workflowId);
        if (nodes == null) return null;
        return nodes.stream()
            .filter(n -> n.getId().equals(nodeId))
            .findFirst()
            .map(this::toResponse)
            .orElse(null);
    }

    public void deleteNode(String workflowId, String nodeId) {
        List<WorkflowNode> nodes = workflowNodes.get(workflowId);
        if (nodes != null) {
            nodes.removeIf(n -> n.getId().equals(nodeId));
        }
    }

    private WorkflowNode createNode(AddNodeCommand request) {
        NodeType type = NodeType.valueOf(request.getType());
        WorkflowNode node = WorkflowNode.create(type, request.getName());
        if (request.getConfig() != null) {
            node.setConfig(new NodeConfig(request.getConfig(), 30000, 0));
        }
        return node;
    }

    private void storeNode(String workflowId, WorkflowNode node) {
        workflowNodes.computeIfAbsent(workflowId, k -> new ArrayList<>()).add(node);
    }

    private NodeOutput toResponse(WorkflowNode node) {
        NodeOutput response = new NodeOutput();
        response.setId(node.getId());
        response.setType(node.getType().name());
        response.setName(node.getName());
        response.setStatus(node.getStatus().name());
        return response;
    }
}
