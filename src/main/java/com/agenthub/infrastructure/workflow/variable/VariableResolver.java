package com.agenthub.infrastructure.workflow.variable;

import com.agenthub.domain.model.workflow.NodeResult;
import com.agenthub.domain.model.workflow.WorkflowContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 变量解析器。
 * 解析工作流中的变量引用和模板字符串。
 *
 * @author huangdayu
 */
@Component
public class VariableResolver {

    /** 变量引用模式：${xxx} */
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

    /**
     * 解析变量引用。
     *
     * @param expression 变量表达式
     * @param context 执行上下文
     * @return 解析后的值
     */
    public Object resolve(String expression, WorkflowContext context) {
        if (expression == null || !expression.contains("${")) {
            return expression;
        }
        return resolveVariableExpression(expression, context);
    }

    /**
     * 解析变量表达式。
     *
     * @param expression 表达式
     * @param context 执行上下文
     * @return 解析后的值
     */
    private Object resolveVariableExpression(String expression, WorkflowContext context) {
        Matcher matcher = VARIABLE_PATTERN.matcher(expression);
        if (!matcher.find()) {
            return expression;
        }
        return resolveSingleVariable(expression, context, matcher);
    }

    /**
     * 解析单个变量。
     *
     * @param expression 表达式
     * @param context 执行上下文
     * @param matcher 匹配器
     * @return 解析后的值
     */
    private Object resolveSingleVariable(String expression, 
                                         WorkflowContext context, 
                                         Matcher matcher) {
        matcher.reset();
        if (isSingleVariable(expression, matcher)) {
            return resolveVariableValue(expression.substring(2, expression.length() - 1), context);
        }
        return resolveTemplateString(expression, context);
    }

    /**
     * 判断是否为单个变量表达式。
     *
     * @param expression 表达式
     * @param matcher 匹配器
     * @return 如果是单个变量返回true
     */
    private boolean isSingleVariable(String expression, Matcher matcher) {
        return expression.startsWith("${") && expression.endsWith("}") 
            && expression.indexOf('$') == expression.lastIndexOf('$');
    }

    /**
     * 解析模板字符串。
     *
     * @param template 模板字符串
     * @param context 执行上下文
     * @return 解析后的字符串
     */
    public String resolveTemplateString(String template, WorkflowContext context) {
        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String variable = matcher.group(1);
            Object value = resolveVariableValue(variable, context);
            matcher.appendReplacement(result, String.valueOf(value));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * 解析变量值。
     *
     * @param variable 变量名
     * @param context 执行上下文
     * @return 变量值
     */
    private Object resolveVariableValue(String variable, WorkflowContext context) {
        String[] parts = variable.split("\\.", 2);
        String scope = parts[0];
        String path = parts.length > 1 ? parts[1] : "";
        
        return switch (scope) {
            case "user", "var" -> resolveUserVariable(path, context);
            case "sys" -> resolveSystemVariable(path, context);
            case "env" -> resolveEnvironmentVariable(path);
            case "node" -> resolveNodeVariableFromPath(path, context);
            default -> resolveNodeVariable(scope, path, context);
        };
    }

    /**
     * 解析用户变量。
     *
     * @param path 变量路径
     * @param context 执行上下文
     * @return 变量值
     */
    private Object resolveUserVariable(String path, WorkflowContext context) {
        return context.getVariable(path);
    }

    /**
     * 解析系统变量。
     *
     * @param path 变量路径
     * @param context 执行上下文
     * @return 变量值
     */
    private Object resolveSystemVariable(String path, WorkflowContext context) {
        return switch (path) {
            case "executionId" -> context.getExecutionId();
            case "workflowId" -> context.getWorkflowId();
            case "timestamp" -> System.currentTimeMillis();
            default -> null;
        };
    }

    /**
     * 解析环境变量。
     *
     * @param path 变量路径
     * @return 变量值
     */
    private Object resolveEnvironmentVariable(String path) {
        return System.getenv(path);
    }

    /**
     * 从路径解析节点输出变量（格式：nodeId.outputField）。
     *
     * @param path 变量路径（格式：nodeId.outputField）
     * @param context 执行上下文
     * @return 变量值
     */
    private Object resolveNodeVariableFromPath(String path, WorkflowContext context) {
        String[] parts = path.split("\\.", 2);
        if (parts.length < 2) {
            return resolveNodeVariable(parts[0], "", context);
        }
        return resolveNodeVariable(parts[0], parts[1], context);
    }

    /**
     * 解析节点输出变量。
     *
     * @param nodeId 节点ID
     * @param path 变量路径
     * @param context 执行上下文
     * @return 变量值
     */
    private Object resolveNodeVariable(String nodeId, String path, WorkflowContext context) {
        NodeResult result = context.getNodeResults().get(nodeId);
        if (result == null) {
            return null;
        }
        return resolveNodeOutput(result, path);
    }

    /**
     * 解析节点输出。
     *
     * @param result 节点结果
     * @param path 输出路径
     * @return 输出值
     */
    private Object resolveNodeOutput(NodeResult result, String path) {
        if (path.isEmpty()) {
            return result.getOutputs();
        }
        return result.getOutput(path);
    }

    /**
     * 解析Map中的所有变量。
     *
     * @param map 输入Map
     * @param context 执行上下文
     * @return 解析后的Map
     */
    public Map<String, Object> resolveMap(Map<String, Object> map, WorkflowContext context) {
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            result.put(entry.getKey(), resolveEntryValue(entry.getValue(), context));
        }
        return result;
    }

    /**
     * 解析条目值。
     *
     * @param value 值
     * @param context 执行上下文
     * @return 解析后的值
     */
    private Object resolveEntryValue(Object value, WorkflowContext context) {
        if (value instanceof String str) {
            return resolve(str, context);
        }
        return value;
    }
}
