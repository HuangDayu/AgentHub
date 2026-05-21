package com.agenthub.infrastructure.model;

import com.agenthub.domain.enums.ModelSupplier;
import com.agenthub.domain.model.KnowledgeBase;
import com.agenthub.domain.model.ModelConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 模型工厂注册表。
 * <p>
 * 自动发现所有 ModelFactory 实现，按供应商注册，
 * 提供根据 supplier+type 获取对应工厂并按配置创建模型实例的能力。
 * </p>
 */
@Component
public class ModelFactoryRegistry {

    private static final Logger log = LoggerFactory.getLogger(ModelFactoryRegistry.class);

    private final Map<ModelSupplier, ModelFactory> factoriesBySupplier;

    /**
     * Spring 自动注入所有 ModelFactory Bean。
     */
    public ModelFactoryRegistry(List<ModelFactory> factories) {
        this.factoriesBySupplier = factories.stream()
                .collect(Collectors.toMap(ModelFactory::getSupplier, Function.identity()));
        log.info("Registered model factories: {}", factoriesBySupplier.keySet());
    }

    /**
     * 获取指定供应商的模型工厂。
     *
     * @param supplier 供应商标识
     * @return 对应的 ModelFactory
     * @throws IllegalArgumentException 如果供应商未注册
     */
    public ModelFactory getFactory(ModelSupplier supplier) {
        ModelFactory factory = factoriesBySupplier.get(supplier);
        if (factory == null) {
            throw new IllegalArgumentException("Unsupported supplier: " + supplier
                    + ". Supported: " + factoriesBySupplier.keySet());
        }
        return factory;
    }

    /**
     * 根据配置自动创建 ChatModel。
     *
     * @param config 模型配置
     * @return Spring AI ChatModel 实例
     */
    public ChatModel createChatModel(ModelConfig config) {
        ModelFactory factory = getFactory(config.getSupplier());
        return factory.createChatModel(config);
    }

    /**
     * 根据配置自动创建 EmbeddingModel。
     *
     * @param config 模型配置
     * @return Spring AI EmbeddingModel 实例
     */
    public EmbeddingModel createEmbeddingModel(ModelConfig config) {
        ModelFactory factory = getFactory(config.getSupplier());
        return factory.createEmbeddingModel(config);
    }

    /**
     * 判断是否支持指定供应商。
     */
    public boolean isSupported(ModelSupplier supplier) {
        return factoriesBySupplier.containsKey(supplier);
    }
}