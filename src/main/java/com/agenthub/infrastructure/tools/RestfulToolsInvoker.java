package com.agenthub.infrastructure.tools;

import com.agenthub.application.command.InvokeToolCommand;
import com.agenthub.application.port.out.repositories.HttpToolRepository;
import com.agenthub.domain.exception.ToolNotFoundException;
import com.agenthub.domain.model.tools.HttpTool;
import com.agenthub.domain.model.tools.HttpToolInvokeResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
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
@Component
public class RestfulToolsInvoker {
    private static final Logger log = LoggerFactory.getLogger(RestfulToolsInvoker.class);

    /**
     * URL 路径变量模式，如 {symbol}、{id}。
     */
    private static final Pattern URL_VARIABLE_PATTERN = Pattern.compile("\\{([^}]+)}");

    private static final TypeReference<Map<java.lang.String, Object>> MAP_TYPE_REF = new TypeReference<>() {
    };

    private final RestTemplate restTemplate;
    private final RetryTemplate retryTemplate;
    private final ObjectMapper objectMapper;
    private final HttpToolRepository repository;

    /**
     * 构造函数，注入基础设施组件。
     */
    public RestfulToolsInvoker(@Qualifier("restfulToolsRestTemplate") RestTemplate restTemplate,
                               @Qualifier("restfulToolsRetryTemplate") RetryTemplate retryTemplate,
                               ObjectMapper objectMapper, HttpToolRepository repository) {
        this.restTemplate = restTemplate;
        this.retryTemplate = retryTemplate;
        this.objectMapper = objectMapper;
        this.repository = repository;
    }


    public HttpToolInvokeResult invoke(String toolId, InvokeToolCommand command) {
        HttpTool httpTool = requireTool(toolId);
        Map<java.lang.String, Object> payload = command.getPayload() == null ? Map.of() : command.getPayload();
        return invoke(httpTool, payload);
    }

    private HttpTool requireTool(String toolId) {
        return repository.findById(toolId).orElseThrow(() -> new ToolNotFoundException(toolId));
    }

    /**
     * 调用外部工具。
     */
    public HttpToolInvokeResult invoke(HttpTool httpTool, Map<java.lang.String, Object> payload) {
        try {
            return doInvoke(httpTool, payload);
        } catch (HttpClientErrorException e) { return handleClientError(httpTool, e); }
        catch (HttpServerErrorException e) { return handleServerError(httpTool, e); }
        catch (ResourceAccessException e) { return handleTimeout(httpTool, e); }
        catch (Exception e) { return handleGeneralError(httpTool, e); }
    }

    /**
     * 执行调用逻辑。
     */
    private HttpToolInvokeResult doInvoke(HttpTool httpTool, Map<java.lang.String, Object> payload) {
        Map<java.lang.String, Object> filteredPayload = validateAndFilter(httpTool, payload);
        String resolvedUrl = resolveUrlVariables(httpTool.getEndpoint(), filteredPayload);
        ResponseEntity<java.lang.String> response = retryTemplate.execute(
                context -> doHttpRequest(resolvedUrl, httpTool.getHttpMethod(), filteredPayload)
        );
        log.info("外部工具调用成功: toolId={}, status={}", httpTool.getId(), response.getStatusCode());
        return new HttpToolInvokeResult(httpTool.getId(), "SUCCESS", parseResponse(response.getBody()));
    }

    /**
     * 处理客户端错误。
     */
    private HttpToolInvokeResult handleClientError(HttpTool httpTool, HttpClientErrorException e) {
        log.warn("外部工具返回客户端错误: toolId={}, status={}", httpTool.getId(), e.getStatusCode());
        return new HttpToolInvokeResult(httpTool.getId(), "FAILURE",
                Map.of("error", "CLIENT_ERROR:" + e.getStatusCode() + ":" + e.getResponseBodyAsString()));
    }

    /**
     * 处理服务端错误。
     */
    private HttpToolInvokeResult handleServerError(HttpTool httpTool, HttpServerErrorException e) {
        log.error("外部工具返回服务端错误: toolId={}, status={}", httpTool.getId(), e.getStatusCode());
        return new HttpToolInvokeResult(httpTool.getId(), "FAILURE",
                Map.of("error", "SERVER_ERROR:" + e.getStatusCode() + ":" + e.getResponseBodyAsString()));
    }

    /**
     * 处理超时错误。
     */
    private HttpToolInvokeResult handleTimeout(HttpTool httpTool, ResourceAccessException e) {
        log.error("外部工具连接超时: toolId={}", httpTool.getId(), e);
        return new HttpToolInvokeResult(httpTool.getId(), "FAILURE", Map.of("error", "TIMEOUT:" + e.getMessage()));
    }

    /**
     * 处理一般错误。
     */
    private HttpToolInvokeResult handleGeneralError(HttpTool httpTool, Exception e) {
        log.error("外部工具调用异常: toolId={}", httpTool.getId(), e);
        return new HttpToolInvokeResult(httpTool.getId(), "FAILURE", Map.of("error", "ERROR:" + e.getMessage()));
    }

    /**
     * 参数白名单校验：仅允许 inputSchema 中定义的属性通过。
     */
    private Map<java.lang.String, Object> validateAndFilter(HttpTool httpTool, Map<java.lang.String, Object> payload) {
        if (httpTool.getInputSchemaJson() == null || httpTool.getInputSchemaJson().isBlank()) return payload;
        try {
            return filterBySchema(httpTool, payload);
        } catch (JsonProcessingException e) {
            log.error("解析 inputSchema 失败: toolId={}", httpTool.getId(), e);
            throw new IllegalArgumentException("inputSchema JSON 格式无效: " + e.getMessage());
        }
    }

    /**
     * 根据 schema 过滤参数。
     */
    private Map<java.lang.String, Object> filterBySchema(HttpTool httpTool, Map<java.lang.String, Object> payload) throws JsonProcessingException {
        JsonNode schemaNode = objectMapper.readTree(httpTool.getInputSchemaJson());
        JsonNode propertiesNode = schemaNode.get("properties");
        if (propertiesNode == null || !propertiesNode.isObject()) return payload;
        Set<java.lang.String> allowedKeys = getAllowedKeys(propertiesNode);
        validateRequiredFields(schemaNode, payload);
        return filterPayload(payload, allowedKeys);
    }

    /**
     * 获取允许的参数名。
     */
    private Set<java.lang.String> getAllowedKeys(JsonNode propertiesNode) {
        Set<java.lang.String> allowedKeys = new HashSet<>();
        propertiesNode.fieldNames().forEachRemaining(allowedKeys::add);
        return allowedKeys;
    }

    /**
     * 校验必填字段。
     */
    private void validateRequiredFields(JsonNode schemaNode, Map<java.lang.String, Object> payload) {
        JsonNode requiredNode = schemaNode.get("required");
        if (requiredNode == null || !requiredNode.isArray()) return;
        for (JsonNode req : requiredNode) {
            String key = req.asText();
            if (!payload.containsKey(key)) throw new IllegalArgumentException("缺少必填参数: " + key);
        }
    }

    /**
     * 过滤参数。
     */
    private Map<java.lang.String, Object> filterPayload(Map<java.lang.String, Object> payload, Set<java.lang.String> allowedKeys) {
        Map<java.lang.String, Object> filtered = new HashMap<>();
        for (Map.Entry<java.lang.String, Object> entry : payload.entrySet()) {
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
    private String resolveUrlVariables(String endpoint, Map<java.lang.String, Object> payload) {
        Matcher matcher = URL_VARIABLE_PATTERN.matcher(endpoint);
        StringBuilder resolved = new StringBuilder();
        while (matcher.find()) replaceVariable(matcher, payload, resolved);
        matcher.appendTail(resolved);
        return resolved.toString();
    }

    private void replaceVariable(Matcher matcher, Map<java.lang.String, Object> payload, StringBuilder resolved) {
        String varName = matcher.group(1);
        Object value = payload.get(varName);
        String replacement = value != null ? value.toString() : matcher.group();
        matcher.appendReplacement(resolved, Matcher.quoteReplacement(replacement));
    }

    /**
     * 提取 URL 中定义的路径变量名。
     */
    private Set<java.lang.String> extractUrlVariableNames(String endpoint) {
        Set<java.lang.String> vars = new HashSet<>();
        Matcher matcher = URL_VARIABLE_PATTERN.matcher(endpoint);
        while (matcher.find()) {
            vars.add(matcher.group(1));
        }
        return vars;
    }

    /**
     * 执行实际的 HTTP 请求。
     */
    private ResponseEntity<java.lang.String> doHttpRequest(String url, String method, Map<java.lang.String, Object> payload) {
        HttpMethod httpMethod = HttpMethod.valueOf(method);
        if (httpMethod == HttpMethod.GET || httpMethod == HttpMethod.DELETE) {
            return executeGetOrDelete(url, httpMethod, payload);
        }
        return executePostOrPut(url, httpMethod, payload);
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
    private ResponseEntity<java.lang.String> executeGetOrDelete(String url, HttpMethod httpMethod, Map<java.lang.String, Object> payload) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
        Set<java.lang.String> urlVars = extractUrlVariableNames(url);
        for (Map.Entry<java.lang.String, Object> entry : payload.entrySet()) {
            if (!urlVars.contains(entry.getKey())) builder.queryParam(entry.getKey(), entry.getValue());
        }
        String finalUrl = builder.build().toUriString();
        return restTemplate.exchange(finalUrl, httpMethod, new HttpEntity<>(createHeaders()), java.lang.String.class);
    }

    /**
     * 执行 POST 或 PUT 请求。
     */
    private ResponseEntity<java.lang.String> executePostOrPut(String url, HttpMethod httpMethod, Map<java.lang.String, Object> payload) {
        HttpEntity<Map<java.lang.String, Object>> entity = new HttpEntity<>(payload, createHeaders());
        return restTemplate.exchange(url, httpMethod, entity, java.lang.String.class);
    }

    /**
     * 解析 HTTP 响应体为 Map。
     */
    private Map<java.lang.String, Object> parseResponse(String body) {
        if (body == null || body.isBlank()) return Map.of();
        try {
            JsonNode node = objectMapper.readTree(body);
            return node.isObject() ? objectMapper.convertValue(node, MAP_TYPE_REF) : Map.of("data", body);
        } catch (JsonProcessingException e) {
            return Map.of("data", body);
        }
    }
}
