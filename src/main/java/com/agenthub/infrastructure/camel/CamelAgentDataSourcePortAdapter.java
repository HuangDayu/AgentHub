package com.agenthub.infrastructure.camel;

import com.agenthub.application.port.out.AgentDataSourcePort;
import com.agenthub.domain.model.AgentDataSource;
import com.agenthub.domain.model.AgentDataSourceDescriptor;
import com.agenthub.domain.model.AgentDataSourceField;
import com.agenthub.domain.model.DataSourceSchema;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Camel AgentDataSourcePort 适配器 - 在基础设施层实现 application 端口
 */
@Component
@RequiredArgsConstructor
public class CamelAgentDataSourcePortAdapter implements AgentDataSourcePort {
    private final CamelAgentDataSourceAdapter adapter;
    private final CamelComponentIntrospector introspector;

    @Override
    public void bootstrap(AgentDataSource source) {
        adapter.bootstrap(source);
    }

    @Override
    public void shutdown(String dataSourceId) {
        adapter.shutdown(dataSourceId);
    }

    @Override
    public AgentDataSourceTestResult test(AgentDataSource source) {
        long start = System.currentTimeMillis();
        boolean ok = adapter.test(source);
        AgentDataSourceTestResult result = new AgentDataSourceTestResult();
        result.setSuccess(ok);
        result.setElapsedMs(System.currentTimeMillis() - start);
        result.setMessage(ok ? "ok" : "failed");
        return result;
    }

    @Override
    public AgentDataSourceInvokeResult invoke(AgentDataSource source, Map<String, Object> headers, String body) {
        long start = System.currentTimeMillis();
        String exchangeId = UUID.randomUUID().toString();
        try {
            return buildSuccessResult(adapter.invoke(source, headers, body), exchangeId, start);
        } catch (Exception e) {
            return buildErrorResult(e.getMessage(), exchangeId, start);
        }
    }

    private AgentDataSourceInvokeResult buildSuccessResult(Object data, String exchangeId, long start) {
        AgentDataSourceInvokeResult result = new AgentDataSourceInvokeResult();
        result.setSuccess(true);
        result.setData(data);
        result.setExchangeId(exchangeId);
        result.setElapsedMs(System.currentTimeMillis() - start);
        return result;
    }

    private AgentDataSourceInvokeResult buildErrorResult(String msg, String exchangeId, long start) {
        AgentDataSourceInvokeResult result = new AgentDataSourceInvokeResult();
        result.setSuccess(false);
        result.setErrorMessage(msg);
        result.setExchangeId(exchangeId);
        result.setElapsedMs(System.currentTimeMillis() - start);
        return result;
    }

    @Override
    public List<AgentDataSourceDescriptor> listDescriptors() {
        return introspector.listDescriptors();
    }

    /**
     * JDBC 自动发现 Schema（通过 INFORMATION_SCHEMA）
     */
    public DataSourceSchema introspect(AgentDataSource source) {
        // 默认返回空 schema，JDBC 真实实现由具体 Camel JDBC Component 负责
        DataSourceSchema schema = new DataSourceSchema();
        schema.setTables(new ArrayList<>());
        return schema;
    }
}
