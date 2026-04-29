package com.agenthub.infrastructure.spring.model;

import com.agenthub.domain.model.ModelConfig;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;

/**
 * 模型工厂接口，根据 ModelConfig 动态创建 Spring AI 模型实例。
 * <p>
 * 每个供应商实现此接口来创建对应的 ChatModel / EmbeddingModel / ImageModel。
 * </p>
 */
public interface ModelFactory {

    /**
     * 获取当前工厂支持的供应商。
     *
     * @return 供应商标识
     */
    String getSupplier();

    /**
     * 根据配置创建 ChatModel。
     *
     * @param config 模型配置
     * @return Spring AI ChatModel 实例
     */
    ChatModel createChatModel(ModelConfig config);

    /**
     * 根据配置创建 EmbeddingModel。
     *
     * @param config 模型配置
     * @return Spring AI EmbeddingModel 实例
     */
    EmbeddingModel createEmbeddingModel(ModelConfig config);
}