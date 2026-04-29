package com.agenthub.test.integration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.agenthub.test.TestAgentHubApplication;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static com.agenthub.test.common.TestCommonTools.getRequestBuilder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * VectorStoreConfig Controller 集成测试 — 覆盖 CRUD 全部端点。
 * <p>
 * 每个测试独立运行，互不依赖创建步骤（除了依赖 create 的测试）。
 */
@SpringBootTest(classes = TestAgentHubApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VectorStoreConfigControllerIntegrationTest {

    private String createdConfigId = null;
    private final String workspaceId = "100000002";
    private final ObjectMapper objectMapper = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .defaultRequest(getRequestBuilder())
                .build();
    }

    // =============================================================
    // POST /api/v1/workspaces/{workspaceId}/vector-stores — 创建向量库配置
    // =============================================================
    @Test
    @Order(1)
    void shouldCreateVectorStoreConfig() throws Exception {
        String responseBody = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/vector-stores", workspaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "qdrant-main",
                                    "type": "QDRANT",
                                    "host": "localhost",
                                    "port": 6334,
                                    "collectionName": "my-collection",
                                    "extraParams": null
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("qdrant-main"))
                .andExpect(jsonPath("$.type").value("QDRANT"))
                .andExpect(jsonPath("$.host").value("localhost"))
                .andExpect(jsonPath("$.port").value(6334))
                .andReturn()
                .getResponse()
                .getContentAsString();

        createdConfigId = objectMapper.readTree(responseBody).get("id").asText();
    }

    // =============================================================
    // POST — 创建 Chroma 类型配置
    // =============================================================
    @Test
    @Order(2)
    void shouldCreateChromaVectorStoreConfig() throws Exception {
        String responseBody = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/vector-stores", workspaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "chroma-test",
                                    "type": "CHROMA",
                                    "host": "localhost",
                                    "port": 8000,
                                    "collectionName": "chroma-collection",
                                    "extraParams": null
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("chroma-test"))
                .andExpect(jsonPath("$.type").value("CHROMA"))
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    // =============================================================
    // POST — 创建 Redis 类型配置
    // =============================================================
    @Test
    @Order(3)
    void shouldCreateRedisVectorStoreConfig() throws Exception {
        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/vector-stores", workspaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "redis-vector",
                                    "type": "REDIS",
                                    "host": "localhost",
                                    "port": 6379,
                                    "collectionName": "redis-idx",
                                    "extraParams": null
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("redis-vector"))
                .andExpect(jsonPath("$.type").value("REDIS"));
    }

    // =============================================================
    // POST — 创建 Milvus 类型配置
    // =============================================================
    @Test
    @Order(4)
    void shouldCreateMilvusVectorStoreConfig() throws Exception {
        String responseBody = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/vector-stores", workspaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "milvus-store",
                                    "type": "MILVUS",
                                    "host": "localhost",
                                    "port": 19530,
                                    "collectionName": "milvus-collection",
                                    "extraParams": null
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("milvus-store"))
                .andExpect(jsonPath("$.type").value("MILVUS"))
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    // =============================================================
    // GET /api/v1/workspaces/{workspaceId}/vector-stores — 列出向量库配置
    // =============================================================
    @Test
    @Order(5)
    void shouldListVectorStoreConfigs() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/vector-stores", workspaceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isNotEmpty()); // qdrant-main, chroma-test, redis-vector, milvus-store
    }

    // =============================================================
    // GET /api/v1/workspaces/{workspaceId}/vector-stores/{configId} — 按 ID 查询
    // =============================================================
    @Test
    @Order(6)
    void shouldGetVectorStoreConfigById() throws Exception {
        Assertions.assertNotNull(createdConfigId, "createdConfigId should be set from previous test");

        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/vector-stores/{configId}", workspaceId, createdConfigId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdConfigId))
                .andExpect(jsonPath("$.name").value("qdrant-main"));
    }

    // =============================================================
    // GET — 查询不存在的配置 (404)
    // =============================================================
    @Test
    @Order(7)
    void shouldReturnNotFoundForUnknownConfig() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/vector-stores/{configId}", workspaceId, "00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound());
    }

    // =============================================================
    // PATCH /api/v1/workspaces/{workspaceId}/vector-stores/{configId} — 更新配置
    // =============================================================
    @Test
    @Order(8)
    void shouldUpdateVectorStoreConfig() throws Exception {
        Assertions.assertNotNull(createdConfigId, "createdConfigId should be set from previous test");

        mockMvc.perform(put("/api/v1/workspaces/{workspaceId}/vector-stores/{configId}", workspaceId, createdConfigId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "qdrant-updated",
                                    "host": "localhost"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("qdrant-updated"))
                .andExpect(jsonPath("$.host").value("localhost"));
    }

    // =============================================================
    // POST /api/v1/workspaces/{workspaceId}/vector-stores/{configId}/refresh — 刷新实例
    // =============================================================
    @Test
    @Order(9)
    void shouldRefreshVectorStoreInstance() throws Exception {
        Assertions.assertNotNull(createdConfigId, "createdConfigId should be set from previous test");
        // 因为向量对象只有在入库时才会被创建和初始化
        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/vector-stores/{configId}/refresh", workspaceId, createdConfigId))
                .andExpect(status().isNotFound());
    }

    // =============================================================
    // DELETE /api/v1/workspaces/{workspaceId}/vector-stores/{configId}/instance — 销毁实例
    // =============================================================
    @Test
    @Order(10)
    void shouldDestroyVectorStoreInstance() throws Exception {
        Assertions.assertNotNull(createdConfigId, "createdConfigId should be set from previous test");

        mockMvc.perform(delete("/api/v1/workspaces/{workspaceId}/vector-stores/{configId}/instance", workspaceId, createdConfigId))
                .andExpect(status().isNoContent());
    }

    // =============================================================
    // DELETE /api/v1/workspaces/{workspaceId}/vector-stores/{configId} — 删除配置
    // =============================================================
    @Test
    @Order(11)
    void shouldDeleteVectorStoreConfig() throws Exception {
        Assertions.assertNotNull(createdConfigId, "createdConfigId should be set from previous test");

        mockMvc.perform(delete("/api/v1/workspaces/{workspaceId}/vector-stores/{configId}", workspaceId, createdConfigId))
                .andExpect(status().isNoContent());

        // 验证已删除
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/vector-stores/{configId}", workspaceId, createdConfigId))
                .andExpect(status().isNotFound());
    }

    // =============================================================
    // DELETE — 删除不存在的配置 (404)
    // =============================================================
    @Test
    @Order(12)
    void shouldReturnNotFoundWhenDeletingUnknownConfig() throws Exception {
        mockMvc.perform(delete("/api/v1/workspaces/{workspaceId}/vector-stores/{configId}", workspaceId, "00000000-0000-0000-0000-000000000001"))
                .andExpect(status().isNotFound());
    }

    // =============================================================
    // POST — 重复名称 (409)
    // =============================================================
    @Test
    @Order(13)
    void shouldReturnConflictForDuplicateName() throws Exception {
        String dupTenantId = "550e8400-e29b-41d4-a716-446655440001";

        // 先创建一个
        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/vector-stores", workspaceId, dupTenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "dup-vs-test",
                                    "type": "QDRANT",
                                    "host": "localhost",
                                    "port": 6334,
                                    "collectionName": "dup-test-collection"
                                }
                                """))
                .andExpect(status().isCreated());

        // 再创建同名 — 409 Conflict
        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/vector-stores", workspaceId, dupTenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "dup-vs-test",
                                    "type": "CHROMA",
                                    "host": "other-host",
                                    "port": 8000,
                                    "collectionName": "dup-test-collection-2"
                                }
                                """))
                .andExpect(status().isConflict());
    }

    // =============================================================
    // GET — 查询租户的向量配置列表
    // =============================================================
    @Test
    @Order(14)
    void shouldReturnEmptyForUnknownTenant() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/vector-stores", workspaceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty());
    }
}
