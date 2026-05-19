package com.agenthub.infrastructure.workflow.processor.impl;

import com.agenthub.application.port.out.tools.ToolExecutionPort;
import com.agenthub.domain.enums.workflow.NodeType;
import com.agenthub.domain.model.workflow.*;
import com.agenthub.infrastructure.workflow.processor.AbstractNodeProcessor;
import com.agenthub.infrastructure.workflow.variable.VariableResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * 工具调用节点处理器.
 */
@Slf4j
@Component
public class ToolNodeProcessor extends AbstractNodeProcessor {

    private final ToolExecutionPort toolExecutionPort;
    private final VariableResolver variableResolver;

    /**
     * 构造函数.
     */
    public ToolNodeProcessor(ToolExecutionPort toolExecutionPort, VariableResolver variableResolver) {
        this.toolExecutionPort = toolExecutionPort;
        this.variableResolver = variableResolver;
    }

    /**
     * 执行工具调用.
     */
    @Override
    protected Mono<Map<String, Object>> doProcess(WorkflowNode node, WorkflowContext context) {
        return Mono.fromCallable(() -> {
            NodeConfig config = node.getConfig();
            String toolName = getToolName(config);
            Map<String, Object> parameters = resolveParameters(config, context);
            return executeTool(toolName, parameters);
        });
    }

    /**
     * 获取工具名称.
     */
    private String getToolName(NodeConfig config) {
        return (String) config.getParameters().getOrDefault("toolName", "");
    }

    /**
     * 解析工具参数.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> resolveParameters(NodeConfig config, WorkflowContext context) {
        Map<String, Object> paramsTemplate = (Map<String, Object>) config.getParameters().getOrDefault("parameters", Map.of());
        return variableResolver.resolveMap(paramsTemplate, context);
    }

    /**
     * 执行工具.
     */
    private Map<String, Object> executeTool(String toolName, Map<String, Object> parameters) {
        try {
            Object result = toolExecutionPort.execute(toolName, parameters);
            Map<String, Object> output = new HashMap<>();
            output.put("result", result);
            output.put("toolName", toolName);
            output.put("success", true);
            return output;
        } catch (Exception e) {
            log.error("Tool execution failed: {}", e.getMessage(), e);
            Map<String, Object> errorOutput = new HashMap<>();
            errorOutput.put("error", e.getMessage());
            errorOutput.put("toolName", toolName);
            errorOutput.put("success", false);
            return errorOutput;
        }
    }

    /**
     * 支持的节点类型.
     */
    @Override
    public String getSupportedType() {
        return NodeType.TOOL.name();
    }
}
