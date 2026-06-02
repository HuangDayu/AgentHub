package com.agenthub.domain.model.strategy;

import com.agenthub.domain.model.agent.ReActAgentContext;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

import static com.agenthub.common.utils.RandomUtils.randomId;

/**
 * 检索策略聚合根。
 * <p>
 * 控制 RAG 知识检索行为，包括查询改写、重排序、分数过滤等。
 * </p>
 */
@NoArgsConstructor
@Data
public class RetrievalStrategy {
    private String id;
    private String tenantId;
    private String workspaceId;
    private String name;
    private String description;
    private RetrievalType retrievalType;
    private int topK;
    private double scoreThreshold;
    private boolean enableTranslationQuery;
    private boolean enableCompressionQuery;
    private boolean enableRerank;
    private boolean enableQueryRewrite;
    private boolean enableTextSearch;
    private boolean enableVectorSearch;
    private String rerankModel;
    private double vectorWeight;
    private double keywordWeight;
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * 检索类型枚举。
     */
    public enum RetrievalType {
        HYBRID,
        SEMANTIC,
        KEYWORD
    }

    private RetrievalStrategy(String id, String workspaceId, Instant createdAt) {
        this.id = id;
        this.tenantId = null;
        this.workspaceId = workspaceId;
        this.createdAt = createdAt;
        this.topK = 10;
        this.scoreThreshold = 0.75;
        this.vectorWeight = 0.7;
        this.keywordWeight = 0.3;
    }

    /**
     * 创建检索策略实例。
     *
     * @param workspaceId 工作空间ID
     * @param name 策略名称
     * @return 策略实例
     */
    public static RetrievalStrategy create(String workspaceId, String name) {
        RetrievalStrategy strategy = new RetrievalStrategy(randomId(), workspaceId, Instant.now());
        strategy.name = name;
        strategy.retrievalType = RetrievalType.HYBRID;
        strategy.updatedAt = strategy.createdAt;
        return strategy;
    }

    /**
     * 从持久化层重建对象。
     */
    public static RetrievalStrategy rebuild(
            String id, String workspaceId, String name, String description,
            RetrievalType retrievalType, int topK, double scoreThreshold,
            boolean enableQueryRewrite, boolean enableTextSearch, boolean enableVectorSearch,
            boolean enableRerank, String rerankModel,
            double vectorWeight, double keywordWeight,
            Instant createdAt, Instant updatedAt) {
        RetrievalStrategy strategy = new RetrievalStrategy(id, workspaceId, createdAt);
        strategy.name = name;
        strategy.description = description;
        strategy.retrievalType = retrievalType;
        strategy.topK = topK;
        strategy.scoreThreshold = scoreThreshold;
        strategy.enableQueryRewrite = enableQueryRewrite;
        strategy.enableTextSearch = enableTextSearch;
        strategy.enableVectorSearch = enableVectorSearch;
        strategy.enableRerank = enableRerank;
        strategy.rerankModel = rerankModel;
        strategy.vectorWeight = vectorWeight;
        strategy.keywordWeight = keywordWeight;
        strategy.updatedAt = updatedAt;
        return strategy;
    }

    /**
     * 检索前生命周期钩子。
     *
     * @param context Agent 上下文
     * @param query 原始查询
     * @return 处理后的查询
     */
    public String beforeRetrieval(ReActAgentContext context, String query) {
        if (!enableQueryRewrite) return query;
        return query;
    }

    /**
     * 检索后生命周期钩子。
     *
     * @param context Agent 上下文
     * @param query 查询
     * @param results 检索结果
     * @return 处理后的结果
     */
    public List<?> afterRetrieval(ReActAgentContext context,
                                  String query,
                                  List<?> results) {
        return results;
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
     * 配置检索参数。
     *
     * @param type 检索类型
     * @param topK 返回数量
     * @param threshold 分数阈值
     */
    public void configureRetrieval(RetrievalType type, int topK, double threshold) {
        this.retrievalType = type;
        this.topK = topK;
        this.scoreThreshold = threshold;
        this.updatedAt = Instant.now();
    }

    /**
     * 启用重排序。
     *
     * @param model 重排序模型名称
     */
    public void enableRerank(String model) {
        this.enableRerank = true;
        this.rerankModel = model;
        this.updatedAt = Instant.now();
    }

    /** 禁用重排序。 */
    public void disableRerank() {
        this.enableRerank = false;
        this.rerankModel = null;
        this.updatedAt = Instant.now();
    }

    /**
     * 设置权重。
     *
     * @param vectorWeight 向量权重
     * @param keywordWeight 关键词权重
     */
    public void setWeights(double vectorWeight, double keywordWeight) {
        this.vectorWeight = vectorWeight;
        this.keywordWeight = keywordWeight;
        this.updatedAt = Instant.now();
    }
}
