package com.agenthub.infrastructure.workflow.processor.impl;

import cn.hutool.core.convert.Convert;
import com.agenthub.application.port.out.workflow.WorkflowExecutionPort;
import com.agenthub.application.port.out.workflow.WorkflowStatePort;
import com.agenthub.application.command.ExecutionCommand;
import com.agenthub.domain.enums.workflow.NodeType;
import com.agenthub.domain.model.workflow.WorkflowContext;
import com.agenthub.domain.model.workflow.WorkflowNode;
import com.agenthub.infrastructure.workflow.processor.AbstractNodeProcessor;
import com.agenthub.infrastructure.workflow.variable.VariableResolver;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * 子工作流节点处理器。
 * 使用 ObjectProvider 延迟获取 WorkflowExecutionPort 以避免循环依赖。
 *
 * @author huangdayu
 */
@Slf4j
@Component
public class SubWorkflowNodeProcessor extends AbstractNodeProcessor {

    private final ObjectProvider<WorkflowExecutionPort> executionPortProvider;
    private final WorkflowStatePort statePort;
    private final VariableResolver variableResolver;
    private final ObjectMapper objectMapper;

    public SubWorkflowNodeProcessor(
            ObjectProvider<WorkflowExecutionPort> executionPortProvider,
            WorkflowStatePort statePort,
            VariableResolver variableResolver,
            ObjectMapper objectMapper) {
        this.executionPortProvider = executionPortProvider;
        this.statePort = statePort;
        this.variableResolver = variableResolver;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getSupportedType() {
        return NodeType.SUB_WORKFLOW.name();
    }

    @Override
    protected Mono<Map<String, Object>> doProcess(WorkflowNode node, WorkflowContext context) {
        ExecutionCommand command = buildCommand(node, context);
        return executeSubWorkflow(command, node, context);
    }

    private ExecutionCommand buildCommand(WorkflowNode node, WorkflowContext context) {
        Map<String, Object> config = node.getConfig().getParameters();
        ExecutionCommand command = new ExecutionCommand();

        command.setWorkflowId(getSubWorkflowId(config));
        command.setTenantId(context.getTenantId());
        command.setWorkspaceId(context.getWorkspaceId());
        command.setInput(resolveInput(config, context));

        return command;
    }

    private String getSubWorkflowId(Map<String, Object> config) {
        return (String) config.getOrDefault("subWorkflowId", "");
    }

    private Map<String, Object> resolveInput(Map<String, Object> config, WorkflowContext context) {
        String mappingJson = (String) config.getOrDefault("inputMapping", "{}");
        Map<String, Object> mapping = parseMapping(mappingJson);
        return resolveVariables(mapping, context);
    }

    private Map<String, Object> parseMapping(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.warn("Failed to parse mapping JSON: {}, using empty map", json);
            return new HashMap<>();
        }
    }

    private Map<String, Object> resolveVariables(
            Map<String, Object> mapping, WorkflowContext context) {
        Map<String, Object> resolved = new HashMap<>();

        for (Map.Entry<String, Object> entry : mapping.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof String str) {
                resolved.put(entry.getKey(), variableResolver.resolveTemplateString(str, context));
            } else {
                resolved.put(entry.getKey(), value);
            }
        }

        return resolved;
    }

    private Mono<Map<String, Object>> executeSubWorkflow(
            ExecutionCommand command, WorkflowNode node, WorkflowContext context) {
        log.info("Executing sub-workflow: {}", command.getWorkflowId());

        // 延迟获取 executionPort
        WorkflowExecutionPort executionPort = executionPortProvider.getObject();
        
        int timeout = getTimeout(node);
        return executionPort.initializeContext(command)
            .timeout(java.time.Duration.ofSeconds(timeout))
            .flatMap(ctx -> executionPort.executeWorkflow(ctx)
                .then(statePort.loadContext(ctx.getExecutionId()))
                .map(opt -> opt.orElse(ctx)))
            .map(ctx -> processResult(ctx, command, node, context))
            .onErrorResume(error -> handleError(error));
    }

    private int getTimeout(WorkflowNode node) {
        Map<String, Object> config = node.getConfig().getParameters();
        Object timeout = config.getOrDefault("timeout", 300);
        return Convert.toInt(timeout);
    }

    private Map<String, Object> processResult(
            WorkflowContext ctx, ExecutionCommand command,
            WorkflowNode node, WorkflowContext context) {
        Map<String, Object> result = createBaseResult(ctx, command);
        Map<String, Object> mappedOutput = applyOutputMapping(ctx, node);

        saveToContext(mappedOutput, context);
        result.put("output", mappedOutput);

        log.info("Sub-workflow completed: {}", command.getWorkflowId());
        return result;
    }

    private Map<String, Object> createBaseResult(
            WorkflowContext ctx, ExecutionCommand command) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("executionId", ctx.getExecutionId());
        result.put("subWorkflowId", command.getWorkflowId());
        return result;
    }

    private Map<String, Object> applyOutputMapping(
            WorkflowContext ctx, WorkflowNode node) {
        Map<String, Object> config = node.getConfig().getParameters();
        String mappingJson = (String) config.getOrDefault("outputMapping", "{}");
        Map<String, Object> mapping = parseMapping(mappingJson);

        return extractValues(ctx.getVariables(), mapping);
    }

    private Map<String, Object> extractValues(
            Map<String, Object> data, Map<String, Object> mapping) {
        Map<String, Object> result = new HashMap<>();

        for (Map.Entry<String, Object> entry : mapping.entrySet()) {
            String path = (String) entry.getValue();
            result.put(entry.getKey(), extractByPath(data, path));
        }

        return result;
    }

    private Object extractByPath(Map<String, Object> data, String path) {
        if (path == null || path.isEmpty()) {
            return data;
        }

        Object current = data;
        for (String part : path.split("\\.")) {
            current = extractPart(current, part);
            if (current == null) return null;
        }

        return current;
    }

    private Object extractPart(Object current, String part) {
        if (current instanceof Map map) {
            return map.get(part);
        }
        return null;
    }

    private void saveToContext(Map<String, Object> output, WorkflowContext context) {
        for (Map.Entry<String, Object> entry : output.entrySet()) {
            context.setVariable(entry.getKey(), entry.getValue());
        }
    }

    private Mono<Map<String, Object>> handleError(Throwable error) {
        log.warn("Sub-workflow execution failed: {}", error.getMessage());
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("error", error.getMessage());
        return Mono.just(result);
    }
}
