package com.agenthub.infrastructure.tools.camel_tools;

import com.agenthub.application.port.out.AgentDataSourcePort;
import com.agenthub.application.port.out.repositories.AgentDataSourceRepository;
import com.agenthub.domain.enums.AgentDataSourceProtocol;
import com.agenthub.domain.exception.ValidationException;
import com.agenthub.domain.model.data_source.AgentDataSource;
import com.agenthub.infrastructure.tools.camel_tools.dto.HttpInvokeResult;
import com.agenthub.infrastructure.tools.camel_tools.dto.HttpRequest;
import com.agenthub.infrastructure.tools.annotations.AgentTools;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.List;
import java.util.Map;

import static com.agenthub.infrastructure.tools.SystemToolsUtils.getWorkspace;

@RequiredArgsConstructor
@AgentTools(name = "HttpDataSourceTools",
        description = "HTTP/REST数据源工具：对已配置的HTTP/HTTPS/REST数据源发送请求并获取响应",
        defaultEnable = true)
public class HttpDataSourceTools {

    private final AgentDataSourceRepository repository;
    private final AgentDataSourcePort port;

    @Tool(description = "发送HTTP请求。通过HttpRequest中的method/path/body指定请求参数。")
    public HttpInvokeResult invokeHttp(
            @ToolParam(description = "数据源名称（通过listAgentDataSources获取）") String dataSourceName,
            @ToolParam(description = "HTTP请求参数（method/path/body）") HttpRequest request,
            ToolContext toolContext) {
        AgentDataSource source = findSource(toolContext, dataSourceName);
        String body = request.getBody() != null ? request.getBody() : "";
        Map<String, Object> headers = Map.of("method", request.getMethod(), "path", request.getPath());
        AgentDataSourcePort.AgentDataSourceInvokeResult result = port.invoke(source, headers, body);
        return toResult(result);
    }

    private AgentDataSource findSource(ToolContext ctx, String name) {
        String workspaceId = getWorkspace(ctx).getWorkspace().getId();
        List<AgentDataSource> sources = repository.findByWorkspaceId(workspaceId);
        return sources.stream()
                .filter(s -> isHttpProtocol(s.getProtocol()))
                .filter(s -> s.getName().equals(name) && s.isEnabled())
                .findFirst()
                .orElseThrow(() -> new ValidationException("HTTP数据源未找到或已禁用: " + name));
    }

    private static boolean isHttpProtocol(AgentDataSourceProtocol p) {
        return p == AgentDataSourceProtocol.HTTP || p == AgentDataSourceProtocol.HTTPS
                || p == AgentDataSourceProtocol.REST;
    }

    private void resolveMapResult(HttpInvokeResult res, Map<String, Object> data) {
        Object status = data.getOrDefault("statusCode", data.get("status"));
        res.setStatusCode(status instanceof Number ? ((Number) status).intValue() : 0);
        res.setBody(data.getOrDefault("body", "").toString());
    }

    @SuppressWarnings("unchecked")
    private HttpInvokeResult toResult(AgentDataSourcePort.AgentDataSourceInvokeResult r) {
        HttpInvokeResult res = new HttpInvokeResult();
        res.setSuccess(r.isSuccess()); res.setElapsedMs(r.getElapsedMs());
        if (!r.isSuccess()) { res.setErrorMessage(r.getErrorMessage()); return res; }
        if (r.getData() instanceof Map) { resolveMapResult(res, (Map<String, Object>) r.getData()); }
        else if (r.getData() != null) { res.setBody(r.getData().toString()); }
        return res;
    }
}
