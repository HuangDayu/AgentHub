package com.agenthub.infrastructure.tools.system_tools.core_tools;

import com.agenthub.application.port.out.repositories.HttpToolRepository;
import com.agenthub.domain.model.agent.ReActAgentContext;
import com.agenthub.domain.model.tools.HttpTool;
import com.agenthub.infrastructure.tools.system_tools.annotations.AgentTools;
import com.agenthub.infrastructure.tools.system_tools.core_tools.dto.HttpToolResult;
import com.agenthub.infrastructure.tools.system_tools.core_tools.dto.RestfulToolDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.http.*;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

import static com.agenthub.infrastructure.tools.system_tools.SystemToolsUtils.getAgentContext;

/**
 * RESTful 工具，提供对 HttpToolRepository 管理的 HTTP 接口的查询和调用能力。
 * 使用 RetryTemplate 实现失败自动重试。
 */
@Slf4j
@RequiredArgsConstructor
@AgentTools(name = "RestfulTools", description = "RESTful接口工具，查询和调用已注册的HTTP接口，支持RetryTemplate重试")
public class RestfulTools {

    private final HttpToolRepository httpToolRepository;
    private final RestTemplate restfulToolsRestTemplate;
    private final RetryTemplate restfulToolsRetryTemplate;

    @Tool(description = "获取当前工作空间下所有已注册的HTTP接口列表")
    public List<RestfulToolDTO> listHttpTools(ToolContext toolContext) {
        ReActAgentContext ctx = getAgentContext(toolContext);
        String workspaceId = ctx.getWorkspace().getWorkspace().getId();
        return httpToolRepository.findByWorkspaceId(workspaceId).stream()
                .filter(HttpTool::isEnabled)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Tool(description = "获取HTTP接口的详细信息，包括端点、方法和参数定义")
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
        return executeWithRetry(tool.getHttpMethod(), tool.getEndpoint(),
                requestBody, tool.getName());
    }

    @Tool(description = "直接调用任意HTTP接口，使用RetryTemplate自动重试")
    public HttpToolResult callHttp(
            @ToolParam(description = "请求方法：GET/POST/PUT/DELETE") String method,
            @ToolParam(description = "完整的请求URL") String url,
            @ToolParam(description = "请求头（JSON格式，可选）") String headersJson,
            @ToolParam(description = "请求体（JSON格式，POST/PUT时使用）") String body) {
        return executeWithRetry(method, url, body, method + " " + url);
    }

    private HttpTool findTool(String toolId) {
        return httpToolRepository.findById(toolId)
                .orElseThrow(() -> new com.agenthub.domain.exception.NotFoundException(
                        "HTTP接口不存在: " + toolId));
    }

    private HttpToolResult executeWithRetry(String method, String url,
                                            String body, String label) {
        try {
            return restfulToolsRetryTemplate.execute(
                    context -> doHttpCall(method, url, body, label));
        } catch (Exception e) {
            log.error("HTTP调用最终失败: {}", label, e);
            return errorResult("重试耗尽: " + e.getMessage(), label);
        }
    }

    private HttpToolResult doHttpCall(String method, String url,
                                      String body, String label) {
        long start = System.currentTimeMillis();
        try {
            HttpMethod httpMethod = HttpMethod.valueOf(method.toUpperCase());
            HttpEntity<String> entity = buildEntity(body);
            ResponseEntity<String> resp = restfulToolsRestTemplate.exchange(url, httpMethod, entity, String.class);
            return buildResult(true, resp.getStatusCode().value(),
                    resp.getBody(), System.currentTimeMillis() - start, label);
        } catch (ResourceAccessException | HttpServerErrorException e) {
            throw new org.springframework.retry.RetryException(label, e);
        } catch (Exception e) {
            return errorResult(e.getMessage(), label);
        }
    }


    private HttpEntity<String> buildEntity(String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return (body != null && !body.isBlank())
                ? new HttpEntity<>(body, headers)
                : new HttpEntity<>(headers);
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

    private HttpToolResult buildResult(boolean ok, int code, String body, long ms, String name) {
        HttpToolResult r = new HttpToolResult();
        r.setSuccess(ok);
        r.setStatusCode(code);
        r.setBody(body);
        r.setDurationMs(ms);
        r.setToolName(name);
        return r;
    }

    private HttpToolResult errorResult(String msg, String label) {
        return buildResult(false, 0, msg, 0, label);
    }
}
