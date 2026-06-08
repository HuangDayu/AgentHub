package com.agenthub.infrastructure.tools.data_source;

import cn.hutool.json.JSONUtil;
import com.agenthub.application.port.out.AgentDataSourcePort;
import com.agenthub.common.constants.AgentConstants;
import com.agenthub.domain.model.AgentDataSource;
import com.agenthub.domain.model.agent.ReActAgentContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Agent 数据源 ToolCallback 工厂 - 将 AgentDataSource 转换为 Spring AI ToolCallback。
 * <p>同进程路径：Agent 直接调用 ToolCallback → AgentDataSourcePort.invoke → Camel 路由。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AgentDataSourceToolFactory {

    private final AgentDataSourcePort port;

    /**
     * 构造数据源对应的 ToolCallback
     */
    public ToolCallback toToolCallback(AgentDataSource source) {
        String toolName = buildToolName(source);
        return FunctionToolCallback
            .<String, String>builder(toolName, (body, ctx) -> invoke(source, body, ctx))
            .description(buildDescription(source))
            .inputType(String.class)
            .inputSchema(INPUT_SCHEMA)
            .build();
    }

    /**
     * 调用数据源并返回 JSON 字符串
     */
    private String invoke(AgentDataSource source, String body, ToolContext ctx) {
        try {
            return doInvoke(source, body, ctx);
        } catch (Exception e) {
            return errorJson(source, e);
        }
    }

    private String doInvoke(AgentDataSource source, String body, ToolContext ctx) {
        assertWorkspace(source, ctx);
        Map<String, Object> headers = resolveHeaders(ctx);
        AgentDataSourcePort.AgentDataSourceInvokeResult result = port.invoke(source, headers, body);
        return JSONUtil.toJsonStr(toResultMap(result));
    }

    private String errorJson(AgentDataSource source, Exception e) {
        log.warn("tool invoke failed for data source {}", source.getId(), e);
        String msg = e.getMessage() == null ? "unknown" : e.getMessage();
        return JSONUtil.toJsonStr(Map.of("success", false, "errorMessage", msg));
    }

    /**
     * 校验工作空间隔离
     */
    private void assertWorkspace(AgentDataSource source, ToolContext ctx) {
        ReActAgentContext react = extractReActContext(ctx);
        if (react == null || react.getWorkspace() == null) return;
        String callerWs = react.getWorkspace().getWorkspace().getId();
        if (callerWs == null) return;
        if (!callerWs.equals(source.getWorkspaceId())) {
            throw new com.agenthub.domain.exception.AccessDeniedException(
                "data source " + source.getId() + " 不属于当前工作空间");
        }
    }

    private Map<String, Object> resolveHeaders(ToolContext ctx) {
        Map<String, Object> headers = new HashMap<>();
        if (ctx == null) return headers;
        Object hdrs = ctx.getContext().get("headers");
        if (hdrs instanceof Map<?, ?> m) {
            m.forEach((k, v) -> headers.put(String.valueOf(k), v));
        }
        return headers;
    }

    private ReActAgentContext extractReActContext(ToolContext ctx) {
        if (ctx == null) return null;
        Object v = ctx.getContext().get(AgentConstants.AGENT_CONTEXT_KEY);
        return v instanceof ReActAgentContext r ? r : null;
    }

    private Map<String, Object> toResultMap(AgentDataSourcePort.AgentDataSourceInvokeResult result) {
        Map<String, Object> m = new HashMap<>();
        m.put("success", result.isSuccess());
        m.put("elapsedMs", result.getElapsedMs());
        m.put("exchangeId", result.getExchangeId());
        m.put("data", result.getData());
        m.put("errorMessage", result.getErrorMessage());
        return m;
    }

    private String buildToolName(AgentDataSource source) {
        return "agent_data_source_invoke_" + sanitize(source.getId());
    }

    private String buildDescription(AgentDataSource source) {
        return "调用 Agent 数据源 [名称=" + safe(source.getName())
            + ", 协议=" + (source.getProtocol() == null ? "UNKNOWN" : source.getProtocol().name())
            + ", URI=" + safe(source.getEndpointUri())
            + "]。参数: body (字符串, 如 SQL / JSON / 文本)。返回: 调用的结果或错误信息。";
    }

    private static String sanitize(String s) {
        if (s == null) return "anon";
        return s.replaceAll("[^a-zA-Z0-9_]", "_");
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static final String INPUT_SCHEMA = """
        {
          "type": "object",
          "properties": {
            "body": {
              "type": "string",
              "description": "请求内容，如 SQL 语句、JSON 文本或其他数据源期望的负载"
            }
          },
          "required": ["body"]
        }
        """;
}
