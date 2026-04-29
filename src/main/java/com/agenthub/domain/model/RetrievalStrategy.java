package com.agenthub.domain.model;

import java.time.Instant;

import static com.agenthub.common.utils.RandomUtils.randomId;

/**
 * 检索策略聚合根。
 */
public class RetrievalStrategy {
    private final String id;
    private final String tenantId;
    private final String workspaceId;
    private String name;
    private String description;
    private RetrievalType retrievalType;
    private int topK;
    private double scoreThreshold;
    private boolean enableRerank;
    private boolean enableQueryRewrite;
    private boolean enableTextSearch;
    private boolean enableVectorSearch;
    private String rerankModel;
    private double vectorWeight;
    private double keywordWeight;
    private final Instant createdAt;
    private Instant updatedAt;

    public enum RetrievalType {
        HYBRID,
        SEMANTIC,
        KEYWORD
    }

    private RetrievalStrategy(String id, String workspaceId, Instant createdAt) {
        this.id = id;
        this.tenantId = null; // tenantId由MyBatis-Plus拦截器自动填充
        this.workspaceId = workspaceId;
        this.createdAt = createdAt;
        this.topK = 10;
        this.scoreThreshold = 0.75;
        this.vectorWeight = 0.7;
        this.keywordWeight = 0.3;
    }

    public static RetrievalStrategy create(String workspaceId, String name) {
        RetrievalStrategy strategy = new RetrievalStrategy(randomId(), workspaceId, Instant.now());
        strategy.name = name;
        strategy.retrievalType = RetrievalType.HYBRID;
        strategy.updatedAt = strategy.createdAt;
        return strategy;
    }

    /**
     * 从持久化层重建对象（保留所有原始数据）。
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

    public void updateBasicInfo(String name, String description) {
        this.name = name;
        this.description = description;
        this.updatedAt = Instant.now();
    }

    public void configureRetrieval(RetrievalType type, int topK, double threshold) {
        this.retrievalType = type;
        this.topK = topK;
        this.scoreThreshold = threshold;
        this.updatedAt = Instant.now();
    }

    public void enableRerank(String model) {
        this.enableRerank = true;
        this.rerankModel = model;
        this.updatedAt = Instant.now();
    }

    public void disableRerank() {
        this.enableRerank = false;
        this.rerankModel = null;
        this.updatedAt = Instant.now();
    }

    public void setWeights(double vectorWeight, double keywordWeight) {
        this.vectorWeight = vectorWeight;
        this.keywordWeight = keywordWeight;
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

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RetrievalType getRetrievalType() {
        return retrievalType;
    }

    public void setRetrievalType(RetrievalType retrievalType) {
        this.retrievalType = retrievalType;
    }

    public int getTopK() {
        return topK;
    }

    public void setTopK(int topK) {
        this.topK = topK;
    }

    public double getScoreThreshold() {
        return scoreThreshold;
    }

    public void setScoreThreshold(double scoreThreshold) {
        this.scoreThreshold = scoreThreshold;
    }

    public boolean isEnableRerank() {
        return enableRerank;
    }

    public void setEnableRerank(boolean enableRerank) {
        this.enableRerank = enableRerank;
    }

    public boolean isEnableQueryRewrite() {
        return enableQueryRewrite;
    }

    public void setEnableQueryRewrite(boolean enableQueryRewrite) {
        this.enableQueryRewrite = enableQueryRewrite;
    }

    public boolean isEnableTextSearch() {
        return enableTextSearch;
    }

    public void setEnableTextSearch(boolean enableTextSearch) {
        this.enableTextSearch = enableTextSearch;
    }

    public boolean isEnableVectorSearch() {
        return enableVectorSearch;
    }

    public void setEnableVectorSearch(boolean enableVectorSearch) {
        this.enableVectorSearch = enableVectorSearch;
    }

    public String getRerankModel() {
        return rerankModel;
    }

    public void setRerankModel(String rerankModel) {
        this.rerankModel = rerankModel;
    }

    public double getVectorWeight() {
        return vectorWeight;
    }

    public void setVectorWeight(double vectorWeight) {
        this.vectorWeight = vectorWeight;
    }

    public double getKeywordWeight() {
        return keywordWeight;
    }

    public void setKeywordWeight(double keywordWeight) {
        this.keywordWeight = keywordWeight;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
