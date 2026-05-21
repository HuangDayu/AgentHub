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
    private ScriptEngine scriptEngine;

    /**
     * 构造函数.
     */
    public ScriptNodeProcessor(VariableResolver variableResolver) {
        this.variableResolver = variableResolver;
        this.scriptEngine = initializeScriptEngine();
    }

    /**
     * 初始化脚本引擎.
     */
    private ScriptEngine initializeScriptEngine() {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("JavaScript");
        if (engine == null) {
            engine = manager.getEngineByName("graal.js");
        }
        if (engine == null) {
            log.warn("未找到JavaScript引擎，代码节点将返回空结果");
        }
        return engine;
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
            if (scriptEngine == null) {
                return handleEngineNotAvailable();
            }
            return executeScriptSafely(script, context);
        } catch (Exception e) {
            return handleScriptFailure(e);
        }
    }

    /**
     * 处理引擎不可用.
     */
    private Map<String, Object> handleEngineNotAvailable() {
        log.warn("JavaScript引擎不可用，返回空结果");
        Map<String, Object> output = new HashMap<>();
        output.put("result", null);
        output.put("success", true);
        return output;
    }

    /**
     * 安全执行脚本.
     */
    private Map<String, Object> executeScriptSafely(String script, WorkflowContext context) throws Exception {
        injectVariables(context);
        Object result = scriptEngine.eval(script);
        Map<String, Object> output = new HashMap<>();
        output.put("result", result);
        output.put("success", true);
        return output;
    }

    /**
     * 处理脚本执行失败.
     */
    private Map<String, Object> handleScriptFailure(Exception e) {
        log.warn("Script execution failed: {}", e.getMessage());
        Map<String, Object> errorOutput = new HashMap<>();
        errorOutput.put("error", e.getMessage());
        errorOutput.put("success", false);
        return errorOutput;
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
