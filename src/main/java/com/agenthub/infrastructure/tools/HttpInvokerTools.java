package com.agenthub.infrastructure.tools;

import com.agenthub.application.command.InvokeToolCommand;
import com.agenthub.application.port.out.HttpToolInvoker;
import com.agenthub.application.port.out.repositories.ToolRepository;
import com.agenthub.infrastructure.tools.annotations.AgentTools;
import com.agenthub.common.exception.ToolNotFoundException;
import com.agenthub.domain.model.Tool;
import com.agenthub.domain.model.ToolId;
import com.agenthub.domain.model.ToolInvocationResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.http.*;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTTP 工具调用器适配器。
 * <p>
 * 通过 HTTP 协议调用外部工具服务，支持重试和超时配置。
 */
@AgentTools
@Primary
@Component
public class HttpInvokerTools implements HttpToolInvoker {
    private static final Logger log = LoggerFactory.getLogger(HttpInvokerTools.class);

    /**
     * URL 路径变量模式，如 {symbol}、{id}。
     */
    private static final Pattern URL_VARIABLE_PATTERN = Pattern.compile("\\{([^}]+)}");

    private static final TypeReference<Map<String, Object>> MAP_TYPE_REF = new TypeReference<>() {
    };

    private final RestTemplate restTemplate;
    private final RetryTemplate retryTemplate;
    private final ObjectMapper objectMapper;
    private final ToolRepository repository;

    /**
     * 构造函数，注入基础设施组件。
     */
    public HttpInvokerTools(@Qualifier("tool.restTemplate") RestTemplate restTemplate,
                            @Qualifier("tool.retryTemplate") RetryTemplate retryTemplate,
                            ObjectMapper objectMapper, ToolRepository repository) {
        this.restTemplate = restTemplate;
        this.retryTemplate = retryTemplate;
        this.objectMapper = objectMapper;
        this.repository = repository;
    }


    @org.springframework.ai.tool.annotation.Tool(description = "调用http端点")
    @Override
    public ToolInvocationResult invoke(String toolId, InvokeToolCommand command) {
        Tool tool = requireTool(toolId);
        Map<String, Object> payload = command.payload() == null ? Map.of() : command.payload();
        return invoke(tool, payload);
    }

    private Tool requireTool(String toolId) {
        return repository.findById(ToolId.of(toolId)).orElseThrow(() -> new ToolNotFoundException(toolId));
    }

    /**
     * 调用外部工具。
     */
    @Override
    public ToolInvocationResult invoke(Tool tool, Map<String, Object> payload) {
        log.info("调用外部工具: toolId={}, endpoint={}, method={}", tool.id().value(), tool.endpoint(), tool.httpMethod());
        try {
            return doInvoke(tool, payload);
        } catch (HttpClientErrorException e) {
            return handleClientError(tool, e);
        } catch (HttpServerErrorException e) {
            return handleServerError(tool, e);
        } catch (ResourceAccessException e) {
            return handleTimeout(tool, e);
        } catch (Exception e) {
            return handleGeneralError(tool, e);
        }
    }

    /**
     * 执行调用逻辑。
     */
    private ToolInvocationResult doInvoke(Tool tool, Map<String, Object> payload) {
        Map<String, Object> filteredPayload = validateAndFilter(tool, payload);
        String resolvedUrl = resolveUrlVariables(tool.endpoint(), filteredPayload);
        ResponseEntity<String> response = retryTemplate.execute(
                context -> doHttpRequest(resolvedUrl, tool.httpMethod(), filteredPayload)
        );
        log.info("外部工具调用成功: toolId={}, status={}", tool.id().value(), response.getStatusCode());
        return new ToolInvocationResult(tool.id().value(), "SUCCESS", parseResponse(response.getBody()));
    }

    /**
     * 处理客户端错误。
     */
    private ToolInvocationResult handleClientError(Tool tool, HttpClientErrorException e) {
        log.warn("外部工具返回客户端错误: toolId={}, status={}", tool.id().value(), e.getStatusCode());
        return new ToolInvocationResult(tool.id().value(), "FAILURE",
                Map.of("error", "CLIENT_ERROR:" + e.getStatusCode() + ":" + e.getResponseBodyAsString()));
    }

    /**
     * 处理服务端错误。
     */
    private ToolInvocationResult handleServerError(Tool tool, HttpServerErrorException e) {
        log.error("外部工具返回服务端错误: toolId={}, status={}", tool.id().value(), e.getStatusCode());
        return new ToolInvocationResult(tool.id().value(), "FAILURE",
                Map.of("error", "SERVER_ERROR:" + e.getStatusCode() + ":" + e.getResponseBodyAsString()));
    }

    /**
     * 处理超时错误。
     */
    private ToolInvocationResult handleTimeout(Tool tool, ResourceAccessException e) {
        log.error("外部工具连接超时: toolId={}", tool.id().value(), e);
        return new ToolInvocationResult(tool.id().value(), "FAILURE", Map.of("error", "TIMEOUT:" + e.getMessage()));
    }

    /**
     * 处理一般错误。
     */
    private ToolInvocationResult handleGeneralError(Tool tool, Exception e) {
        log.error("外部工具调用异常: toolId={}", tool.id().value(), e);
        return new ToolInvocationResult(tool.id().value(), "FAILURE", Map.of("error", "ERROR:" + e.getMessage()));
    }

    /**
     * 参数白名单校验：仅允许 inputSchema 中定义的属性通过。
     */
    private Map<String, Object> validateAndFilter(Tool tool, Map<String, Object> payload) {
        if (tool.inputSchemaJson() == null || tool.inputSchemaJson().isBlank()) {
            return payload;
        }
        try {
            return filterBySchema(tool, payload);
        } catch (JsonProcessingException e) {
            log.error("解析 inputSchema 失败: toolId={}", tool.id().value(), e);
            throw new IllegalArgumentException("inputSchema JSON 格式无效: " + e.getMessage());
        }
    }

    /**
     * 根据 schema 过滤参数。
     */
    private Map<String, Object> filterBySchema(Tool tool, Map<String, Object> payload) throws JsonProcessingException {
        JsonNode schemaNode = objectMapper.readTree(tool.inputSchemaJson());
        JsonNode propertiesNode = schemaNode.get("properties");
        if (propertiesNode == null || !propertiesNode.isObject()) {
            log.warn("inputSchema 缺少 properties 定义，跳过校验: toolId={}", tool.id().value());
            return payload;
        }
        Set<String> allowedKeys = getAllowedKeys(propertiesNode);
        validateRequiredFields(schemaNode, payload);
        return filterPayload(payload, allowedKeys);
    }

    /**
     * 获取允许的参数名。
     */
    private Set<String> getAllowedKeys(JsonNode propertiesNode) {
        Set<String> allowedKeys = new HashSet<>();
        propertiesNode.fieldNames().forEachRemaining(allowedKeys::add);
        return allowedKeys;
    }

    /**
     * 校验必填字段。
     */
    private void validateRequiredFields(JsonNode schemaNode, Map<String, Object> payload) {
        JsonNode requiredNode = schemaNode.get("required");
        if (requiredNode != null && requiredNode.isArray()) {
            for (JsonNode req : requiredNode) {
                String key = req.asText();
                if (!payload.containsKey(key)) {
                    throw new IllegalArgumentException("缺少必填参数: " + key);
                }
            }
        }
    }

    /**
     * 过滤参数。
     */
    private Map<String, Object> filterPayload(Map<String, Object> payload, Set<String> allowedKeys) {
        Map<String, Object> filtered = new HashMap<>();
        for (Map.Entry<String, Object> entry : payload.entrySet()) {
            if (!allowedKeys.contains(entry.getKey())) {
                throw new IllegalArgumentException("不允许的参数: " + entry.getKey() + "，允许的参数: " + allowedKeys);
            }
            filtered.put(entry.getKey(), entry.getValue());
        }
        return filtered;
    }

    /**
     * 解析 URL 路径变量。
     */
    private String resolveUrlVariables(String endpoint, Map<String, Object> payload) {
        Matcher matcher = URL_VARIABLE_PATTERN.matcher(endpoint);
        StringBuilder resolved = new StringBuilder();
        while (matcher.find()) {
            String varName = matcher.group(1);
            Object value = payload.get(varName);
            String replacement = value != null ? value.toString() : matcher.group();
            matcher.appendReplacement(resolved, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(resolved);
        return resolved.toString();
    }

    /**
     * 提取 URL 中定义的路径变量名。
     */
    private Set<String> extractUrlVariableNames(String endpoint) {
        Set<String> vars = new HashSet<>();
        Matcher matcher = URL_VARIABLE_PATTERN.matcher(endpoint);
        while (matcher.find()) {
            vars.add(matcher.group(1));
        }
        return vars;
    }

    /**
     * 执行实际的 HTTP 请求。
     */
    private ResponseEntity<String> doHttpRequest(String url, String method, Map<String, Object> payload) {
        HttpMethod httpMethod = HttpMethod.valueOf(method);
        HttpHeaders headers = createHeaders();
        if (httpMethod == HttpMethod.GET || httpMethod == HttpMethod.DELETE) {
            return executeGetOrDelete(url, httpMethod, payload, headers);
        }
        return executePostOrPut(url, httpMethod, payload, headers);
    }

    /**
     * 创建请求头。
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(MediaType.parseMediaTypes("application/json, */*"));
        return headers;
    }

    /**
     * 执行 GET 或 DELETE 请求。
     */
    private ResponseEntity<String> executeGetOrDelete(String url, HttpMethod httpMethod, Map<String, Object> payload, HttpHeaders headers) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
        Set<String> urlVars = extractUrlVariableNames(url);
        for (Map.Entry<String, Object> entry : payload.entrySet()) {
            if (!urlVars.contains(entry.getKey())) {
                builder.queryParam(entry.getKey(), entry.getValue());
            }
        }
        String finalUrl = builder.build().toUriString();
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(finalUrl, httpMethod, entity, String.class);
    }

    /**
     * 执行 POST 或 PUT 请求。
     */
    private ResponseEntity<String> executePostOrPut(String url, HttpMethod httpMethod, Map<String, Object> payload, HttpHeaders headers) {
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        return restTemplate.exchange(url, httpMethod, entity, String.class);
    }

    /**
     * 解析 HTTP 响应体为 Map。
     */
    private Map<String, Object> parseResponse(String body) {
        if (body == null || body.isBlank()) {
            return Map.of();
        }
        try {
            JsonNode node = objectMapper.readTree(body);
            if (node.isObject()) {
                return objectMapper.convertValue(node, MAP_TYPE_REF);
            }
            return Map.of("data", body);
        } catch (JsonProcessingException e) {
            return Map.of("data", body);
        }
    }
}
