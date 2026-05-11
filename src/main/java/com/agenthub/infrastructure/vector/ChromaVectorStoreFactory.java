package com.agenthub.infrastructure.vector;

import com.agenthub.domain.model.VectorStoreConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chroma.vectorstore.ChromaApi;
import org.springframework.ai.chroma.vectorstore.ChromaVectorStore;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

/**
 * Chroma 向量库工厂。
 */
@Component
public class ChromaVectorStoreFactory implements VectorStoreFactory {
    
    private static final Logger log = LoggerFactory.getLogger(ChromaVectorStoreFactory.class);

    @Override
    public String getType() {
        return "CHROMA";
    }

    /**
     * 创建Chroma向量存储实例。
     *
     * @param config         向量库配置
     * @param embeddingModel 嵌入模型
     * @return VectorStore实例
     */
    @Override
    public VectorStore create(VectorStoreConfig config, EmbeddingModel embeddingModel) {
        String baseUrl = resolveBaseUrl(config);
        ChromaApi chromaApi = new ChromaApi(baseUrl, RestClient.builder(), JsonMapper.builder().build());
        return ChromaVectorStore.builder(chromaApi, embeddingModel)
                .collectionName(config.getCollectionName())
                .build();
    }

    /**
     * 解析基础URL。
     */
    private String resolveBaseUrl(VectorStoreConfig config) {
        return (config.getExtraParams() != null && !config.getExtraParams().isBlank())
                ? config.getExtraParams()
                : "http://" + config.getHost() + ":" + config.getPort();
    }

    /**
     * 测试Chroma连接。
     *
     * @param config 向量库配置
     * @return 测试结果
     */
    @Override
    public VectorStoreTestResult testConnection(VectorStoreConfig config) {
        try {
            log.info("Testing Chroma connection: {}:{}", config.getHost(), config.getPort());
            String baseUrl = resolveBaseUrl(config);
            String response = testHeartbeat(baseUrl);
            log.info("Chroma connection test successful: {}", response);
            return new VectorStoreTestResult(true, "连接成功", 
                    String.format("地址: %s, 响应: %s", baseUrl, response));
        } catch (Exception e) {
            log.error("Chroma connection test failed: {}", e.getMessage(), e);
            return buildFailureResult(e);
        }
    }

    /**
     * 测试heartbeat端点。
     */
    private String testHeartbeat(String baseUrl) {
        RestClient restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
        return restClient.get()
                .uri("/api/v1/heartbeat")
                .retrieve()
                .body(String.class);
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
}
