package com.agenthub.infrastructure.tools.system_tools.base_tools;

import com.agenthub.infrastructure.tools.system_tools.annotations.AgentTools;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author huangdayu
 */
@RequiredArgsConstructor
@AgentTools(name = "NodeTools", description = "节点工具，提供节点管理功能")
public class NodeTools {

    private final Map<String, Map<String, Object>> nodes = new HashMap<>();

    @Tool(description = "获取或管理节点信息")
    public Map<String, Object> nodes(@ToolParam String action, @ToolParam String nodeId) {
        return switch (action.toLowerCase()) {
            case "list" -> listNodes();
            case "get" -> getNodeInfo(nodeId);
            case "status" -> getNodeStatus(nodeId);
            default -> Map.of("error", "未知操作: " + action);
        };
    }

    private Map<String, Object> listNodes() {
        Map<String, Object> result = new HashMap<>();
        result.put("nodes", new ArrayList<>(nodes.keySet()));
        return result;
    }

    private Map<String, Object> getNodeInfo(String nodeId) {
        return nodes.getOrDefault(nodeId, new HashMap<>());
    }

    private Map<String, Object> getNodeStatus(String nodeId) {
        Map<String, Object> status = new HashMap<>();
        status.put("nodeId", nodeId);
        status.put("status", "running");
        return status;
    }
}
