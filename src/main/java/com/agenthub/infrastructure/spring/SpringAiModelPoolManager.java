package com.agenthub.infrastructure.spring;

import com.agenthub.application.dto.ModelTestOutput;
import com.agenthub.application.port.out.ModelPoolManagerPort;
import com.agenthub.domain.model.ModelConfig;
import com.agenthub.application.port.out.repositories.ModelConfigRepository;
import com.agenthub.domain.model.ModelType;
import com.agenthub.infrastructure.spring.model.ModelFactoryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 动态模型管理器。
 * 负责创建和缓存模型实例。
 */
@Component
public class SpringAiModelPoolManager implements ModelPoolManagerPort {
    private static final Logger log = LoggerFactory.getLogger(SpringAiModelPoolManager.class);
    private final Map<String, Object> modelInstanceCache = new ConcurrentHashMap<>();
    private final ModelConfigRepository modelConfigRepository;
    private final ModelFactoryRegistry modelFactoryRegistry;

    public SpringAiModelPoolManager(ModelConfigRepository modelConfigRepository, ModelFactoryRegistry modelFactoryRegistry) {
        this.modelConfigRepository = modelConfigRepository;
        this.modelFactoryRegistry = modelFactoryRegistry;
    }

    public ChatModel getOrCreateChatModel(String configId) {
        return getCachedOrCreate(configId, () -> {
            ModelConfig config = modelConfigRepository.findById(configId)
                    .orElseThrow(() -> new IllegalArgumentException("Model config not found: id=" + configId));
            if (!config.enabled()) {
                throw new IllegalStateException("Model config is disabled: id=" + configId);
            }
            return modelFactoryRegistry.createChatModel(config);
        });
    }

    public EmbeddingModel getOrCreateEmbeddingModel(String configId) {
        return getCachedOrCreate(configId, () -> {
            ModelConfig config = modelConfigRepository.findById(configId)
                    .orElseThrow(() -> new IllegalArgumentException("Model config not found: id=" + configId));
            if (!config.enabled()) {
                throw new IllegalStateException("Model config is disabled: id=" + configId);
            }
            return modelFactoryRegistry.createEmbeddingModel(config);
        });
    }

    @SuppressWarnings("unchecked")
    private <T> T getCachedOrCreate(String configId, java.util.function.Supplier<T> creator) {
        return (T) modelInstanceCache.computeIfAbsent(configId, k -> creator.get());
    }

    @Override
    public void evictCache(String configId) {
        modelInstanceCache.remove(configId);
    }

    @Override
    public void evictAllCache() {
        modelInstanceCache.clear();
    }


    /**
     * 测试模型配置。
     */
    @Override
    public ModelTestOutput testModel(String configId) {
        ModelConfig config = findExistingConfig(configId);
        try {
            log.info("Testing model config: id={}, type={}, supplier={}", configId, config.type(), config.supplier());
            return doTestModel(config);
        } catch (Exception e) {
            log.error("Model test failed for config {}: {}", configId, e.getMessage(), e);
            return new ModelTestOutput(false, "测试失败: " + e.getMessage(), e.getClass().getSimpleName());
        }
    }

    /**
     * 执行模型测试。
     */
    private ModelTestOutput doTestModel(ModelConfig config) {
        if (config.type() == ModelType.CHAT) {
            return testChatModel(config);
        } else if (config.type() == ModelType.EMBEDDING) {
            return testEmbeddingModel(config);
        }
        return new ModelTestOutput(false, "未知的模型类型: " + config.type(), null);
    }

    /**
     * 测试对话模型。
     */
    private ModelTestOutput testChatModel(ModelConfig config) {
        try {
            var chatModel = getOrCreateChatModel(config.id());
            var prompt = new org.springframework.ai.chat.prompt.Prompt("Hello");
            var response = chatModel.call(prompt);
            String result = response.getResult().getOutput().toString();
            log.info("Chat model test successful for config {}", config.id());
            return buildChatTestResult(config, result);
        } catch (Exception e) {
            log.error("Chat model test failed: {}", e.getMessage(), e);
            return new ModelTestOutput(false, "对话模型测试失败: " + e.getMessage(), e.getClass().getSimpleName());
        }
    }

    /**
     * 构建对话测试结果。
     */
    private ModelTestOutput buildChatTestResult(ModelConfig config, String result) {
        String truncated = result.substring(0, Math.min(50, result.length()));
        String details = String.format("供应商: %s, 模型: %s, 响应: %s", config.supplier(), config.model(), truncated);
        return new ModelTestOutput(true, "对话模型测试成功", details);
    }

    /**
     * 测试嵌入模型。
     */
    private ModelTestOutput testEmbeddingModel(ModelConfig config) {
        try {
            var embeddingModel = getOrCreateEmbeddingModel(config.id());
            var response = embeddingModel.embed("test");
            log.info("Embedding model test successful for config {}, dimension={}", config.id(), response.length);
            return buildEmbeddingTestResult(config, response.length);
        } catch (Exception e) {
            log.error("Embedding model test failed: {}", e.getMessage(), e);
            return new ModelTestOutput(false, "嵌入模型测试失败: " + e.getMessage(), e.getClass().getSimpleName());
        }
    }

    /**
     * 构建嵌入测试结果。
     */
    private ModelTestOutput buildEmbeddingTestResult(ModelConfig config, int dimension) {
        String details = String.format("供应商: %s, 模型: %s, 向量维度: %d", config.supplier(), config.model(), dimension);
        return new ModelTestOutput(true, "嵌入模型测试成功", details);
    }

    /**
     * 查找现有配置。
     */
    private ModelConfig findExistingConfig(String id) {
        return modelConfigRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Model config not found: id=" + id));
    }
}
