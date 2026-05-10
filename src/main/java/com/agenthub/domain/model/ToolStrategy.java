package com.agenthub.domain.model;

import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.agenthub.common.utils.RandomUtils.randomId;

/**
 * 工具策略聚合根。
 */
@Data
public class ToolStrategy {
    private String id;
    private String tenantId;
    private String workspaceId;
    private String name;
    private String description;
    private int maxConcurrentCalls;
    private int timeoutSeconds;
    private int retryCount;
    private boolean fallbackEnabled;
    private final List<ToolBinding> toolBindings;
    private final Instant createdAt;
    private Instant updatedAt;

    private ToolStrategy(String id, String workspaceId, Instant createdAt) {
        this.id = id;
        this.tenantId = null; // tenantId由MyBatis-Plus拦截器自动填充
        this.workspaceId = workspaceId;
        this.createdAt = createdAt;
        this.maxConcurrentCalls = 5;
        this.timeoutSeconds = 30;
        this.retryCount = 3;
        this.toolBindings = new ArrayList<>();
    }

    public static ToolStrategy create(String workspaceId, String name) {
        ToolStrategy strategy = new ToolStrategy(randomId(), workspaceId, Instant.now());
        strategy.name = name;
        strategy.updatedAt = strategy.createdAt;
        return strategy;
    }

    /**
     * 从持久化层重建对象（保留所有原始数据）。
     */
    public static ToolStrategy rebuild(
            String id, String workspaceId, String name, String description,
            int maxConcurrentCalls, int timeoutSeconds, int retryCount,
            boolean fallbackEnabled, Instant createdAt, Instant updatedAt) {
        ToolStrategy strategy = new ToolStrategy(id, workspaceId, createdAt);
        strategy.name = name;
        strategy.description = description;
        strategy.maxConcurrentCalls = maxConcurrentCalls;
        strategy.timeoutSeconds = timeoutSeconds;
        strategy.retryCount = retryCount;
        strategy.fallbackEnabled = fallbackEnabled;
        strategy.updatedAt = updatedAt;
        return strategy;
    }

    public void updateBasicInfo(String name, String description) {
        this.name = name;
        this.description = description;
        this.updatedAt = Instant.now();
    }

    public void configureExecution(int maxConcurrent, int timeout, int retry) {
        this.maxConcurrentCalls = maxConcurrent;
        this.timeoutSeconds = timeout;
        this.retryCount = retry;
        this.updatedAt = Instant.now();
    }

    public void enableFallback() {
        this.fallbackEnabled = true;
        this.updatedAt = Instant.now();
    }

    public void disableFallback() {
        this.fallbackEnabled = false;
        this.updatedAt = Instant.now();
    }

    public void addToolBinding(String toolId, int priority, boolean enabled) {
        toolBindings.removeIf(b -> b.getToolId().equals(toolId));
        toolBindings.add(new ToolBinding(toolId, priority, enabled));
        this.updatedAt = Instant.now();
    }

    public void removeToolBinding(String toolId) {
        toolBindings.removeIf(b -> b.getToolId().equals(toolId));
        this.updatedAt = Instant.now();
    }

    public static class ToolBinding {
        private final String toolId;
        private final int priority;
        private final boolean enabled;

        public ToolBinding(String toolId, int priority, boolean enabled) {
            this.toolId = toolId;
            this.priority = priority;
            this.enabled = enabled;
        }

        public String getToolId() {
            return toolId;
        }

        public int getPriority() {
            return priority;
        }

        public boolean isEnabled() {
            return enabled;
        }
    }
}
