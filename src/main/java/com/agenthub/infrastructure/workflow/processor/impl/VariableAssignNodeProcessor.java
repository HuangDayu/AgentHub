package com.agenthub.infrastructure.workflow.processor.impl;

import com.agenthub.domain.enums.workflow.NodeType;
import com.agenthub.domain.model.workflow.WorkflowContext;
import com.agenthub.domain.model.workflow.WorkflowNode;
import com.agenthub.infrastructure.workflow.processor.AbstractNodeProcessor;
import com.agenthub.infrastructure.workflow.variable.VariableResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 变量赋值节点处理器。
 * 解析变量表达式并更新上下文变量。
 *
 * @author huangdayu
 */
@Component
@RequiredArgsConstructor
public class VariableAssignNodeProcessor extends AbstractNodeProcessor {

    private final VariableResolver variableResolver;

    /**
     * 获取支持的节点类型。
     *
     * @return VARIABLE类型
     */
    @Override
    public String getSupportedType() {
        return NodeType.VARIABLE.name();
    }

    /**
     * 执行变量赋值处理。
     *
     * @param node 工作流节点
     * @param context 执行上下文
     * @return 赋值结果的Mono
     */
    @Override
    protected Mono<Map<String, Object>> doProcess(
            WorkflowNode node, WorkflowContext context) {
        return Mono.fromSupplier(() -> assignVariables(node, context));
    }

    /**
     * 执行变量赋值。
     *
     * @param node 工作流节点
     * @param context 执行上下文
     * @return 赋值结果
     */
    private Map<String, Object> assignVariables(WorkflowNode node, WorkflowContext context) {
        List<VariableAssignment> assignments = parseAssignments(node);
        Map<String, Object> results = new HashMap<>();
        for (VariableAssignment assignment : assignments) {
            Object value = resolveValue(assignment, context);
            context.setVariable(assignment.name(), value);
            results.put(assignment.name(), value);
        }
        return results;
    }

    /**
     * 解析变量赋值配置。
     *
     * @param node 工作流节点
     * @return 赋值列表
     */
    @SuppressWarnings("unchecked")
    private List<VariableAssignment> parseAssignments(WorkflowNode node) {
        Map<String, Object> config = node.getConfig().getParameters();
        List<Map<String, Object>> assignmentConfigs = 
            (List<Map<String, Object>>) config.getOrDefault("assignments", List.of());
        return assignmentConfigs.stream()
            .map(this::createAssignment)
            .toList();
    }

    /**
     * 创建变量赋值。
     *
     * @param config 赋值配置
     * @return 变量赋值
     */
    private VariableAssignment createAssignment(Map<String, Object> config) {
        String name = (String) config.get("name");
        String expression = (String) config.get("expression");
        Object value = config.get("value");
        return new VariableAssignment(name, expression, value);
    }

    /**
     * 解析赋值。
     *
     * @param assignment 变量赋值
     * @param context 执行上下文
     * @return 解析后的值
     */
    private Object resolveValue(VariableAssignment assignment, WorkflowContext context) {
        if (assignment.expression() != null) {
            return variableResolver.resolve(assignment.expression(), context);
        }
        return assignment.value();
    }

    /**
     * 变量赋值记录。
     */
    private record VariableAssignment(String name, String expression, Object value) {}
}
