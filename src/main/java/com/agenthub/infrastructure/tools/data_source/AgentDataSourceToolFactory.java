package com.agenthub.infrastructure.tools.data_source;

import com.agenthub.application.port.out.AgentDataSourcePort;
import com.agenthub.domain.enums.AgentDataSourceProtocol;
import com.agenthub.domain.model.AgentDataSource;
import com.agenthub.infrastructure.tools.data_source.params.FileOptions;
import com.agenthub.infrastructure.tools.data_source.params.HttpOptions;
import com.agenthub.infrastructure.tools.data_source.params.KafkaOptions;
import com.agenthub.infrastructure.tools.data_source.params.MongoOptions;
import com.agenthub.infrastructure.tools.data_source.params.RedisOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Agent 数据源 ToolCallback 工厂 - 将 AgentDataSource 转换为 Spring AI ToolCallback。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AgentDataSourceToolFactory {

    private final AgentDataSourcePort port;
    private final Map<String, DataSourceToolAdapter> adapterCache = new ConcurrentHashMap<>();

    public ToolCallback toToolCallback(AgentDataSource source) {
        DataSourceToolAdapter adapter = new DataSourceToolAdapter(source, port);
        adapterCache.put(source.getId(), adapter);
        return buildToolCallback(adapter);
    }

    private ToolCallback buildToolCallback(DataSourceToolAdapter adapter) {
        AgentDataSource source = adapter.getSource();
        String toolName = adapter.getToolPrefix() + sanitize(source.getName());
        String description = buildDescription(source);
        AgentDataSourceProtocol protocol = source.getProtocol();

        return switch (protocol) {
            case JDBC, SQL -> FunctionToolCallback
                .<String, String>builder(toolName, (sql, ctx) -> adapter.executeSql(sql))
                .description(description).inputType(String.class).inputSchema(JDBC_SCHEMA).build();
            case HTTP, HTTPS, REST -> FunctionToolCallback
                .<HttpOptions, String>builder(toolName, (opts, ctx) -> {
                    String method = opts.getPath() != null ? "GET" : "GET";
                    return adapter.callHttp(method, opts);
                })
                .description(description).inputType(HttpOptions.class).inputSchema(HTTP_SCHEMA).build();
            case MONGODB -> FunctionToolCallback
                .<MongoOptions, String>builder(toolName, (opts, ctx) -> {
                    return adapter.operateMongo("find", "find", opts);
                })
                .description(description).inputType(MongoOptions.class).inputSchema(MONGO_SCHEMA).build();
            case KAFKA -> FunctionToolCallback
                .<KafkaOptions, String>builder(toolName, (opts, ctx) -> {
                    return adapter.sendKafka("", opts);
                })
                .description(description).inputType(KafkaOptions.class).inputSchema(KAFKA_SCHEMA).build();
            case REDIS -> FunctionToolCallback
                .<RedisOptions, String>builder(toolName, (opts, ctx) -> {
                    return adapter.executeRedis("GET", "", opts);
                })
                .description(description).inputType(RedisOptions.class).inputSchema(REDIS_SCHEMA).build();
            case FTP, SFTP, FILE -> FunctionToolCallback
                .<FileOptions, String>builder(toolName, (opts, ctx) -> {
                    return adapter.operateFile("read", "", opts);
                })
                .description(description).inputType(FileOptions.class).inputSchema(FILE_SCHEMA).build();
            default -> FunctionToolCallback
                .<String, String>builder(toolName, (body, ctx) -> adapter.invokeDefault(body))
                .description(description).inputType(String.class).inputSchema(BODY_SCHEMA).build();
        };
    }

    private String buildDescription(AgentDataSource source) {
        String name = safe(source.getName());
        String uri = safe(source.getEndpointUri());
        String guidance = getProtocolGuidance(source.getProtocol());
        return "调用数据源 [" + name + ", 协议=" + protocolName(source.getProtocol())
            + ", URI=" + uri + "]。" + guidance;
    }

    private String getProtocolGuidance(AgentDataSourceProtocol protocol) {
        if (protocol == null) return "传入body，返回调用结果。";
        return switch (protocol) {
            case JDBC, SQL -> "传入sql参数（仅SELECT），返回查询结果JSON。";
            case HTTP, HTTPS, REST -> "传入method(必填)/path/body/queryParams，返回响应JSON。";
            case MONGODB -> "传入collection/operation/query/document，返回操作结果。";
            case KAFKA -> "传入message(必填)/topic/key/headers，返回发送结果。";
            case REDIS -> "传入command(必填)/key(必填)/value/args，返回命令结果。";
            case FTP, SFTP, FILE -> "传入operation(必填)/path(必填)/content，返回操作结果。";
            default -> "传入body，返回调用结果。";
        };
    }

    private String protocolName(AgentDataSourceProtocol protocol) {
        return protocol != null ? protocol.name() : "UNKNOWN";
    }

    private static String sanitize(String s) {
        if (s == null) return "anon";
        return s.replaceAll("[^a-zA-Z0-9_]", "_");
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static final String JDBC_SCHEMA = """
        {"type":"object","properties":{"sql":{"type":"string","description":"SQL查询语句，仅支持SELECT"}},"required":["sql"]}""";
    private static final String HTTP_SCHEMA = """
        {"type":"object","properties":{"method":{"type":"string","description":"HTTP方法: GET/POST/PUT/DELETE"},"path":{"type":"string","description":"请求路径"},"body":{"type":"string","description":"请求体"},"queryParams":{"type":"string","description":"查询参数"}},"required":["method"]}""";
    private static final String MONGO_SCHEMA = """
        {"type":"object","properties":{"collection":{"type":"string","description":"集合名称"},"operation":{"type":"string","description":"操作类型"},"query":{"type":"string","description":"查询条件"},"document":{"type":"string","description":"文档内容"}},"required":["collection","operation"]}""";
    private static final String KAFKA_SCHEMA = """
        {"type":"object","properties":{"message":{"type":"string","description":"消息内容"},"topic":{"type":"string","description":"Topic"},"key":{"type":"string","description":"消息key"},"headers":{"type":"string","description":"消息头"}},"required":["message"]}""";
    private static final String REDIS_SCHEMA = """
        {"type":"object","properties":{"command":{"type":"string","description":"Redis命令"},"key":{"type":"string","description":"键名"},"value":{"type":"string","description":"值"},"args":{"type":"string","description":"额外参数"}},"required":["command","key"]}""";
    private static final String FILE_SCHEMA = """
        {"type":"object","properties":{"operation":{"type":"string","description":"操作类型"},"path":{"type":"string","description":"文件路径"},"content":{"type":"string","description":"文件内容"}},"required":["operation","path"]}""";
    private static final String BODY_SCHEMA = """
        {"type":"object","properties":{"body":{"type":"string","description":"消息内容"}},"required":["body"]}""";
}
