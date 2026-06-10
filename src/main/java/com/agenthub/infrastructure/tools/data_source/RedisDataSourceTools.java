package com.agenthub.infrastructure.tools.data_source;

import com.agenthub.application.port.out.AgentDataSourcePort;
import com.agenthub.application.port.out.repositories.AgentDataSourceRepository;
import com.agenthub.domain.enums.AgentDataSourceProtocol;
import com.agenthub.domain.exception.ValidationException;
import com.agenthub.domain.model.AgentDataSource;
import com.agenthub.infrastructure.tools.data_source.dto.DataSourceCommandResult;
import com.agenthub.infrastructure.tools.data_source.dto.RedisSetRequest;
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
@AgentTools(name = "RedisDataSourceTools",
        description = "Redis数据源工具：对已配置的Redis数据源执行缓存操作命令",
        defaultEnable = true)
public class RedisDataSourceTools {

    private final AgentDataSourceRepository repository;
    private final AgentDataSourcePort port;

    @Tool(description = "获取指定key的值")
    public DataSourceCommandResult get(
            @ToolParam(description = "数据源名称（通过listAgentDataSources获取）") String dataSourceName,
            @ToolParam(description = "Redis key") String key,
            ToolContext toolContext) {
        return execute(dataSourceName, Map.of("command", "GET", "key", key), toolContext);
    }

    @Tool(description = "设置指定key的值")
    public DataSourceCommandResult set(
            @ToolParam(description = "数据源名称（通过listAgentDataSources获取）") String dataSourceName,
            @ToolParam(description = "设置请求（key/value）") RedisSetRequest request,
            ToolContext toolContext) {
        return execute(dataSourceName, Map.of("command", "SET", "key", request.getKey(), "value", request.getValue()), toolContext);
    }

    @Tool(description = "删除指定key")
    public DataSourceCommandResult del(
            @ToolParam(description = "数据源名称（通过listAgentDataSources获取）") String dataSourceName,
            @ToolParam(description = "Redis key") String key,
            ToolContext toolContext) {
        return execute(dataSourceName, Map.of("command", "DEL", "key", key), toolContext);
    }

    @Tool(description = "执行自定义Redis命令。commandArgs格式为\"COMMAND arg1 arg2\"，如\"KEYS *\"或\"EXISTS mykey\"")
    public DataSourceCommandResult executeCommand(
            @ToolParam(description = "数据源名称（通过listAgentDataSources获取）") String dataSourceName,
            @ToolParam(description = "Redis命令和参数，如\"KEYS *\"或\"EXISTS mykey\"") String commandArgs,
            ToolContext toolContext) {
        return execute(dataSourceName, Map.of("commandArgs", commandArgs), toolContext);
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
                .filter(s -> s.getProtocol() == AgentDataSourceProtocol.REDIS)
                .filter(s -> s.getName().equals(name) && s.isEnabled())
                .findFirst()
                .orElseThrow(() -> new ValidationException("Redis数据源未找到或已禁用: " + name));
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
