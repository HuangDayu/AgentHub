package com.agenthub.domain.model.strategy;

import com.agenthub.domain.model.agent.AgentMessage;
import com.agenthub.domain.model.agent.ReActAgentContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

import static com.agenthub.common.utils.RandomUtils.randomId;

/**
 * 模型策略聚合根。
 * <p>
 * 控制 LLM 推理参数，并在推理前后执行生命周期钩子。
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModelStrategy {
    private String id;
    private String tenantId;
    private String workspaceId;
    private String name;
    private String description;
    private double temperature;
    private int maxTokens = 9899;
    private int maxMessages = 100;
    private double topP;
    private int topK;
    private double frequencyPenalty;
    private double presencePenalty;
    private Instant createdAt;
    private Instant updatedAt;

    private ModelStrategy(String id, String workspaceId, Instant createdAt) {
        this.id = id;
        this.tenantId = null;
        this.workspaceId = workspaceId;
        this.createdAt = createdAt;
        this.temperature = 0.7;
        this.maxTokens = 2048;
        this.topP = 1.0;
        this.frequencyPenalty = 0.0;
        this.presencePenalty = 0.0;
    }

    /**
     * 创建模型策略实例。
     *
     * @param workspaceId 工作空间ID
     * @param name 策略名称
     * @return 策略实例
     */
    public static ModelStrategy create(String workspaceId, String name) {
        ModelStrategy strategy = new ModelStrategy(randomId(), workspaceId, Instant.now());
        strategy.name = name;
        strategy.updatedAt = strategy.createdAt;
        return strategy;
    }

    /**
     * 持久化状态快照，用于在 rebuild 时一次性传入所有字段。
     */
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class State {
        private String id;
        private String workspaceId;
        private String name;
        private String description;
        private Double temperature;
        private Integer maxTokens;
        private Integer maxMessages;
        private Double topP;
        private Integer topK;
        private Double frequencyPenalty;
        private Double presencePenalty;
        private Instant createdAt;
        private Instant updatedAt;
    }

    /**
     * 从持久化层重建对象。
     */
    public static ModelStrategy rebuild(State state) {
        ModelStrategy strategy = new ModelStrategy(state.getId(), state.getWorkspaceId(), state.getCreatedAt());
        applyState(strategy, state);
        return strategy;
    }

    private static void applyState(ModelStrategy strategy, State state) {
        strategy.name = state.getName();
        strategy.description = state.getDescription();
        applyParams(strategy, state);
        strategy.updatedAt = state.getUpdatedAt();
    }

    /** 应用推理参数字段。 */
    private static void applyParams(ModelStrategy strategy, State state) {
        strategy.temperature = state.getTemperature() != null ? state.getTemperature() : 0.7;
        strategy.maxTokens = state.getMaxTokens() != null ? state.getMaxTokens() : 2048;
        strategy.maxMessages = state.getMaxMessages() != null ? state.getMaxMessages() : 100;
        strategy.topP = state.getTopP() != null ? state.getTopP() : 1.0;
        strategy.topK = state.getTopK() != null ? state.getTopK() : 0;
        strategy.frequencyPenalty = state.getFrequencyPenalty() != null ? state.getFrequencyPenalty() : 0.0;
        strategy.presencePenalty = state.getPresencePenalty() != null ? state.getPresencePenalty() : 0.0;
    }

    /**
     * 推理前生命周期钩子。
     *
     * @param context Agent 上下文
     * @param messages 输入消息列表
     */
    public void beforeInference(ReActAgentContext context,
                                List<AgentMessage> messages) {
        // 可用于动态调整参数、日志记录
    }

    /**
     * 推理后生命周期钩子。
     *
     * @param context Agent 上下文
     * @param messages 输入消息列表
     * @param response 模型响应
     * @return 处理后的响应
     */
    public AgentMessage afterInference(ReActAgentContext context,
                                       List<AgentMessage> messages,
                                       AgentMessage response) {
        return response;
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
     * 配置推理参数。
     *
     * @param temperature 温度
     * @param maxTokens 最大token数
     * @param topP 核采样参数
     */
    public void configureParameters(double temperature, int maxTokens, double topP) {
        this.temperature = temperature;
        this.maxTokens = maxTokens;
        this.topP = topP;
        this.updatedAt = Instant.now();
    }

    /**
     * 设置惩罚参数。
     *
     * @param frequencyPenalty 频率惩罚
     * @param presencePenalty 存在惩罚
     */
    public void setPenalties(double frequencyPenalty, double presencePenalty) {
        this.frequencyPenalty = frequencyPenalty;
        this.presencePenalty = presencePenalty;
        this.updatedAt = Instant.now();
    }
}
