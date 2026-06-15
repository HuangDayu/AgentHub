package com.agenthub.infrastructure.tools.camel_tools;

import cn.hutool.json.JSONUtil;
import com.agenthub.application.port.out.AgentDataSourcePort;
import com.agenthub.domain.enums.AgentDataSourceProtocol;
import com.agenthub.domain.model.datasource.AgentDataSource;
import lombok.Getter;

import java.util.Map;

@Getter
public class DataSourceToolAdapter {

    private final AgentDataSource source;
    private final AgentDataSourcePort port;

    public DataSourceToolAdapter(AgentDataSource source, AgentDataSourcePort port) {
        this.source = source;
        this.port = port;
    }

    public String invoke(Map<String, Object> params) {
        String body = formatBody(source.getProtocol(), params);
        return doInvoke(body);
    }

    private String formatBody(AgentDataSourceProtocol protocol, Map<String, Object> params) {
        switch (protocol) {
            case JDBC:
            case SQL:
                return stringParam(params, "sql");
            case HTTP:
            case HTTPS:
            case REST:
                return JSONUtil.toJsonStr(params);
            case MONGODB:
                return JSONUtil.toJsonStr(params);
            case KAFKA:
                return JSONUtil.toJsonStr(params);
            case REDIS:
                return JSONUtil.toJsonStr(params);
            case FTP:
            case SFTP:
            case FILE:
                return JSONUtil.toJsonStr(params);
            case JMS:
                return JSONUtil.toJsonStr(params);
            case MAIL:
                return JSONUtil.toJsonStr(params);
            case DIRECT:
                return stringParam(params, "body");
            case TIMER:
                return stringParam(params, "body");
            default:
                return JSONUtil.toJsonStr(params);
        }
    }

    private static String stringParam(Map<String, Object> params, String key) {
        Object v = params.get(key);
        return v != null ? v.toString() : "";
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
}
