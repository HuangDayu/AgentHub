package com.agenthub.infrastructure.workflow.processor.impl;

import com.agenthub.domain.enums.workflow.NodeType;
import com.agenthub.domain.model.workflow.WorkflowContext;
import com.agenthub.domain.model.workflow.WorkflowEdge;
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
 * 条件判断节点处理器。
 * 计算条件表达式并选择执行分支。
 *
 * @author huangdayu
 */
@Component
@RequiredArgsConstructor
public class ConditionNodeProcessor extends AbstractNodeProcessor {

    private final VariableResolver variableResolver;

    /**
     * 获取支持的节点类型。
     *
     * @return CONDITION类型
     */
    @Override
    public String getSupportedType() {
        return NodeType.CONDITION.name();
    }

    /**
     * 执行条件判断处理。
     *
     * @param node 工作流节点
     * @param context 执行上下文
     * @return 判断结果的Mono
     */
    @Override
    protected Mono<Map<String, Object>> doProcess(
            WorkflowNode node, WorkflowContext context) {
        return Mono.fromSupplier(() -> evaluateConditions(node, context));
    }

    /**
     * 评估所有条件。
     *
     * @param node 工作流节点
     * @param context 执行上下文
     * @return 评估结果
     */
    private Map<String, Object> evaluateConditions(WorkflowNode node, WorkflowContext context) {
        List<ConditionBranch> branches = parseBranches(node);
        String selectedBranch = findMatchingBranch(branches, context);
        return buildResult(selectedBranch, branches);
    }

    /**
     * 解析条件分支。
     *
     * @param node 工作流节点
     * @return 分支列表
     */
    @SuppressWarnings("unchecked")
    private List<ConditionBranch> parseBranches(WorkflowNode node) {
        Map<String, Object> config = node.getConfig().getParameters();
        List<Map<String, Object>> branchConfigs = 
            (List<Map<String, Object>>) config.getOrDefault("branches", List.of());
        return branchConfigs.stream()
            .map(this::createBranch)
            .toList();
    }

    /**
     * 创建条件分支。
     *
     * @param config 分支配置
     * @return 条件分支
     */
    private ConditionBranch createBranch(Map<String, Object> config) {
        String name = (String) config.getOrDefault("name", "default");
        String expression = (String) config.getOrDefault("expression", "true");
        String targetNodeId = (String) config.get("targetNodeId");
        return new ConditionBranch(name, expression, targetNodeId);
    }

    /**
     * 查找匹配的分支。
     *
     * @param branches 分支列表
     * @param context 执行上下文
     * @return 匹配的分支名称
     */
    private String findMatchingBranch(List<ConditionBranch> branches, WorkflowContext context) {
        return branches.stream()
            .filter(branch -> evaluateExpression(branch.expression(), context))
            .findFirst()
            .map(ConditionBranch::name)
            .orElse("default");
    }

    /**
     * 评估条件表达式。
     *
     * @param expression 表达式
     * @param context 执行上下文
     * @return 评估结果
     */
    private boolean evaluateExpression(String expression, WorkflowContext context) {
        if (expression == null || expression.isBlank()) {
            return false;
        }
        Object result = variableResolver.resolve("${" + expression + "}", context);
        return Boolean.TRUE.equals(result);
    }

    /**
     * 构建输出结果。
     *
     * @param selectedBranch 选中的分支
     * @param branches 所有分支
     * @return 输出结果
     */
    private Map<String, Object> buildResult(String selectedBranch, List<ConditionBranch> branches) {
        Map<String, Object> output = new HashMap<>();
        output.put("selectedBranch", selectedBranch);
        output.put("branchCount", branches.size());
        output.put("evaluated", true);
        return output;
    }

    /**
     * 条件分支记录。
     */
    private record ConditionBranch(String name, String expression, String targetNodeId) {}
}
