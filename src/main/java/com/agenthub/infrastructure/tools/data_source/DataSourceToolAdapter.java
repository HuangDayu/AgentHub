package com.agenthub.infrastructure.tools.data_source;

import cn.hutool.json.JSONUtil;
import com.agenthub.application.port.out.AgentDataSourcePort;
import com.agenthub.domain.enums.AgentDataSourceProtocol;
import com.agenthub.domain.model.AgentDataSource;
import com.agenthub.infrastructure.tools.data_source.params.FileOptions;
import com.agenthub.infrastructure.tools.data_source.params.HttpOptions;
import com.agenthub.infrastructure.tools.data_source.params.KafkaOptions;
import com.agenthub.infrastructure.tools.data_source.params.MongoOptions;
import com.agenthub.infrastructure.tools.data_source.params.RedisOptions;
import lombok.Getter;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.Map;

/**
 * 数据源工具适配器 - 将 AgentDataSource 包装为 @Tool 方法。
 */
@Getter
public class DataSourceToolAdapter {

    private final AgentDataSource source;
    private final AgentDataSourcePort port;
    private final String toolPrefix;

    public DataSourceToolAdapter(AgentDataSource source, AgentDataSourcePort port) {
        this.source = source;
        this.port = port;
        this.toolPrefix = resolveToolPrefix(source.getProtocol());
    }

    @Tool(description = "执行SQL查询，仅支持SELECT，返回查询结果JSON")
    public String executeSql(
            @ToolParam(description = "SQL查询语句，如: SELECT * FROM users WHERE id = 1") String sql) {
        return doInvoke(sql);
    }

    @Tool(description = "调用HTTP接口，返回响应JSON")
    public String callHttp(
            @ToolParam(description = "HTTP方法: GET/POST/PUT/DELETE") String method,
            @ToolParam(description = "请求选项: path/body/queryParams") HttpOptions options) {
        return doInvoke(JSONUtil.toJsonStr(Map.of(
            "method", method != null ? method : "GET",
            "path", options.getPath() != null ? options.getPath() : "",
            "body", options.getBody() != null ? options.getBody() : "",
            "queryParams", options.getQueryParams() != null ? options.getQueryParams() : "")));
    }

    @Tool(description = "操作MongoDB，返回操作结果")
    public String operateMongo(
            @ToolParam(description = "集合名称") String collection,
            @ToolParam(description = "操作: find/findOne/insertOne/updateOne/deleteOne") String operation,
            @ToolParam(description = "操作选项: query/document") MongoOptions options) {
        return doInvoke(JSONUtil.toJsonStr(Map.of(
            "collection", collection != null ? collection : "",
            "operation", operation != null ? operation : "find",
            "query", options.getQuery() != null ? options.getQuery() : "{}",
            "document", options.getDocument() != null ? options.getDocument() : "{}")));
    }

    @Tool(description = "发送Kafka消息，返回发送结果")
    public String sendKafka(
            @ToolParam(description = "消息内容") String message,
            @ToolParam(description = "消息选项: topic/key/headers") KafkaOptions options) {
        return doInvoke(JSONUtil.toJsonStr(Map.of(
            "message", message != null ? message : "",
            "topic", options.getTopic() != null ? options.getTopic() : "",
            "key", options.getKey() != null ? options.getKey() : "",
            "headers", options.getHeaders() != null ? options.getHeaders() : "{}")));
    }

    @Tool(description = "执行Redis命令，返回命令结果")
    public String executeRedis(
            @ToolParam(description = "Redis命令: GET/SET/DEL/HGET/HSET等") String command,
            @ToolParam(description = "键名") String key,
            @ToolParam(description = "操作选项: value/args") RedisOptions options) {
        return doInvoke(JSONUtil.toJsonStr(Map.of(
            "command", command != null ? command : "",
            "key", key != null ? key : "",
            "value", options.getValue() != null ? options.getValue() : "",
            "args", options.getArgs() != null ? options.getArgs() : "[]")));
    }

    @Tool(description = "操作文件系统，返回操作结果")
    public String operateFile(
            @ToolParam(description = "操作: read/write/list/delete/exists") String operation,
            @ToolParam(description = "文件路径") String path,
            @ToolParam(description = "操作选项: content") FileOptions options) {
        return doInvoke(JSONUtil.toJsonStr(Map.of(
            "operation", operation != null ? operation : "",
            "path", path != null ? path : "",
            "content", options.getContent() != null ? options.getContent() : "")));
    }

    @Tool(description = "调用数据源，返回调用结果")
    public String invokeDefault(
            @ToolParam(description = "消息内容或负载数据") String body) {
        return doInvoke(body);
    }

    public String getMethodName() {
        if (source.getProtocol() == null) return "invokeDefault";
        return switch (source.getProtocol()) {
            case JDBC, SQL -> "executeSql";
            case HTTP, HTTPS, REST -> "callHttp";
            case MONGODB -> "operateMongo";
            case KAFKA -> "sendKafka";
            case REDIS -> "executeRedis";
            case FTP, SFTP, FILE -> "operateFile";
            default -> "invokeDefault";
        };
    }

    private String doInvoke(String body) {
        try {
            AgentDataSourcePort.AgentDataSourceInvokeResult result = port.invoke(source, Map.of(), body);
            return JSONUtil.toJsonStr(toResultMap(result));
        } catch (Exception e) {
            String msg = e.getMessage() == null ? "unknown" : e.getMessage();
            return JSONUtil.toJsonStr(Map.of("success", false, "errorMessage", msg));
        }
    }

    private Map<String, Object> toResultMap(AgentDataSourcePort.AgentDataSourceInvokeResult result) {
        return Map.of(
            "success", result.isSuccess(),
            "elapsedMs", result.getElapsedMs(),
            "exchangeId", result.getExchangeId() != null ? result.getExchangeId() : "",
            "data", result.getData() != null ? result.getData() : "",
            "errorMessage", result.getErrorMessage() != null ? result.getErrorMessage() : "");
    }

    private static String resolveToolPrefix(AgentDataSourceProtocol protocol) {
        return PROTOCOL_PREFIX.getOrDefault(protocol, "datasource_invoke_");
    }

    @SuppressWarnings("unchecked")
    private static final Map<AgentDataSourceProtocol, String> PROTOCOL_PREFIX = Map.ofEntries(
        Map.entry(AgentDataSourceProtocol.JDBC, "sql_query_"),
        Map.entry(AgentDataSourceProtocol.SQL, "sql_query_"),
        Map.entry(AgentDataSourceProtocol.HTTP, "http_call_"),
        Map.entry(AgentDataSourceProtocol.HTTPS, "http_call_"),
        Map.entry(AgentDataSourceProtocol.REST, "http_call_"),
        Map.entry(AgentDataSourceProtocol.MONGODB, "mongo_op_"),
        Map.entry(AgentDataSourceProtocol.KAFKA, "kafka_send_"),
        Map.entry(AgentDataSourceProtocol.REDIS, "redis_cmd_"),
        Map.entry(AgentDataSourceProtocol.FTP, "file_op_"),
        Map.entry(AgentDataSourceProtocol.SFTP, "file_op_"),
        Map.entry(AgentDataSourceProtocol.FILE, "file_op_"),
        Map.entry(AgentDataSourceProtocol.JMS, "datasource_invoke_")
    );
}
