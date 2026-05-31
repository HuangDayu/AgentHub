package com.agenthub.infrastructure.workflow.processor.impl;

import com.agenthub.domain.enums.workflow.DagNodeType;
import com.agenthub.domain.model.workflow.NodeConfig;
import com.agenthub.domain.model.workflow.DagWorkflowContext;
import com.agenthub.domain.model.workflow.DagWorkflowNode;
import com.agenthub.infrastructure.workflow.processor.AbstractNodeProcessor;
import com.agenthub.infrastructure.workflow.variable.VariableResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * API调用节点处理器.
 */
@Slf4j
@Component
public class ApiNodeProcessor extends AbstractNodeProcessor {

    private final WebClient webClient;
    private final VariableResolver variableResolver;

    /**
     * 构造函数.
     */
    public ApiNodeProcessor(WebClient.Builder webClientBuilder, VariableResolver variableResolver) {
        this.webClient = webClientBuilder.build();
        this.variableResolver = variableResolver;
    }

    /**
     * 执行API调用.
     */
    @Override
    protected Mono<Map<String, Object>> doProcess(DagWorkflowNode node, DagWorkflowContext context) {
        return Mono.fromCallable(() -> {
            NodeConfig config = node.getConfig();
            String url = resolveUrl(config, context);
            String method = getMethod(config);
            return executeRequest(url, method, config, context);
        });
    }

    /**
     * 解析URL.
     */
    private String resolveUrl(NodeConfig config, DagWorkflowContext context) {
        String urlTemplate = (String) config.getParameters().getOrDefault("url", "");
        return variableResolver.resolveTemplateString(urlTemplate, context);
    }

    /**
     * 获取HTTP方法.
     */
    private String getMethod(NodeConfig config) {
        return (String) config.getParameters().getOrDefault("method", "GET");
    }

    /**
     * 执行HTTP请求.
     */
    private Map<String, Object> executeRequest(String url, String method, 
                                               NodeConfig config, DagWorkflowContext context) {
        try {
            return doExecuteRequest(url, method, config, context);
        } catch (Exception e) {
            return handleRequestFailure(url, e);
        }
    }

    /**
     * 执行具体的HTTP请求.
     */
    private Map<String, Object> doExecuteRequest(String url, String method, 
                                                  NodeConfig config, DagWorkflowContext context) {
        return switch (method.toUpperCase()) {
            case "GET" -> executeGet(url);
            case "POST" -> executePost(url, config, context);
            case "PUT" -> executePut(url, config, context);
            case "DELETE" -> executeDelete(url);
            default -> throw new IllegalArgumentException("不支持的HTTP方法: " + method);
        };
    }

    /**
     * 处理请求失败.
     */
    private Map<String, Object> handleRequestFailure(String url, Exception e) {
        log.warn("API调用失败: {} - {}", url, e.getMessage());
        Map<String, Object> errorResult = new HashMap<>();
        errorResult.put("error", e.getMessage());
        errorResult.put("success", false);
        return errorResult;
    }

    /**
     * 执行GET请求.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> executeGet(String url) {
        return webClient.get()
            .uri(url)
            .retrieve()
            .bodyToMono(Map.class)
            .timeout(Duration.ofMillis(30000))
            .block();
    }

    /**
     * 执行POST请求.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> executePost(String url, NodeConfig config, DagWorkflowContext context) {
        Map<String, Object> body = buildRequestBody(config, context);
        return webClient.post()
            .uri(url)
            .bodyValue(body)
            .retrieve()
            .bodyToMono(Map.class)
            .timeout(Duration.ofMillis(config.getTimeoutMs()))
            .block();
    }

    /**
     * 执行PUT请求.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> executePut(String url, NodeConfig config, DagWorkflowContext context) {
        Map<String, Object> body = buildRequestBody(config, context);
        return webClient.put()
            .uri(url)
            .bodyValue(body)
            .retrieve()
            .bodyToMono(Map.class)
            .timeout(Duration.ofMillis(config.getTimeoutMs()))
            .block();
    }

    /**
     * 执行DELETE请求.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> executeDelete(String url) {
        return webClient.delete()
            .uri(url)
            .retrieve()
            .bodyToMono(Map.class)
            .timeout(Duration.ofMillis(30000))
            .block();
    }

    /**
     * 构建请求体.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> buildRequestBody(NodeConfig config, DagWorkflowContext context) {
        Map<String, Object> bodyTemplate = (Map<String, Object>) config.getParameters().get("body");
        if (bodyTemplate == null) return new HashMap<>();
        return variableResolver.resolveMap(bodyTemplate, context);
    }

    /**
     * 支持的节点类型.
     */
    @Override
    public String getSupportedType() {
        return DagNodeType.API.name();
    }
}
