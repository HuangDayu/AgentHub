package com.agenthub.infrastructure.vector;

import com.agenthub.domain.model.VectorStoreConfig;
import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.schema.model.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.weaviate.WeaviateVectorStore;
import org.springframework.ai.vectorstore.weaviate.WeaviateVectorStoreOptions;
import org.springframework.stereotype.Component;

/**
 * Weaviate 向量库工厂。
 */
@Component
public class WeaviateVectorStoreFactory implements VectorStoreFactory {
    
    private static final Logger log = LoggerFactory.getLogger(WeaviateVectorStoreFactory.class);

    @Override
    public String getType() {
        return "WEAVIATE";
    }

    /**
     * 创建Weaviate向量存储实例。
     *
     * @param config         向量库配置
     * @param embeddingModel 嵌入模型
     * @return VectorStore实例
     */
    @Override
    public VectorStore create(VectorStoreConfig config, EmbeddingModel embeddingModel) {
        WeaviateClient client = createWeaviateClient(config);
        WeaviateVectorStoreOptions options = createOptions(config);
        return WeaviateVectorStore.builder(client, embeddingModel)
                .options(options)
                .build();
    }

    /**
     * 创建Weaviate客户端。
     */
    private WeaviateClient createWeaviateClient(VectorStoreConfig config) {
        Config weaviateConfig = new Config("http", config.host() + ":" + config.port());
        return new WeaviateClient(weaviateConfig);
    }

    /**
     * 创建向量存储选项。
     */
    private WeaviateVectorStoreOptions createOptions(VectorStoreConfig config) {
        WeaviateVectorStoreOptions options = new WeaviateVectorStoreOptions();
        options.setObjectClass(config.collectionName());
        return options;
    }

    /**
     * 测试Weaviate连接。
     *
     * @param config 向量库配置
     * @return 测试结果
     */
    @Override
    public VectorStoreTestResult testConnection(VectorStoreConfig config) {
        try {
            log.info("Testing Weaviate connection: {}:{}", config.host(), config.port());
            WeaviateClient client = createWeaviateClient(config);
            return performConnectionTest(client, config);
        } catch (Exception e) {
            log.error("Weaviate connection test failed: {}", e.getMessage(), e);
            return buildFailureResult(e);
        }
    }

    /**
     * 执行连接测试。
     */
    private VectorStoreTestResult performConnectionTest(WeaviateClient client, VectorStoreConfig config) {
        Result<Schema> result = client.schema().getter().run();
        if (result.hasErrors()) {
            throw new RuntimeException("Weaviate connection test failed: " + result.getError().getMessages());
        }
        int classCount = calculateClassCount(result.getResult());
        log.info("Weaviate connection test successful, found {} classes", classCount);
        return new VectorStoreTestResult(true, "连接成功",
                String.format("地址: %s:%d, 类数: %d", config.host(), config.port(), classCount));
    }

    /**
     * 计算Schema中的类数量。
     */
    private int calculateClassCount(Schema schema) {
        return schema != null && schema.getClasses() != null ? schema.getClasses().size() : 0;
    }

    /**
     * 构建失败结果。
     */
    private VectorStoreTestResult buildFailureResult(Exception e) {
        return new VectorStoreTestResult(false, "连接失败: " + e.getMessage(),
                e.getClass().getSimpleName());
    }
}
