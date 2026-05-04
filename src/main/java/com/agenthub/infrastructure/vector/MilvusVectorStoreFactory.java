package com.agenthub.infrastructure.vector;

import com.agenthub.domain.model.VectorStoreConfig;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.ShowCollectionsResponse;
import io.milvus.param.ConnectParam;
import io.milvus.param.R;
import io.milvus.param.collection.ShowCollectionsParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.stereotype.Component;

/**
 * Milvus 向量库工厂。
 */
@Component
public class MilvusVectorStoreFactory implements VectorStoreFactory {
    
    private static final Logger log = LoggerFactory.getLogger(MilvusVectorStoreFactory.class);

    @Override
    public String getType() {
        return "MILVUS";
    }

    /**
     * 创建Milvus向量存储实例。
     *
     * @param config         向量库配置
     * @param embeddingModel 嵌入模型
     * @return VectorStore实例
     */
    @Override
    public VectorStore create(VectorStoreConfig config, EmbeddingModel embeddingModel) {
        MilvusServiceClient client = createClient(config);
        return MilvusVectorStore.builder(client, embeddingModel)
                .collectionName(config.collectionName())
                .initializeSchema(true)
                .build();
    }

    /**
     * 创建Milvus客户端连接。
     *
     * @param config 向量库配置
     * @return Milvus客户端
     */
    private MilvusServiceClient createClient(VectorStoreConfig config) {
        ConnectParam.Builder builder = ConnectParam.newBuilder()
                .withHost(config.host())
                .withPort(config.port());
        if (config.apiKey() != null && !config.apiKey().isBlank()) {
            builder.withToken(config.apiKey());
        }
        return new MilvusServiceClient(builder.build());
    }

    /**
     * 测试Milvus连接。
     *
     * @param config 向量库配置
     * @return 测试结果
     */
    @Override
    public VectorStoreTestResult testConnection(VectorStoreConfig config) {
        MilvusServiceClient client = null;
        try {
            log.info("Testing Milvus connection: {}:{}", config.host(), config.port());
            client = createClient(config);
            int collectionCount = getCollectionCount(client);
            log.info("Milvus connection test successful, found {} collections", collectionCount);
            return buildSuccessResult(config, collectionCount);
        } catch (Exception e) {
            log.error("Milvus connection test failed: {}", e.getMessage(), e);
            return buildFailureResult(e);
        } finally {
            closeClient(client);
        }
    }

    /**
     * 获取集合数量。
     *
     * @param client Milvus客户端
     * @return 集合数量
     */
    private int getCollectionCount(MilvusServiceClient client) {
        R<ShowCollectionsResponse> response = client.showCollections(
                ShowCollectionsParam.newBuilder().build()
        );
        if (response.getStatus() != R.Status.Success.getCode()) {
            throw new RuntimeException("Milvus connection test failed: " + response.getMessage());
        }
        return response.getData().getCollectionNamesList().size();
    }

    /**
     * 构建成功结果。
     */
    private VectorStoreTestResult buildSuccessResult(VectorStoreConfig config, int count) {
        return new VectorStoreTestResult(
                true,
                "连接成功",
                String.format("地址: %s:%d, 集合数: %d", config.host(), config.port(), count)
        );
    }

    /**
     * 构建失败结果。
     */
    private VectorStoreTestResult buildFailureResult(Exception e) {
        return new VectorStoreTestResult(
                false,
                "连接失败: " + e.getMessage(),
                e.getClass().getSimpleName()
        );
    }

    /**
     * 关闭客户端连接。
     */
    private void closeClient(MilvusServiceClient client) {
        if (client != null) {
            try {
                client.close();
            } catch (Exception e) {
                log.warn("Failed to close Milvus client: {}", e.getMessage());
            }
        }
    }
}
