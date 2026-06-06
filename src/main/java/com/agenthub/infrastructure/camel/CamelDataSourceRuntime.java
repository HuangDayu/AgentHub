package com.agenthub.infrastructure.camel;

import com.agenthub.domain.model.AgentDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Camel 引擎入口 - 管理多工作空间 CamelContext
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CamelDataSourceRuntime {
    private final Map<String, CamelContext> workspaceContexts = new ConcurrentHashMap<>();
    private final CamelAgentDataSourceMapper mapper = new CamelAgentDataSourceMapper();

    /**
     * 获取或创建工作空间的 CamelContext
     */
    public CamelContext getOrCreateContext(String workspaceId) {
        return workspaceContexts.computeIfAbsent(workspaceId, ws -> {
            try {
                CamelContext ctx = new DefaultCamelContext();
                ctx.start();
                log.info("created CamelContext for workspace {}", ws);
                return ctx;
            } catch (Exception e) {
                throw new RuntimeException("failed to create CamelContext for " + ws, e);
            }
        });
    }

    /**
     * 关闭工作空间 CamelContext
     */
    public void shutdownWorkspace(String workspaceId) {
        CamelContext ctx = workspaceContexts.remove(workspaceId);
        if (ctx != null) {
            try {
                ctx.stop();
            } catch (Exception e) {
                log.warn("failed to stop CamelContext for {}", workspaceId, e);
            }
        }
    }

    /**
     * 关闭所有 CamelContext
     */
    public void shutdownAll() {
        workspaceContexts.forEach((ws, ctx) -> {
            try {
                ctx.stop();
            } catch (Exception e) {
                log.warn("failed to stop CamelContext for {}", ws, e);
            }
        });
        workspaceContexts.clear();
    }

    /**
     * 查找任意一个 CamelContext（用于 shutdown）
     */
    public CamelContext findAnyContext() {
        return workspaceContexts.values().stream().findFirst().orElse(null);
    }

    /**
     * 根据工作空间 ID 查找 CamelContext
     */
    public CamelContext findByWorkspace(String workspaceId) {
        return workspaceContexts.get(workspaceId);
    }
}
