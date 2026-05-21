package com.agenthub.infrastructure.workflow.state;

import com.agenthub.domain.enums.workflow.WorkflowStatus;
import com.agenthub.domain.model.workflow.NodeResult;
import com.agenthub.domain.model.workflow.WorkflowContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * 工作流状态管理器。
 * 负责工作流执行状态的持久化和恢复。
 *
 * @author huangdayu
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowStateManager {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    
    /** 状态过期时间 */
    private static final Duration STATE_TTL = Duration.ofHours(24);
    
    /** 历史记录键前缀 */
    private static final String HISTORY_KEY_PREFIX = "workflow:history:";
    
    /** Redis键前缀 */
    private static final String KEY_PREFIX = "workflow:state:";

    /**
     * 保存执行上下文。
     *
     * @param context 执行上下文
     * @return 保存结果的Mono
     */
    public Mono<Void> saveContext(WorkflowContext context) {
        String key = buildContextKey(context.getExecutionId());
        String historyKey = buildHistoryKey(context.getWorkflowId());
        return serializeContext(context)
            .flatMap(json -> saveToRedis(key, json))
            .then(redisTemplate.opsForList().leftPush(historyKey, context.getExecutionId()).then())
            .doOnSuccess(v -> log.debug("保存执行上下文: {}", context.getExecutionId()))
            .doOnError(e -> log.error("保存执行上下文失败: {}", context.getExecutionId(), e));
    }

    /**
     * 加载执行上下文。
     *
     * @param executionId 执行ID
     * @return 执行上下文的Mono
     */
    public Mono<WorkflowContext> loadContext(String executionId) {
        String key = buildContextKey(executionId);
        return redisTemplate.opsForValue().get(key)
            .flatMap(this::deserializeContext)
            .flatMap(context -> loadAndUpdateStatus(context, executionId))
            .doOnSuccess(ctx -> log.debug("加载执行上下文: {}", executionId))
            .doOnError(e -> log.error("加载执行上下文失败: {}", executionId, e));
    }

    /**
     * 加载状态并更新到context。
     */
    private Mono<WorkflowContext> loadAndUpdateStatus(WorkflowContext context, String executionId) {
        String statusKey = buildStatusKey(executionId);
        return redisTemplate.opsForValue().get(statusKey)
            .map(statusStr -> updateContextStatus(context, statusStr))
            .onErrorResume(e -> handleStatusLoadError(context, executionId, e))
            .defaultIfEmpty(context);
    }

    /**
     * 更新context的状态。
     */
    private WorkflowContext updateContextStatus(WorkflowContext context, String statusStr) {
        if (statusStr != null && !statusStr.isEmpty()) {
            try {
                context.setStatus(WorkflowStatus.valueOf(statusStr));
            } catch (Exception e) {
                log.warn("Failed to parse status: {}", statusStr);
            }
        }
        return context;
    }

    /**
     * 处理状态加载错误。
     */
    private Mono<WorkflowContext> handleStatusLoadError(WorkflowContext context, String executionId, Throwable e) {
        log.warn("Failed to load status for execution: {}", executionId, e);
        return Mono.just(context);
    }

    /**
     * 保存节点执行结果。
     *
     * @param executionId 执行ID
     * @param result 节点结果
     * @return 保存结果的Mono
     */
    public Mono<Void> saveNodeResult(String executionId, NodeResult result) {
        String key = buildNodeResultKey(executionId, result.getNodeId());
        return serializeNodeResult(result)
            .flatMap(json -> saveToRedis(key, json))
            .doOnSuccess(v -> log.debug("保存节点结果: {} -> {}", executionId, result.getNodeId()));
    }

    /**
     * 加载节点执行结果。
     *
     * @param executionId 执行ID
     * @param nodeId 节点ID
     * @return 节点结果的Mono
     */
    public Mono<NodeResult> loadNodeResult(String executionId, String nodeId) {
        String key = buildNodeResultKey(executionId, nodeId);
        return redisTemplate.opsForValue().get(key)
            .flatMap(this::deserializeNodeResult)
            .doOnSuccess(result -> log.debug("加载节点结果: {} -> {}", executionId, nodeId));
    }

    /**
     * 更新工作流状态。
     *
     * @param executionId 执行ID
     * @param status 新状态
     * @return 更新结果的Mono
     */
    public Mono<Void> updateStatus(String executionId, WorkflowStatus status) {
        String key = buildStatusKey(executionId);
        return redisTemplate.opsForValue().set(key, status.name(), STATE_TTL)
            .then()
            .doOnSuccess(v -> log.debug("更新工作流状态: {} -> {}", executionId, status));
    }

    /**
     * 删除执行状态。
     *
     * @param executionId 执行ID
     * @return 删除结果的Mono
     */
    public Mono<Void> deleteState(String executionId) {
        String contextKey = buildContextKey(executionId);
        String statusKey = buildStatusKey(executionId);
        return redisTemplate.delete(contextKey, statusKey)
            .then()
            .doOnSuccess(v -> log.debug("删除执行状态: {}", executionId));
    }

    /**
     * 更新节点状态。
     *
     * @param executionId 执行ID
     * @param nodeId 节点ID
     * @param status 节点状态
     * @return 更新结果的Mono
     */
    public Mono<Void> updateNodeStatus(String executionId, String nodeId, 
                                       com.agenthub.domain.enums.workflow.NodeStatus status) {
        String key = buildNodeResultKey(executionId, nodeId) + ":status";
        return redisTemplate.opsForValue().set(key, status.name(), STATE_TTL)
            .then()
            .doOnSuccess(v -> log.debug("更新节点状态: {} -> {} = {}", executionId, nodeId, status));
    }

    /**
     * 按工作流ID查询执行上下文列表。
     *
     * @param workflowId 工作流ID
     * @param limit 最大返回数量
     * @return 执行上下文列表
     */
    public Flux<WorkflowContext> listContexts(String workflowId, int limit) {
        String historyKey = buildHistoryKey(workflowId);
        return redisTemplate.opsForList().range(historyKey, 0, limit - 1)
            .flatMap(executionId -> loadContext(executionId)
                .onErrorResume(e -> {
                    log.warn("加载历史执行上下文失败: {}", executionId, e);
                    return Mono.empty();
                }))
            .doOnComplete(() -> log.debug("查询执行历史: {} limit={}", workflowId, limit));
    }

    /**
     * 构建上下文键。
     *
     * @param executionId 执行ID
     * @return Redis键
     */
    private String buildContextKey(String executionId) {
        return KEY_PREFIX + executionId + ":context";
    }

    /**
     * 构建历史记录键。
     *
     * @param workflowId 工作流ID
     * @return Redis键
     */
    private String buildHistoryKey(String workflowId) {
        return HISTORY_KEY_PREFIX + workflowId;
    }

    /**
     * 构建节点结果键。
     *
     * @param executionId 执行ID
     * @param nodeId 节点ID
     * @return Redis键
     */
    private String buildNodeResultKey(String executionId, String nodeId) {
        return KEY_PREFIX + executionId + ":node:" + nodeId;
    }

    /**
     * 构建状态键。
     *
     * @param executionId 执行ID
     * @return Redis键
     */
    private String buildStatusKey(String executionId) {
        return KEY_PREFIX + executionId + ":status";
    }

    /**
     * 序列化执行上下文。
     *
     * @param context 执行上下文
     * @return JSON字符串的Mono
     */
    private Mono<String> serializeContext(WorkflowContext context) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(context));
    }

    /**
     * 反序列化执行上下文。
     *
     * @param json JSON字符串
     * @return 执行上下文的Mono
     */
    private Mono<WorkflowContext> deserializeContext(String json) {
        return Mono.fromCallable(() -> objectMapper.readValue(json, WorkflowContext.class));
    }

    /**
     * 序列化节点结果。
     *
     * @param result 节点结果
     * @return JSON字符串的Mono
     */
    private Mono<String> serializeNodeResult(NodeResult result) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(result));
    }

    /**
     * 反序列化节点结果。
     *
     * @param json JSON字符串
     * @return 节点结果的Mono
     */
    private Mono<NodeResult> deserializeNodeResult(String json) {
        return Mono.fromCallable(() -> objectMapper.readValue(json, NodeResult.class));
    }

    /**
     * 保存到Redis。
     *
     * @param key 键
     * @param value 值
     * @return 保存结果的Mono
     */
    private Mono<Void> saveToRedis(String key, String value) {
        return redisTemplate.opsForValue().set(key, value, STATE_TTL).then();
    }
}
