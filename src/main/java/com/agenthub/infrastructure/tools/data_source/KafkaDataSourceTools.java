package com.agenthub.infrastructure.tools.data_source;

import com.agenthub.application.port.out.AgentDataSourcePort;
import com.agenthub.application.port.out.repositories.AgentDataSourceRepository;
import com.agenthub.domain.enums.AgentDataSourceProtocol;
import com.agenthub.domain.exception.ValidationException;
import com.agenthub.domain.model.AgentDataSource;
import com.agenthub.infrastructure.tools.data_source.dto.DataSourceCommandResult;
import com.agenthub.infrastructure.tools.data_source.dto.KafkaConsumeRequest;
import com.agenthub.infrastructure.tools.data_source.dto.KafkaSendRequest;
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
@AgentTools(name = "KafkaDataSourceTools",
        description = "Kafka数据源工具：对已配置的Kafka数据源发送消息和消费消息",
        defaultEnable = true)
public class KafkaDataSourceTools {

    private final AgentDataSourceRepository repository;
    private final AgentDataSourcePort port;

    @Tool(description = "向Kafka主题发送消息")
    public DataSourceCommandResult sendMessage(
            @ToolParam(description = "数据源名称（通过listAgentDataSources获取）") String dataSourceName,
            @ToolParam(description = "发送请求（主题/消息/key）") KafkaSendRequest request,
            ToolContext toolContext) {
        Map<String, Object> params = Map.of(
                "topic", request.getTopic(), "message", request.getMessage(),
                "key", request.getKey() != null ? request.getKey() : "");
        return execute(dataSourceName, params, toolContext);
    }

    @Tool(description = "从Kafka主题消费消息，返回最近的消息列表")
    public DataSourceCommandResult consumeMessages(
            @ToolParam(description = "数据源名称（通过listAgentDataSources获取）") String dataSourceName,
            @ToolParam(description = "消费请求（主题/数量）") KafkaConsumeRequest request,
            ToolContext toolContext) {
        return execute(dataSourceName, Map.of(
                "operation", "consume", "topic", request.getTopic(),
                "maxMessages", Math.min(request.getMaxMessages() < 1 ? 10 : request.getMaxMessages(), 100)), toolContext);
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
                .filter(s -> s.getProtocol() == AgentDataSourceProtocol.KAFKA)
                .filter(s -> s.getName().equals(name) && s.isEnabled())
                .findFirst()
                .orElseThrow(() -> new ValidationException("Kafka数据源未找到或已禁用: " + name));
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
