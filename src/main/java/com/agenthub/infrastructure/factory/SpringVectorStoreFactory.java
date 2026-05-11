package com.agenthub.infrastructure.factory;

import com.agenthub.application.dto.VectorStoreTestOutput;
import com.agenthub.application.port.out.VectorPoolManagerPort;
import com.agenthub.application.port.out.repositories.VectorStoreConfigRepository;
import com.agenthub.common.exception.NotFoundException;
import com.agenthub.domain.model.VectorStoreConfig;
import com.agenthub.infrastructure.vector.VectorStoreFactory;
import com.agenthub.infrastructure.vector.VectorStoreFactoryRegistry;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 动态向量存储管理器。
 * <p>
 * 负责根据 VectorStoreConfig 动态创建和缓存 VectorStore 实例，
 * 支持实例的生命周期管理（创建、获取、销毁、刷新）。
 * 缓存 Key 为 "tenantId:configId" 格式。
 * </p>
 */
@Service
public class SpringVectorStoreFactory implements VectorPoolManagerPort {

    private static final Logger log = LoggerFactory.getLogger(SpringVectorStoreFactory.class);

    private final VectorStoreFactoryRegistry vectorStoreFactoryRegistry;
    private final VectorStoreConfigRepository vectorStoreConfigRepository;
    private final Map<String, VectorStoreAndModel> instanceCache = new ConcurrentHashMap<>();

    /**
     * 构造动态向量存储管理器。
     */
    public SpringVectorStoreFactory(VectorStoreFactoryRegistry vectorStoreFactoryRegistry, VectorStoreConfigRepository vectorStoreConfigRepository) {
        this.vectorStoreFactoryRegistry = vectorStoreFactoryRegistry;
        this.vectorStoreConfigRepository = vectorStoreConfigRepository;
    }

    /**
     * 根据配置 ID 获取或创建向量存储实例。
     */
    public VectorStore getOrCreate(String vectorStoreConfigId, EmbeddingModel embeddingModel) {
        Optional<VectorStoreConfig> configOptional = vectorStoreConfigRepository.findById(vectorStoreConfigId);
        if (configOptional.isEmpty()) {
            throw new NotFoundException("VectorStore config not found");
        }
        return getOrCreate(configOptional.get(), embeddingModel);
    }

    /**
     * 根据配置创建或获取缓存的 VectorStore 实例。
     *
     * @param config 向量库配置
     * @return VectorStore 实例
     */
    public VectorStore getOrCreate(VectorStoreConfig config, EmbeddingModel embeddingModel) {
        String cacheKey = buildCacheKey(config);
        return instanceCache.computeIfAbsent(cacheKey, key -> {
            log.info("Creating new VectorStore instance for config={}, type={}", config.getId(), config.getType());
            VectorStoreFactory factory = vectorStoreFactoryRegistry.getFactory(config.getType());
            VectorStore vectorStore = factory.create(config, embeddingModel);
            return new VectorStoreAndModel(vectorStore, embeddingModel);
        }).getVectorStore();
    }

    /**
     * 强制刷新指定配置的 VectorStore 实例。
     * 先销毁旧实例，再创建新实例。
     *
     * @param config 向量库配置
     * @return 新创建的 VectorStore 实例
     */
    public void refresh(VectorStoreConfig config) {
        String cacheKey = buildCacheKey(config);
        VectorStoreAndModel vectorStore = instanceCache.get(cacheKey);
        if (vectorStore == null) {
            throw new NotFoundException("VectorStore instance not found");
        }
        destroy(cacheKey);
        getOrCreate(config, vectorStore.getEmbeddingModel());
    }

    /**
     * 销毁指定配置的 VectorStore 实例。
     *
     * @param config 向量库配置
     * @return 是否成功销毁
     */
    public boolean destroy(VectorStoreConfig config) {
        String cacheKey = buildCacheKey(config);
        return destroy(cacheKey);
    }

    /**
     * 销毁指定租户的所有 VectorStore 实例。
     *
     * @param tenantId 租户 ID
     * @return 销毁的实例数量
     */
    public int destroyAllForTenant(String tenantId) {
        String prefix = tenantId.toString() + ":";
        int count = 0;
        for (String key : instanceCache.keySet()) {
            if (key.startsWith(prefix)) {
                destroy(key);
                count++;
            }
        }
        log.info("Destroyed {} VectorStore instances for tenant={}", count, tenantId);
        return count;
    }

    /**
     * 获取缓存的实例数量。
     */
    public int cachedInstanceCount() {
        return instanceCache.size();
    }

    /**
     * 销毁指定缓存键的实例。
     */
    private boolean destroy(String cacheKey) {
        VectorStoreAndModel removed = instanceCache.remove(cacheKey);
        if (removed != null) {
            log.info("Destroyed VectorStore instance: {}", cacheKey);
        }
        return removed != null;
    }

    /**
     * 构建缓存键。
     */
    private String buildCacheKey(VectorStoreConfig config) {
        return "vectorStore" + ":" + config.getId();
    }

    /**
     * 向量存储和嵌入模型的组合记录。
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VectorStoreAndModel {
        private VectorStore vectorStore;
        private EmbeddingModel embeddingModel;
    }

    /**
     * 测试向量库连接。
     *
     * @param config 向量库配置
     * @return 测试结果
     */
    @Override
    public VectorStoreTestOutput testConnection(VectorStoreConfig config) {
        log.info("Testing vector store connection for config={}, type={}", config.getId(), config.getType());
        try {
            VectorStoreFactory factory = vectorStoreFactoryRegistry.getFactory(config.getType());
            VectorStoreFactory.VectorStoreTestResult factoryResult = factory.testConnection(config);
            log.info("Vector store connection test completed for config={}, success={}", config.getId(), factoryResult.success());
            return new VectorStoreTestOutput(factoryResult.success(), factoryResult.message(), factoryResult.details());
        } catch (Exception e) {
            log.error("Vector store connection test failed for config={}: {}", config.getId(), e.getMessage(), e);
            return new VectorStoreTestOutput(false, "连接失败: " + e.getMessage(), e.getClass().getSimpleName());
        }
    }

    /**
     * 测试向量库连接。
     *
     * @param configId 配置 ID
     * @return 测试结果
     */
    @Override
    public VectorStoreTestOutput testConnection(String configId) {
        Optional<VectorStoreConfig> configOptional = vectorStoreConfigRepository.findById(configId);
        try {
            VectorStoreConfig config = configOptional.get();
            return doTestConnection(configId, config);
        } catch (Exception e) {
            return buildFailureResult(e);
        }
    }

    /**
     * 执行连接测试。
     */
    private VectorStoreTestOutput doTestConnection(String configId, VectorStoreConfig config) {
        log.info("Testing vector store connection for config={}, type={}", configId, config.getType());
        var result = testConnection(config);
        log.info("Vector store connection test completed for config={}, success={}", configId, result.isSuccess());
        return result;
    }

    /**
     * 构建失败结果。
     */
    private VectorStoreTestOutput buildFailureResult(Exception e) {
        log.error("Vector store connection test failed: {}", e.getMessage(), e);
        return new VectorStoreTestOutput(
                false,
                "连接失败: " + e.getMessage(),
                e.getClass().getSimpleName()
        );
    }
}
