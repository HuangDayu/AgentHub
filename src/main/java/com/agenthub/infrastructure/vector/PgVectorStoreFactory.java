package com.agenthub.infrastructure.vector;

import com.agenthub.domain.model.VectorStoreConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * PGVector 向量库工厂。
 */
@Component
public class PgVectorStoreFactory implements VectorStoreFactory {
    
    private static final Logger log = LoggerFactory.getLogger(PgVectorStoreFactory.class);

    private final DataSource dataSource;

    public PgVectorStoreFactory(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public String getType() {
        return "PGVECTOR";
    }

    /**
     * 创建PGVector向量存储实例。
     *
     * @param config         向量库配置
     * @param embeddingModel 嵌入模型
     * @return VectorStore实例
     */
    @Override
    public VectorStore create(VectorStoreConfig config, EmbeddingModel embeddingModel) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .dimensions(1536)
                .indexType(PgIndexType.HNSW)
                .distanceType(PgDistanceType.COSINE_DISTANCE)
                .initializeSchema(true)
                .build();
    }

    /**
     * 测试PGVector连接。
     *
     * @param config 向量库配置
     * @return 测试结果
     */
    @Override
    public VectorStoreTestResult testConnection(VectorStoreConfig config) {
        try {
            log.info("Testing PGVector connection");
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            testBasicConnection(jdbcTemplate);
            return checkExtension(jdbcTemplate);
        } catch (Exception e) {
            log.error("PGVector connection test failed: {}", e.getMessage(), e);
            return buildFailureResult(e);
        }
    }

    /**
     * 测试基本数据库连接。
     */
    private void testBasicConnection(JdbcTemplate jdbcTemplate) {
        Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        if (result == null || result != 1) {
            throw new RuntimeException("PGVector connection test failed: unexpected result");
        }
    }

    /**
     * 检查pgvector扩展是否安装。
     */
    private VectorStoreTestResult checkExtension(JdbcTemplate jdbcTemplate) {
        Boolean extensionExists = jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM pg_extension WHERE extname = 'vector')",
                Boolean.class
        );
        if (extensionExists == null || !extensionExists) {
            log.warn("PGVector extension not installed");
            return buildExtensionNotInstalledResult();
        }
        log.info("PGVector connection test successful, extension installed");
        return new VectorStoreTestResult(true, "连接成功", "PGVector扩展已安装");
    }

    /**
     * 构建扩展未安装结果。
     */
    private VectorStoreTestResult buildExtensionNotInstalledResult() {
        return new VectorStoreTestResult(
                false,
                "PGVector扩展未安装",
                "请在PostgreSQL中安装pgvector扩展: CREATE EXTENSION vector;"
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
}
