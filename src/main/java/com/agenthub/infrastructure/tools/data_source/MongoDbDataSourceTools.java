package com.agenthub.infrastructure.tools.data_source;

import com.agenthub.application.port.out.AgentDataSourcePort;
import com.agenthub.application.port.out.repositories.AgentDataSourceRepository;
import com.agenthub.domain.enums.AgentDataSourceProtocol;
import com.agenthub.domain.exception.ValidationException;
import com.agenthub.domain.model.AgentDataSource;
import com.agenthub.infrastructure.tools.data_source.dto.DataSourceCommandResult;
import com.agenthub.infrastructure.tools.data_source.dto.MongoInsertRequest;
import com.agenthub.infrastructure.tools.data_source.dto.MongoQueryRequest;
import com.agenthub.infrastructure.tools.system_tools.annotations.AgentTools;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.List;
import java.util.Map;

import static com.agenthub.infrastructure.tools.system_tools.SystemToolsUtils.getWorkspace;

@RequiredArgsConstructor
@AgentTools(name = "MongoDbDataSourceTools",
        description = "MongoDB数据源工具：对已配置的MongoDB数据源执行文档查询和写入操作",
        defaultEnable = true)
public class MongoDbDataSourceTools {

    private final AgentDataSourceRepository repository;
    private final AgentDataSourcePort port;

    @Tool(description = "查询MongoDB集合中的文档，支持JSON格式的过滤条件")
    public DataSourceCommandResult find(
            @ToolParam(description = "数据源名称（通过listAgentDataSources获取）") String dataSourceName,
            @ToolParam(description = "查询请求（集合/过滤条件/条数限制）") MongoQueryRequest request,
            ToolContext toolContext) {
        return execute(dataSourceName, Map.of(
                "operation", "find", "collection", request.getCollection(),
                "filter", request.getFilterJson(),
                "limit", Math.min(request.getLimit() < 1 ? 100 : request.getLimit(), 1000)), toolContext);
    }

    @Tool(description = "向MongoDB集合中插入一个文档")
    public DataSourceCommandResult insert(
            @ToolParam(description = "数据源名称（通过listAgentDataSources获取）") String dataSourceName,
            @ToolParam(description = "插入请求（集合/文档JSON）") MongoInsertRequest request,
            ToolContext toolContext) {
        return execute(dataSourceName, Map.of(
                "operation", "insert", "collection", request.getCollection(),
                "document", request.getDocumentJson()), toolContext);
    }

    private DataSourceCommandResult execute(String name, Map<String, Object> params, ToolContext ctx) {
        AgentDataSource source = findSource(ctx, name);
        String body = JSONUtil.toJsonStr(params);
        AgentDataSourcePort.AgentDataSourceInvokeResult result = port.invoke(source, Map.of(), body);
        return toResult(result);
    }

    private AgentDataSource findSource(ToolContext ctx, String name) {
        String workspaceId = getWorkspace(ctx).getWorkspace().getId();
        List<AgentDataSource> sources = repository.findByWorkspaceId(workspaceId);
        return sources.stream()
                .filter(s -> s.getProtocol() == AgentDataSourceProtocol.MONGODB)
                .filter(s -> s.getName().equals(name) && s.isEnabled())
                .findFirst()
                .orElseThrow(() -> new ValidationException("MongoDB数据源未找到或已禁用: " + name));
    }

    private DataSourceCommandResult toResult(AgentDataSourcePort.AgentDataSourceInvokeResult r) {
        DataSourceCommandResult res = new DataSourceCommandResult();
        res.setSuccess(r.isSuccess());
        res.setElapsedMs(r.getElapsedMs());
        if (!r.isSuccess()) { res.setErrorMessage(r.getErrorMessage()); return res; }
        res.setData(r.getData());
        res.setMessage("操作成功，耗时 " + r.getElapsedMs() + "ms");
        return res;
    }
}
