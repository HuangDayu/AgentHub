package com.agenthub.infrastructure.spring.vector;

import com.agenthub.domain.model.VectorStoreConfig;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;

/**
 * 向量库工厂接口。
 */
public interface VectorStoreFactory {

    /**
     * 当前工厂支持的类型标识（对应 VectorStoreType 的 key）。
     */
    String getType();

    /**
     * 根据配置创建 VectorStore 实例。
     */
    VectorStore create(VectorStoreConfig config, EmbeddingModel embeddingModel);

    /**
     * 测试向量库连接。
     * <p>
     * 尝试连接向量库并验证配置是否正确。
     * </p>
     *
     * @param config 向量库配置
     * @return 测试结果
     */
    default VectorStoreTestResult testConnection(VectorStoreConfig config) {
        // 默认实现：返回成功（子类可以覆盖实现真实的连接测试）
        return new VectorStoreTestResult(true, "连接测试未实现", null);
    }

    /**
     * 向量库测试结果。
     */
    record VectorStoreTestResult(
            boolean success,
            String message,
            String details
    ) {}
}
