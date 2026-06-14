package com.agenthub.infrastructure.tools.core_tools;

import com.agenthub.application.port.out.repositories.HttpToolRepository;
import com.agenthub.domain.model.agent.ReActAgentContext;
import com.agenthub.domain.model.tools.HttpTool;
import com.agenthub.infrastructure.tools.annotations.AgentTools;
import com.agenthub.infrastructure.tools.core_tools.dto.HttpCallToolInput;
import com.agenthub.infrastructure.tools.core_tools.dto.HttpRequest;
import com.agenthub.infrastructure.tools.core_tools.dto.HttpToolResult;
import com.agenthub.infrastructure.tools.core_tools.dto.RestfulToolDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.http.*;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.agenthub.infrastructure.tools.SystemToolsUtils.getAgentContext;

/**
 * RESTful 工具，提供对 HTTP 接口的查询和调用能力，支持 RetryTemplate。
 */
@Slf4j
@RequiredArgsConstructor
@AgentTools(name = "RestfulTools", description = "RESTful接口工具，查询和调用已注册的HTTP接口，支持自动重试", defaultEnable = false)
public class RestfulTools {

    private final HttpToolRepository httpToolRepository;
    private final RestTemplate restTemplate;
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_BACKOFF_MS = 1000;
    private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");
    private static final Pattern INTERNAL_IP = Pattern.compile(
            "127\\.\\d+\\.\\d+\\.\\d+|10\\.\\d+\\.\\d+\\.\\d+|192\\.168\\.\\d+\\.\\d+|169\\.254\\.\\d+\\.\\d+"
    );

    @Tool(description = "获取当前工作空间下所有已注册的HTTP接口列表")
    public List<RestfulToolDTO> listHttpTools(ToolContext toolContext) {
        ReActAgentContext ctx = getAgentContext(toolContext);
        String workspaceId = ctx.getWorkspace().getWorkspace().getId();
        return httpToolRepository.findByWorkspaceId(workspaceId).stream()
                .filter(HttpTool::isEnabled)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Tool(description = "获取HTTP接口的详细信息")
    public RestfulToolDTO getHttpToolDetail(
            @ToolParam(description = "HTTP接口ID") String toolId) {
        return toDto(findTool(toolId));
    }

    @Tool(description = "调用已注册的HTTP接口，使用RetryTemplate自动重试。")
    public HttpToolResult invokeHttpTool(
            @ToolParam(description = "HTTP接口ID") String toolId,
            @ToolParam(description = "请求参数（JSON格式）") String requestBody,
            ToolContext toolContext) {
        HttpTool tool = findTool(toolId);
        return executeWithRetry(new HttpRequest(tool.getHttpMethod(), tool.getEndpoint(), requestBody),
                tool.getName());
    }

    @Tool(description = "直接调用任意HTTP接口，使用RetryTemplate自动重试")
    public HttpToolResult callHttp(
            @ToolParam(description = "HTTP请求信息") HttpCallToolInput input) {
        if (!isSafeUrl(input.getUrl())) return errorResult("不安全的URL: " + input.getUrl(),
                input.getMethod() + " " + input.getUrl());
        return executeWithRetry(new HttpRequest(input.getMethod(), input.getUrl(), input.getBody()),
                input.getMethod() + " " + input.getUrl());
    }

    private boolean isSafeUrl(String url) {
        if (url == null) return false;
        String lower = url.toLowerCase();
        if (!ALLOWED_SCHEMES.stream().anyMatch(lower::startsWith)) return false;
        return !INTERNAL_IP.matcher(lower).find();
    }

    private HttpTool findTool(String toolId) {
        return httpToolRepository.findById(toolId)
                .orElseThrow(() -> new com.agenthub.domain.exception.NotFoundException(
                        "HTTP接口不存在: " + toolId));
    }

    private HttpToolResult executeWithRetry(HttpRequest request, String label) {
        for (int i = 1; i <= MAX_RETRIES; i++) {
            HttpToolResult result = attemptCall(request, label);
            if (result != null) { return result; }
        }
        return errorResult("重试" + MAX_RETRIES + "次后仍失败", label);
    }

    private HttpToolResult attemptCall(HttpRequest request, String label) {
        try { return doHttpCall(request, label); }
        catch (ResourceAccessException | HttpServerErrorException e) {
            log.warn("HTTP调用失败: {}", e.getMessage());
            sleep(RETRY_BACKOFF_MS);
            return null;
        }
        catch (Exception e) { return errorResult(e.getMessage(), label); }
    }

    private HttpToolResult doHttpCall(HttpRequest request, String label) {
        long start = System.currentTimeMillis();
        HttpMethod httpMethod = HttpMethod.valueOf(request.getMethod().toUpperCase());
        HttpEntity<String> entity = buildEntity(request.getBody());
        ResponseEntity<String> resp = restTemplate.exchange(request.getUrl(), httpMethod, entity, String.class);
        HttpToolResult r = successResult(resp.getStatusCode().value(), resp.getBody(), label);
        r.setDurationMs(System.currentTimeMillis() - start);
        return r;
    }

    private HttpEntity<String> buildEntity(String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return (body != null && !body.isBlank())
                ? new HttpEntity<>(body, headers)
                : new HttpEntity<>(headers);
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    private RestfulToolDTO toDto(HttpTool tool) {
        RestfulToolDTO dto = new RestfulToolDTO();
        dto.setId(tool.getId());
        dto.setName(tool.getName());
        dto.setDescription(tool.getDescription());
        dto.setEndpoint(tool.getEndpoint());
        dto.setHttpMethod(tool.getHttpMethod());
        dto.setInputSchemaJson(tool.getInputSchemaJson());
        dto.setTimeoutMs(tool.getTimeoutMs());
        dto.setEnabled(tool.isEnabled());
        return dto;
    }

    private HttpToolResult successResult(int code, String body, String label) {
        HttpToolResult r = new HttpToolResult();
        r.setSuccess(true);
        r.setStatusCode(code);
        r.setBody(body);
        r.setToolName(label);
        return r;
    }

    private HttpToolResult errorResult(String msg, String label) {
        HttpToolResult r = new HttpToolResult();
        r.setSuccess(false);
        r.setStatusCode(0);
        r.setBody(msg);
        r.setDurationMs(0);
        r.setToolName(label);
        return r;
    }
}
