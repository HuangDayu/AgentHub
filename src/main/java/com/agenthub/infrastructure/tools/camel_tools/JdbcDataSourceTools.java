package com.agenthub.infrastructure.tools.camel_tools;

import com.agenthub.application.port.out.AgentDataSourcePort;
import com.agenthub.application.port.out.repositories.AgentDataSourceRepository;
import com.agenthub.domain.enums.AgentDataSourceProtocol;
import com.agenthub.domain.exception.ValidationException;
import com.agenthub.domain.model.data_source.AgentDataSource;
import com.agenthub.infrastructure.tools.camel_tools.dto.JdbcQueryResult;
import com.agenthub.infrastructure.tools.camel_tools.dto.JdbcUpdateResult;
import com.agenthub.infrastructure.tools.annotations.AgentTools;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.List;
import java.util.Map;

import static com.agenthub.infrastructure.tools.SystemToolsUtils.getWorkspace;

@RequiredArgsConstructor
@AgentTools(name = "JdbcDataSourceTools",
        description = "JDBC/SQL数据源工具：对已配置的JDBC数据源执行SQL查询和更新操作",
        defaultEnable = true)
public class JdbcDataSourceTools {

    private final AgentDataSourceRepository repository;
    private final AgentDataSourcePort port;

    @Tool(description = "对指定JDBC数据源执行SELECT查询，返回列名和数据行")
    public JdbcQueryResult executeQuery(
            @ToolParam(description = "数据源名称（通过listAgentDataSources获取）") String dataSourceName,
            @ToolParam(description = "SQL查询语句，必须是SELECT") String sql,
            ToolContext toolContext) {
        AgentDataSource source = findSource(toolContext, dataSourceName);
        AgentDataSourcePort.AgentDataSourceInvokeResult result = port.invoke(source, Map.of(), sql);
        return toQueryResult(result);
    }

    @Tool(description = "对指定JDBC数据源执行INSERT/UPDATE/DELETE操作，返回影响行数")
    public JdbcUpdateResult executeUpdate(
            @ToolParam(description = "数据源名称（通过listAgentDataSources获取）") String dataSourceName,
            @ToolParam(description = "SQL更新语句，支持INSERT/UPDATE/DELETE") String sql,
            ToolContext toolContext) {
        AgentDataSource source = findSource(toolContext, dataSourceName);
        AgentDataSourcePort.AgentDataSourceInvokeResult result = port.invoke(source, Map.of(), sql);
        return toUpdateResult(result);
    }

    private AgentDataSource findSource(ToolContext ctx, String name) {
        String workspaceId = getWorkspace(ctx).getWorkspace().getId();
        List<AgentDataSource> sources = repository.findByWorkspaceId(workspaceId);
        return sources.stream()
                .filter(s -> isJdbcProtocol(s.getProtocol()))
                .filter(s -> s.getName().equals(name) && s.isEnabled())
                .findFirst()
                .orElseThrow(() -> new ValidationException("JDBC数据源未找到或已禁用: " + name));
    }

    private static boolean isJdbcProtocol(AgentDataSourceProtocol p) {
        return p == AgentDataSourceProtocol.JDBC || p == AgentDataSourceProtocol.SQL;
    }

    @SuppressWarnings("unchecked")
    private JdbcQueryResult toQueryResult(AgentDataSourcePort.AgentDataSourceInvokeResult r) {
        JdbcQueryResult res = new JdbcQueryResult();
        res.setSuccess(r.isSuccess());
        res.setElapsedMs(r.getElapsedMs());
        if (!r.isSuccess()) { res.setErrorMessage(r.getErrorMessage()); return res; }
        if (r.getData() instanceof List) {
            List<Map<String, Object>> rows = (List<Map<String, Object>>) r.getData();
            res.setRows(rows);
            res.setRowCount(rows.size());
            if (!rows.isEmpty()) res.setColumns(rows.get(0).keySet().stream().toList());
        }
        return res;
    }

    private JdbcUpdateResult toUpdateResult(AgentDataSourcePort.AgentDataSourceInvokeResult r) {
        JdbcUpdateResult res = new JdbcUpdateResult();
        res.setSuccess(r.isSuccess());
        res.setElapsedMs(r.getElapsedMs());
        if (!r.isSuccess()) { res.setErrorMessage(r.getErrorMessage()); return res; }
        if (r.getData() instanceof Number) res.setAffectedRows(((Number) r.getData()).intValue());
        return res;
    }
}
