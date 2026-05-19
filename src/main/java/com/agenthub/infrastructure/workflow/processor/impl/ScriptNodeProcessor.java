package com.agenthub.infrastructure.workflow.processor.impl;

import com.agenthub.domain.enums.workflow.NodeType;
import com.agenthub.domain.model.workflow.*;
import com.agenthub.infrastructure.workflow.processor.AbstractNodeProcessor;
import com.agenthub.infrastructure.workflow.variable.VariableResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.HashMap;
import java.util.Map;

/**
 * 代码执行节点处理器.
 */
@Slf4j
@Component
public class ScriptNodeProcessor extends AbstractNodeProcessor {

    private final VariableResolver variableResolver;
    private final ScriptEngine scriptEngine;

    /**
     * 构造函数.
     */
    public ScriptNodeProcessor(VariableResolver variableResolver) {
        this.variableResolver = variableResolver;
        ScriptEngineManager manager = new ScriptEngineManager();
        this.scriptEngine = manager.getEngineByName("JavaScript");
    }

    /**
     * 执行代码节点.
     */
    @Override
    protected Mono<Map<String, Object>> doProcess(WorkflowNode node, WorkflowContext context) {
        return Mono.fromCallable(() -> {
            NodeConfig config = node.getConfig();
            String script = getScript(config);
            return executeScript(script, context);
        });
    }

    /**
     * 获取脚本内容.
     */
    private String getScript(NodeConfig config) {
        return (String) config.getParameters().getOrDefault("script", "");
    }

    /**
     * 执行脚本.
     */
    private Map<String, Object> executeScript(String script, WorkflowContext context) {
        try {
            injectVariables(context);
            Object result = scriptEngine.eval(script);
            Map<String, Object> output = new HashMap<>();
            output.put("result", result);
            output.put("success", true);
            return output;
        } catch (Exception e) {
            log.error("Script execution failed: {}", e.getMessage(), e);
            Map<String, Object> errorOutput = new HashMap<>();
            errorOutput.put("error", e.getMessage());
            errorOutput.put("success", false);
            return errorOutput;
        }
    }

    /**
     * 注入变量到脚本上下文.
     */
    private void injectVariables(WorkflowContext context) {
        scriptEngine.getBindings(javax.script.ScriptContext.ENGINE_SCOPE).put("variables", context.getVariables());
    }

    /**
     * 支持的节点类型.
     */
    @Override
    public String getSupportedType() {
        return NodeType.CODE.name();
    }
}
