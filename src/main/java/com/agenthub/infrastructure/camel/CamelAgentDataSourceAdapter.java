package com.agenthub.infrastructure.camel;

import com.agenthub.domain.enums.AgentDataSourceProtocol;
import com.agenthub.domain.model.datasource.AgentDataSource;
import com.agenthub.infrastructure.store.db.datasource.JdbcDataSourceFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Camel 数据源适配器
 * <p>每数据源对应一条 Camel direct 路由：direct:agent-data-source-{id} → 真实 endpoint。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CamelAgentDataSourceAdapter {
    private final CamelDataSourceRuntime runtime;
    private final JdbcDataSourceFactory dataSourceFactory;
    private final Map<String, DataSource> registeredDataSources = new ConcurrentHashMap<>();
    private final Map<String, Boolean> registeredRoutes = new ConcurrentHashMap<>();

    /**
     * 获取已注册的 JDBC DataSource（供 Schema 自动发现使用）
     */
    public DataSource getRegisteredDataSource(String dataSourceId) {
        return registeredDataSources.get(dataSourceId);
    }

    /**
     * 注册数据源路由
     */
    public void bootstrap(AgentDataSource source) {
        if (source.getProtocol() == null) return;
        try {
            registerDataSourceIfNeeded(source);
            addRoute(source);
            registeredRoutes.put(source.getId(), Boolean.TRUE);
        } catch (Exception e) {
            throw new RuntimeException("failed to bootstrap data source: " + source.getId(), e);
        }
    }

    /**
     * 关闭数据源路由
     */
    public void shutdown(String dataSourceId) {
        CamelContext ctx = runtime.findAnyContext();
        if (ctx == null) return;
        try {
            removeRoute(ctx, routeId(dataSourceId));
            registeredRoutes.remove(dataSourceId);
        } catch (Exception ignored) {
            // 路由可能不存在
        }
        closeDataSource(dataSourceId);
    }

    private void removeRoute(CamelContext ctx, String route) throws Exception {
        if (ctx.getRoute(route) == null) return;
        ctx.getRouteController().stopRoute(route);
        ctx.removeRoute(route);
    }

    /**
     * 测试连接
     */
    public boolean test(AgentDataSource source) {
        try {
            ensureBootstrapped(source);
            return ping(source) != null;
        } catch (Exception e) {
            log.warn("test connection failed for {}", source.getId(), e);
            return false;
        }
    }

    private String ping(AgentDataSource source) {
        ensureBootstrapped(source);
        DataSource ds = registeredDataSources.get(source.getId());
        if (ds == null) return null;
        try (Connection c = ds.getConnection()) {
            return c.isValid(5) ? "ok" : null;
        } catch (Exception e) {
            throw new RuntimeException("ping failed: " + e.getMessage(), e);
        }
    }

    /**
     * 调用数据源
     */
    public Object invoke(AgentDataSource source, Map<String, Object> headers, String body) {
        try {
            ensureBootstrapped(source);
            return doInvoke(source, headers, body);
        } catch (Exception e) {
            throw new RuntimeException("invoke failed: " + e.getMessage(), e);
        }
    }

    private Object doInvoke(AgentDataSource source, Map<String, Object> headers, String body) {
        CamelContext ctx = runtime.getOrCreateContext(source.getWorkspaceId());
        ProducerTemplate template = ctx.createProducerTemplate();
        Map<String, Object> safe = headers == null ? new java.util.HashMap<>() : headers;
        return template.requestBodyAndHeaders(directUri(source.getId()), body, safe, Object.class);
    }

    /**
     * 懒加载注册：若路由未注册则自动注册
     */
    private void ensureBootstrapped(AgentDataSource source) {
        if (Boolean.TRUE.equals(registeredRoutes.get(source.getId()))) return;
        bootstrap(source);
    }

    private void registerDataSourceIfNeeded(AgentDataSource source) {
        if (!requiresJdbcDataSource(source)) return;
        String lookupKey = jdbcLookupKey(source.getEndpointUri());
        DataSource ds = dataSourceFactory.create(source);
        CamelContext ctx = runtime.getOrCreateContext(source.getWorkspaceId());
        ctx.getRegistry().bind(lookupKey, ds);
        registeredDataSources.put(source.getId(), ds);
        log.info("registered DataSource {} for data source {}", lookupKey, source.getId());
    }

    private void closeDataSource(String dataSourceId) {
        DataSource ds = registeredDataSources.remove(dataSourceId);
        if (ds instanceof AutoCloseable ac) {
            try { ac.close(); } catch (Exception ignored) { /* best effort */ }
        }
    }

    private boolean requiresJdbcDataSource(AgentDataSource source) {
        AgentDataSourceProtocol p = source.getProtocol();
        return p == AgentDataSourceProtocol.JDBC || p == AgentDataSourceProtocol.SQL;
    }

    private String jdbcLookupKey(String endpointUri) {
        int idx = endpointUri.indexOf(':');
        String tail = idx < 0 ? endpointUri : endpointUri.substring(idx + 1);
        int q = tail.indexOf('?');
        return q < 0 ? tail : tail.substring(0, q);
    }

    private void addRoute(AgentDataSource source) throws Exception {
        String route = routeId(source.getId());
        CamelContext ctx = runtime.getOrCreateContext(source.getWorkspaceId());
        ctx.addRoutes(buildRoute(route, source.getEndpointUri()));
    }

    private RouteBuilder buildRoute(String routeId, String endpointUri) {
        String cleanUri = stripQuery(endpointUri);
        return new RouteBuilder() {
            @Override
            public void configure() {
                fromF("direct:%s", routeId).routeId(routeId).to(cleanUri);
            }
        };
    }

    private static String stripQuery(String uri) {
        int q = uri.indexOf('?');
        return q < 0 ? uri : uri.substring(0, q);
    }

    private String directUri(String dataSourceId) {
        return "direct:" + routeId(dataSourceId);
    }

    private static String routeId(String dataSourceId) {
        return "agent-data-source-" + dataSourceId;
    }
}
