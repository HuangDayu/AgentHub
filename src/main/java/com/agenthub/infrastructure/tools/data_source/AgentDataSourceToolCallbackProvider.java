package com.agenthub.infrastructure.tools.data_source;

import com.agenthub.application.port.out.repositories.AgentDataSourceRepository;
import com.agenthub.domain.event.AgentDataSourceChangedEvent;
import com.agenthub.domain.model.AgentDataSource;
import com.agenthub.infrastructure.context.TenantContextHolder;
import com.agenthub.infrastructure.context.TenantThreadContext;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Agent 数据源 ToolCallback 提供者 - 同进程路径。
 * <p>监听 AgentDataSourceChangedEvent 自动刷新缓存；只暴露 enabled 的数据源。</p>
 * <p>Spring AI 自动发现所有 ToolCallbackProvider Bean 并聚合。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AgentDataSourceToolCallbackProvider implements ToolCallbackProvider {

    private final AgentDataSourceRepository repository;
    private final AgentDataSourceToolFactory factory;
    private final ReentrantLock refreshLock = new ReentrantLock();
    private volatile ToolCallback[] cached = new ToolCallback[0];

    /**
     * 启动时加载所有 enabled 数据源
     */
    @PostConstruct
    public void init() {
        refresh();
    }

    /**
     * 返回当前缓存的所有 enabled 数据源 ToolCallback
     */
    @Override
    public ToolCallback[] getToolCallbacks() {
        return cached;
    }

    /**
     * 监听数据源变更事件触发刷新
     */
    @EventListener
    public void onChange(AgentDataSourceChangedEvent event) {
        log.info("agent data source changed: {} - {}", event.getDataSourceId(), event.getChangeType());
        refresh();
    }

    /**
     * 主动刷新缓存（在 ignoreTenantContext 作用域中执行，兼容事件触发时的空租户）
     */
    public void refresh() {
        refreshLock.lock();
        try {
            reloadCallbacks();
        } finally {
            refreshLock.unlock();
        }
    }

    private void reloadCallbacks() {
        try (TenantContextHolder.TenantContextScope scope =
                 TenantContextHolder.open(TenantThreadContext.ignoreContext())) {
            cached = loadEnabledCallbacks();
        } catch (Exception e) {
            log.warn("failed to refresh agent data source tool callbacks", e);
        }
    }

    private ToolCallback[] loadEnabledCallbacks() {
        List<AgentDataSource> enabled = repository.findAll().stream()
            .filter(AgentDataSource::isEnabled)
            .toList();
        ToolCallback[] array = enabled.stream().map(factory::toToolCallback).toArray(ToolCallback[]::new);
        log.info("refreshed agent data source tool callbacks: {} enabled", array.length);
        return array;
    }
}
