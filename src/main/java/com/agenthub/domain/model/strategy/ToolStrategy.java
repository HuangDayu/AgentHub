package com.agenthub.domain.model.strategy;

import com.agenthub.domain.model.agent.ReActAgentContext;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static com.agenthub.common.utils.RandomUtils.randomId;

/**
 * 工具策略聚合根。
 * <p>
 * 控制工具调用行为，包括并发控制、超时管理、重试策略等。
 * </p>
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

    @EqualsAndHashCode.Exclude
    private final AtomicInteger concurrentCalls = new AtomicInteger(0);

    @EqualsAndHashCode.Exclude
    private final Set<String> activeCalls = ConcurrentHashMap.newKeySet();

    private ToolStrategy(String id, String workspaceId, Instant createdAt) {
        this.id = id;
        this.tenantId = null;
        this.workspaceId = workspaceId;
        this.createdAt = createdAt;
        this.maxConcurrentCalls = 5;
        this.timeoutSeconds = 30;
        this.retryCount = 3;
        this.toolBindings = new ArrayList<>();
    }

    /**
     * 创建工具策略实例。
     *
     * @param workspaceId 工作空间ID
     * @param name 策略名称
     * @return 策略实例
     */
    public static ToolStrategy create(String workspaceId, String name) {
        ToolStrategy strategy = new ToolStrategy(randomId(), workspaceId, Instant.now());
        strategy.name = name;
        strategy.updatedAt = strategy.createdAt;
        return strategy;
    }

    /**
     * 从持久化层重建对象。
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

    /**
     * 工具调用前生命周期钩子。
     *
     * @param context Agent 上下文
     * @param toolName 工具名称
     * @param arguments 调用参数
     */
    public void beforeToolCall(ReActAgentContext context,
                               String toolName,
                               String arguments) {
        concurrentCalls.incrementAndGet();
        activeCalls.add(toolName);
    }

    /**
     * 工具调用后生命周期钩子。
     *
     * @param context Agent 上下文
     * @param toolName 工具名称
     * @param result 执行结果
     * @return 处理后的结果
     */
    public String afterToolCall(ReActAgentContext context,
                                String toolName,
                                String result) {
        activeCalls.remove(toolName);
        concurrentCalls.decrementAndGet();
        return result;
    }

    /**
     * 检查工具是否允许调用。
     *
     * @param context Agent 上下文
     * @param toolName 工具名称
     * @return 是否允许
     */
    public boolean isToolAllowed(ReActAgentContext context, String toolName) {
        return toolBindings.stream()
                .filter(ToolBinding::isEnabled)
                .anyMatch(b -> b.getToolId().equals(toolName));
    }

    /**
     * 更新基本信息。
     *
     * @param name 策略名称
     * @param description 策略描述
     */
    public void updateBasicInfo(String name, String description) {
        this.name = name;
        this.description = description;
        this.updatedAt = Instant.now();
    }

    /**
     * 配置执行参数。
     *
     * @param maxConcurrent 最大并发数
     * @param timeout 超时时间（秒）
     * @param retry 重试次数
     */
    public void configureExecution(int maxConcurrent, int timeout, int retry) {
        this.maxConcurrentCalls = maxConcurrent;
        this.timeoutSeconds = timeout;
        this.retryCount = retry;
        this.updatedAt = Instant.now();
    }

    /** 启用回退策略。 */
    public void enableFallback() {
        this.fallbackEnabled = true;
        this.updatedAt = Instant.now();
    }

    /** 禁用回退策略。 */
    public void disableFallback() {
        this.fallbackEnabled = false;
        this.updatedAt = Instant.now();
    }

    /**
     * 添加工具绑定。
     *
     * @param toolId 工具ID
     * @param priority 优先级
     * @param enabled 是否启用
     */
    public void addToolBinding(String toolId, int priority, boolean enabled) {
        toolBindings.removeIf(b -> b.getToolId().equals(toolId));
        toolBindings.add(new ToolBinding(toolId, priority, enabled));
        this.updatedAt = Instant.now();
    }

    /**
     * 移除工具绑定。
     *
     * @param toolId 工具ID
     */
    public void removeToolBinding(String toolId) {
        toolBindings.removeIf(b -> b.getToolId().equals(toolId));
        this.updatedAt = Instant.now();
    }

    /**
     * 工具绑定信息。
     */
    public static class ToolBinding {
        private final String toolId;
        private final int priority;
        private final boolean enabled;

        /**
         * 构造工具绑定。
         *
         * @param toolId 工具ID
         * @param priority 优先级
         * @param enabled 是否启用
         */
        public ToolBinding(String toolId, int priority, boolean enabled) {
            this.toolId = toolId;
            this.priority = priority;
            this.enabled = enabled;
        }

        /** 获取工具ID。 */
        public String getToolId() {
            return toolId;
        }

        /** 获取优先级。 */
        public int getPriority() {
            return priority;
        }

        /** 是否启用。 */
        public boolean isEnabled() {
            return enabled;
        }
    }
}
