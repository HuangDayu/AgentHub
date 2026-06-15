package com.agenthub.infrastructure.camel;

import com.agenthub.application.port.out.AgentDataSourcePort;
import com.agenthub.domain.model.datasource.AgentDataSource;
import com.agenthub.domain.model.datasource.AgentDataSourceDescriptor;
import com.agenthub.domain.model.datasource.DataSourceSchema;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
    private final CamelComponentIntrospector protocolIntrospector;
    private final CamelSchemaIntrospector schemaIntrospector;

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
        return protocolIntrospector.listDescriptors();
    }

    /**
     * JDBC 自动发现 Schema（通过 DatabaseMetaData）
     */
    public DataSourceSchema introspect(AgentDataSource source) {
        return schemaIntrospector.introspect(source);
    }
}
