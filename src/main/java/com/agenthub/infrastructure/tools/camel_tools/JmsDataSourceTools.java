package com.agenthub.infrastructure.tools.camel_tools;

import com.agenthub.application.port.out.AgentDataSourcePort;
import com.agenthub.application.port.out.repositories.AgentDataSourceRepository;
import com.agenthub.domain.enums.AgentDataSourceProtocol;
import com.agenthub.domain.exception.ValidationException;
import com.agenthub.domain.model.datasource.AgentDataSource;
import com.agenthub.infrastructure.tools.camel_tools.dto.DataSourceCommandResult;
import com.agenthub.infrastructure.tools.camel_tools.dto.JmsSendRequest;
import com.agenthub.infrastructure.tools.annotations.AgentTools;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.List;
import java.util.Map;

import static com.agenthub.infrastructure.tools.SystemToolsUtils.getWorkspace;

@RequiredArgsConstructor
@AgentTools(name = "JmsDataSourceTools",
        description = "JMS数据源工具：通过已配置的JMS（Java消息服务）数据源发送和接收消息",
        defaultEnable = true)
public class JmsDataSourceTools {

    private final AgentDataSourceRepository repository;
    private final AgentDataSourcePort port;

    @Tool(description = "向JMS队列或主题发送消息。返回发送状态。")
    public DataSourceCommandResult sendMessage(
            @ToolParam(description = "数据源名称（通过listAgentDataSources获取）") String dataSourceName,
            @ToolParam(description = "发送请求（目标/消息内容）") JmsSendRequest request,
            ToolContext toolContext) {
        return execute(dataSourceName, Map.of(
                "operation", "send", "destination", request.getDestination(),
                "message", request.getMessage()), toolContext);
    }

    @Tool(description = "从JMS队列或主题接收消息（超时5秒）")
    public DataSourceCommandResult receiveMessage(
            @ToolParam(description = "数据源名称（通过listAgentDataSources获取）") String dataSourceName,
            @ToolParam(description = "JMS目标名称（队列或主题名）") String destination,
            ToolContext toolContext) {
        return execute(dataSourceName, Map.of(
                "operation", "receive", "destination", destination, "timeout", 5000), toolContext);
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
                .filter(s -> s.getProtocol() == AgentDataSourceProtocol.JMS)
                .filter(s -> s.getName().equals(name) && s.isEnabled())
                .findFirst()
                .orElseThrow(() -> new ValidationException("JMS数据源未找到或已禁用: " + name));
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
