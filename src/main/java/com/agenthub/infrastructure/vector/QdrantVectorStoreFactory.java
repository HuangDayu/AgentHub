package com.agenthub.infrastructure.vector;

import com.google.common.util.concurrent.ListenableFuture;
import com.agenthub.common.exception.VectorStoreException;
import com.agenthub.domain.model.VectorStoreConfig;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Collections;
import io.qdrant.client.grpc.Collections.VectorParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.qdrant.QdrantVectorStore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Qdrant 向量库工厂。
 */
@Component
public class QdrantVectorStoreFactory implements VectorStoreFactory {
    private static final Logger log = LoggerFactory.getLogger(QdrantVectorStoreFactory.class);
    @Override
    public String getType() {
        return "QDRANT";
    }

    /**
     * 创建Qdrant向量存储实例。
     *
     * @param config         向量库配置
     * @param embeddingModel 嵌入模型
     * @return VectorStore实例
     */
    @Override
    public VectorStore create(VectorStoreConfig config, EmbeddingModel embeddingModel) {
        QdrantClient client = createQdrantClient(config);
        createCollection(client, config, embeddingModel);
        return buildVectorStore(client, config, embeddingModel);
    }

    /**
     * 创建Qdrant客户端。
     */
    private QdrantClient createQdrantClient(VectorStoreConfig config) {
        QdrantGrpcClient.Builder builder = QdrantGrpcClient.newBuilder(
                config.getHost(), config.getPort(), false, false);
        return new QdrantClient(builder.build());
    }

    /**
     * 构建向量存储实例。
     */
    private VectorStore buildVectorStore(QdrantClient client, VectorStoreConfig config,
            EmbeddingModel embeddingModel) {
        return QdrantVectorStore.builder(client, embeddingModel)
                .collectionName(config.getCollectionName())
                .batchingStrategy(new TokenCountBatchingStrategy())
                .initializeSchema(false)
                .build();
    }

    /**
     * 创建Qdrant集合（如不存在）。
     */
    private void createCollection(QdrantClient client, VectorStoreConfig config,
            EmbeddingModel embeddingModel) {
        try {
            if (checkCollectionExists(client, config)) {
                return;
            }
            createNewCollection(client, config, embeddingModel);
        } catch (VectorStoreException e) {
            throw e;
        } catch (Exception e) {
            throw new VectorStoreException("VectorStore collection create failed: " + e.getMessage());
        }
    }

    /**
     * 检查集合是否存在。
     */
    private boolean checkCollectionExists(QdrantClient client, VectorStoreConfig config)
            throws Exception {
        ListenableFuture<Boolean> existsFuture = client.collectionExistsAsync(config.getCollectionName());
        boolean exists = existsFuture.get(5, TimeUnit.SECONDS);
        if (exists) {
            log.debug("Qdrant collection already exists: {}", config.getCollectionName());
        }
        return exists;
    }

    /**
     * 创建新集合。
     */
    private void createNewCollection(QdrantClient client, VectorStoreConfig config,
            EmbeddingModel embeddingModel) throws Exception {
        VectorParams vectorParams = buildVectorParams(embeddingModel);
        ListenableFuture<Collections.CollectionOperationResponse> future =
                client.createCollectionAsync(config.getCollectionName(), vectorParams);
        Collections.CollectionOperationResponse response = future.get(10, TimeUnit.SECONDS);
        validateCreationResponse(response, config);
    }

    /**
     * 构建向量参数。
     */
    private VectorParams buildVectorParams(EmbeddingModel embeddingModel) {
        return VectorParams.newBuilder()
                .setSize(embeddingModel.dimensions())
                .setDistance(Collections.Distance.Cosine)
                .build();
    }

    /**
     * 验证创建响应。
     */
    private void validateCreationResponse(Collections.CollectionOperationResponse response,
            VectorStoreConfig config) {
        if (!response.getResult()) {
            throw new VectorStoreException("VectorStore collection create failed");
        }
        log.info("Successfully created Qdrant collection: {}", config.getCollectionName());
    }

    /**
     * 测试Qdrant连接。
     *
     * @param config 向量库配置
     * @return 测试结果
     */
    @Override
    public VectorStoreTestResult testConnection(VectorStoreConfig config) {
        QdrantClient client = null;
        try {
            log.info("Testing Qdrant connection: {}:{}", config.getHost(), config.getPort());
            client = createQdrantClient(config);
            return performConnectionTest(client, config);
        } catch (Exception e) {
            return handleTestFailure(e);
        } finally {
            closeClientSafely(client);
        }
    }

    /**
     * 执行连接测试。
     */
    private VectorStoreTestResult performConnectionTest(QdrantClient client, VectorStoreConfig config)
            throws Exception {
        ListenableFuture<List<String>> future = client.listCollectionsAsync();
        List<String> response = future.get(5, TimeUnit.SECONDS);
        log.info("Qdrant connection test successful, found {} collections", response.size());
        return new VectorStoreTestResult(true, "连接成功",
                String.format("地址: %s:%d, 集合数: %d", config.getHost(), config.getPort(), response.size()));
    }

    /**
     * 处理测试失败。
     */
    private VectorStoreTestResult handleTestFailure(Exception e) {
        log.error("Qdrant connection test failed: {}", e.getMessage(), e);
        return new VectorStoreTestResult(false, "连接失败: " + e.getMessage(),
                e.getClass().getSimpleName());
    }

    /**
     * 安全关闭客户端。
     */
    private void closeClientSafely(QdrantClient client) {
        if (client != null) {
            try {
                client.close();
            } catch (Exception e) {
                log.warn("Failed to close Qdrant client: {}", e.getMessage());
            }
        }
    }
}
