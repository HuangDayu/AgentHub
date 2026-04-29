package com.agenthub.domain.model;

/**
 * 模型调用任务类型枚举。
 * <p>
 * 用于区分不同场景的模型请求，以匹配最适合的模型。
 * </p>
 */
public enum TaskType {

    /**
     * 智能对话
     */
    CHAT,

    /**
     * 文本嵌入
     */
    EMBEDDING,

    /**
     * 图像生成
     */
    IMAGE_GENERATION,

    /**
     * 文本总结
     */
    SUMMARIZATION,

    /**
     * 代码生成
     */
    CODE_GENERATION,

    /**
     * RAG 检索增强生成
     */
    RAG,

    /**
     * 向量搜索
     */
    VECTOR_SEARCH
}
