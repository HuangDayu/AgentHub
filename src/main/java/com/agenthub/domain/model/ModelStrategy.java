package com.agenthub.domain.model;

import java.time.Instant;

import static com.agenthub.common.utils.RandomUtils.randomId;

/**
 * 模型策略聚合根。
 */
public class ModelStrategy {
    private final String id;
    private final String tenantId;
    private final String workspaceId;
    private String name;
    private String description;
    private double temperature;
    private int maxTokens = 9899;
    private int maxMessages = 100;
    private double topP;
    private double frequencyPenalty;
    private double presencePenalty;
    private final Instant createdAt;
    private Instant updatedAt;

    private ModelStrategy(String id, String workspaceId, Instant createdAt) {
        this.id = id;
        this.tenantId = null; // tenantId由MyBatis-Plus拦截器自动填充
        this.workspaceId = workspaceId;
        this.createdAt = createdAt;
        this.temperature = 0.7;
        this.maxTokens = 2048;
        this.topP = 1.0;
        this.frequencyPenalty = 0.0;
        this.presencePenalty = 0.0;
    }

    public static ModelStrategy create(String workspaceId, String name) {
        ModelStrategy strategy = new ModelStrategy(randomId(), workspaceId, Instant.now());
        strategy.name = name;
        strategy.updatedAt = strategy.createdAt;
        return strategy;
    }

    /**
     * 从持久化层重建对象（保留所有原始数据）。
     */
    public static ModelStrategy rebuild(
            String id, String workspaceId, String name, String description,
            double temperature, int maxTokens, double topP,
            double frequencyPenalty, double presencePenalty,
            Instant createdAt, Instant updatedAt) {
        ModelStrategy strategy = new ModelStrategy(id, workspaceId, createdAt);
        strategy.name = name;
        strategy.description = description;
        strategy.temperature = temperature;
        strategy.maxTokens = maxTokens;
        strategy.topP = topP;
        strategy.frequencyPenalty = frequencyPenalty;
        strategy.presencePenalty = presencePenalty;
        strategy.updatedAt = updatedAt;
        return strategy;
    }

    public void updateBasicInfo(String name, String description) {
        this.name = name;
        this.description = description;
        this.updatedAt = Instant.now();
    }

    public void configureParameters(double temperature, int maxTokens, double topP) {
        this.temperature = temperature;
        this.maxTokens = maxTokens;
        this.topP = topP;
        this.updatedAt = Instant.now();
    }

    public void setPenalties(double frequencyPenalty, double presencePenalty) {
        this.frequencyPenalty = frequencyPenalty;
        this.presencePenalty = presencePenalty;
        this.updatedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }



    public double getTemperature() {
        return temperature;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public double getTopP() {
        return topP;
    }

    public double getFrequencyPenalty() {
        return frequencyPenalty;
    }

    public double getPresencePenalty() {
        return presencePenalty;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public int getMaxMessages() {
        return maxMessages;
    }

    public void setMaxMessages(int maxMessages) {
        this.maxMessages = maxMessages;
    }

    public void setTopP(double topP) {
        this.topP = topP;
    }

    public void setFrequencyPenalty(double frequencyPenalty) {
        this.frequencyPenalty = frequencyPenalty;
    }

    public void setPresencePenalty(double presencePenalty) {
        this.presencePenalty = presencePenalty;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
