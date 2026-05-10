package com.agenthub.infrastructure.tools.http_tools;

import com.agenthub.domain.model.HttpTool;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * HTTP工具回调，将HttpTool适配为Spring AI的ToolCallback。
 * 
 * @author huangdayu
 */
@Slf4j
public class HttpToolCallback implements ToolCallback {
    
    private final HttpTool httpTool;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public HttpToolCallback(HttpTool httpTool, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.httpTool = httpTool;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }
    
    public String getName() {
        return httpTool.getName();
    }
    
    @Override
    public ToolDefinition getToolDefinition() {
        return ToolDefinition.builder()
                .name(httpTool.getName())
                .description(httpTool.getDescription())
                .inputSchema(resolveInputSchema())
                .build();
    }
    
    @Override
    public String call(String functionInput) {
        try {
            return executeHttpCall(functionInput);
        } catch (Exception e) {
            log.error("HTTP tool call failed: {}", httpTool.getName(), e);
            return buildErrorResponse(e);
        }
    }
    
    private String executeHttpCall(String functionInput) throws Exception {
        Map<String, Object> payload = parseInput(functionInput);
        HttpEntity<Map<String, Object>> entity = createHttpEntity(payload);
        ResponseEntity<String> response = sendRequest(entity);
        return response.getBody();
    }
    
    private Map<String, Object> parseInput(String input) throws Exception {
        if (input == null || input.isBlank()) return Map.of();
        return objectMapper.readValue(input, objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));
    }
    
    private HttpEntity<Map<String, Object>> createHttpEntity(Map<String, Object> payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(payload, headers);
    }
    
    private ResponseEntity<String> sendRequest(HttpEntity<Map<String, Object>> entity) {
        HttpMethod method = HttpMethod.valueOf(httpTool.getHttpMethod());
        return restTemplate.exchange(httpTool.getEndpoint(), method, entity, String.class);
    }
    
    private String resolveInputSchema() {
        return httpTool.getInputSchemaJson() != null ? httpTool.getInputSchemaJson() : "{\"type\":\"object\"}";
    }
    
    private String buildErrorResponse(Exception e) {
        return "{\"error\":\"" + e.getMessage() + "\"}";
    }
}
