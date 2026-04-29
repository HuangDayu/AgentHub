package com.agenthub.test.integration;

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
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 模型配置管理集成测试 — 覆盖 ModelConfigController CRUD 全部端点。
 */
@SpringBootTest(classes = TestAgentHubApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ModelConfigControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;
    private static String createdModelId = "100000002";
    private final String workspaceId = "100000002";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .defaultRequest(getRequestBuilder())
                .build();
    }


    // =============================================================
    // POST /api/v1/workspaces/{workspaceId}/models — 创建 CHAT 模型 (OPENAI)
    // =============================================================
    @Test
    @Order(1)
    void shouldCreateChatModelConfig() throws Exception {
        String response = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/models", workspaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "gpt-4o-chat",
                                    "type": "CHAT",
                                    "supplier": "OPENAI",
                                    "apiKey": "sk-test-openai-key-12345678",
                                    "baseUrl": "https://api.openai.com/v1",
                                    "model": "gpt-4o",
                                    "enabled": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("gpt-4o-chat"))
                .andExpect(jsonPath("$.type").value("CHAT"))
                .andExpect(jsonPath("$.supplier").value("OPENAI"))
                .andExpect(jsonPath("$.model").value("gpt-4o"))
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.id").isString())
                .andReturn()
                .getResponse()
                .getContentAsString();

        createdModelId = objectMapper.readTree(response).get("id").asText();
    }

    // =============================================================
    // POST — 创建 EMBEDDING 模型 (OPENAI)
    // =============================================================
    @Test
    @Order(2)
    void shouldCreateEmbeddingModelConfig() throws Exception {
        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/models", workspaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "text-embedding-3-small",
                                    "type": "EMBEDDING",
                                    "supplier": "OPENAI",
                                    "apiKey": "sk-emb-key-12345678",
                                    "baseUrl": "https://api.openai.com/v1",
                                    "model": "text-embedding-3-small",
                                    "enabled": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("EMBEDDING"))
                .andExpect(jsonPath("$.model").value("text-embedding-3-small"))
                .andExpect(jsonPath("$.supplier").value("OPENAI"));
    }

    // =============================================================
    // POST — 创建 OLLAMA 模型
    // =============================================================
    @Test
    @Order(3)
    void shouldCreateOllamaModelConfig() throws Exception {
        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/models", workspaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "llama3-chat",
                                    "type": "CHAT",
                                    "supplier": "OLLAMA",
                                    "apiKey": "ollama-key-12345678",
                                    "baseUrl": "http://localhost:11434",
                                    "model": "llama3",
                                    "enabled": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.supplier").value("OLLAMA"))
                .andExpect(jsonPath("$.model").value("llama3"));
    }

    // =============================================================
    // POST — 创建 DEEPSEEK 模型
    // =============================================================
    @Test
    @Order(4)
    void shouldCreateDeepSeekModelConfig() throws Exception {
        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/models", workspaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "deepseek-chat",
                                    "type": "CHAT",
                                    "supplier": "DEEPSEEK",
                                    "apiKey": "sk-ds-key-12345678",
                                    "baseUrl": "https://api.deepseek.com/v1",
                                    "model": "deepseek-chat",
                                    "enabled": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.supplier").value("DEEPSEEK"))
                .andExpect(jsonPath("$.model").value("deepseek-chat"));
    }

    // =============================================================
    // POST — 创建 OPENROUTER 模型
    // =============================================================
    @Test
    @Order(5)
    void shouldCreateOpenRouterModelConfig() throws Exception {
        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/models", workspaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "claude-sonnet",
                                    "type": "CHAT",
                                    "supplier": "OPENROUTER",
                                    "apiKey": "sk-or-key-12345678",
                                    "baseUrl": "https://openrouter.ai/api/v1",
                                    "model": "anthropic/claude-3.5-sonnet",
                                    "enabled": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.supplier").value("OPENROUTER"))
                .andExpect(jsonPath("$.model").value("anthropic/claude-3.5-sonnet"));
    }

    // =============================================================
    // POST — 创建 IMAGE 模型
    // =============================================================
    @Test
    @Order(6)
    void shouldCreateImageModelConfig() throws Exception {
        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/models", workspaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "dall-e-3",
                                    "type": "IMAGE",
                                    "supplier": "OPENAI",
                                    "apiKey": "sk-image-key-12345678",
                                    "baseUrl": "https://api.openai.com/v1",
                                    "model": "dall-e-3",
                                    "enabled": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("IMAGE"))
                .andExpect(jsonPath("$.model").value("dall-e-3"));
    }

    // =============================================================
    // GET /api/v1/workspaces/{workspaceId}/models — 列出所有模型配置
    // =============================================================
    @Test
    @Order(7)
    void shouldListAllModelsForTenant() throws Exception {
        // Create a model first
        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/models", workspaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "list-test-model",
                                    "type": "CHAT",
                                    "supplier": "OPENAI",
                                    "apiKey": "sk-test-key",
                                    "baseUrl": "https://api.openai.com/v1",
                                    "model": "gpt-4o",
                                    "enabled": true
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/models", workspaceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    // =============================================================
    // GET /api/v1/workspaces/{workspaceId}/models?type=CHAT — 按类型过滤
    // =============================================================
    @Test
    @Order(8)
    void shouldListModelsByChatType() throws Exception {
        // Create a CHAT model first
        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/models", workspaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "chat-type-test-model",
                                    "type": "CHAT",
                                    "supplier": "OPENAI",
                                    "apiKey": "sk-test-key",
                                    "baseUrl": "https://api.openai.com/v1",
                                    "model": "gpt-4o",
                                    "enabled": true
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/models", workspaceId)
                        .param("type", "CHAT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[*].type", everyItem(is("CHAT"))));
    }

    @Test
    @Order(9)
    void shouldListModelsByEmbeddingType() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/models", workspaceId)
                        .param("type", "EMBEDDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].type", everyItem(is("EMBEDDING"))));
    }

    // =============================================================
    // GET /api/v1/workspaces/{workspaceId}/models?enabled=true — 按启用状态查询
    // =============================================================
    @Test
    @Order(10)
    void shouldListEnabledModels() throws Exception {
        // Create an enabled model first
        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/models", workspaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "enabled-test-model",
                                    "type": "CHAT",
                                    "supplier": "OPENAI",
                                    "apiKey": "sk-test-key",
                                    "baseUrl": "https://api.openai.com/v1",
                                    "model": "gpt-4o",
                                    "enabled": true
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/models", workspaceId)
                        .param("enabled", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    // =============================================================
    // GET /api/v1/workspaces/{workspaceId}/models/{id} — 获取模型详情
    // =============================================================
    @Test
    @Order(11)
    void shouldGetModelById() throws Exception {
        // Create a model first
        String response = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/models", workspaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "get-by-id-test-model",
                                    "type": "CHAT",
                                    "supplier": "OPENAI",
                                    "apiKey": "sk-test-key",
                                    "baseUrl": "https://api.openai.com/v1",
                                    "model": "gpt-4o",
                                    "enabled": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String modelId = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/models/{id}", workspaceId, modelId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(modelId))
                .andExpect(jsonPath("$.name").value("get-by-id-test-model"))
                .andExpect(jsonPath("$.type").value("CHAT"));
    }

    @Test
    @Order(12)
    void shouldReturnNotFoundForUnknownModelId() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/models/{id}", workspaceId, 99999L))
                .andExpect(status().isNotFound());
    }

    // =============================================================
    // PUT /api/v1/workspaces/{workspaceId}/models/{id} — 更新模型配置
    // =============================================================
    @Test
    @Order(13)
    void shouldUpdateModelConfig() throws Exception {
        // Create a model first
        String response = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/models", workspaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "update-test-model",
                                    "type": "CHAT",
                                    "supplier": "OPENAI",
                                    "apiKey": "sk-test-key",
                                    "baseUrl": "https://api.openai.com/v1",
                                    "model": "gpt-4o",
                                    "enabled": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String modelId = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(put("/api/v1/workspaces/{workspaceId}/models/{id}", workspaceId, modelId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "gpt-4o-chat-updated",
                                    "type": "CHAT",
                                    "supplier": "OPENAI",
                                    "apiKey": "sk-updated-key-12345678",
                                    "baseUrl": "https://api.openai.com/v2",
                                    "model": "gpt-4o-turbo",
                                    "enabled": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("gpt-4o-chat-updated"))
                .andExpect(jsonPath("$.model").value("gpt-4o-turbo"))
                .andExpect(jsonPath("$.baseUrl").value("https://api.openai.com/v2"))
                .andExpect(jsonPath("$.enabled").value(false));
    }

    // =============================================================
    // DELETE /api/v1/workspaces/{workspaceId}/models/{id} — 删除模型配置
    // =============================================================
    @Test
    @Order(14)
    void shouldDeleteModelConfig() throws Exception {
        // Create a model first
        String response = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/models", workspaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "delete-test-model",
                                    "type": "CHAT",
                                    "supplier": "OPENAI",
                                    "apiKey": "sk-test-key",
                                    "baseUrl": "https://api.openai.com/v1",
                                    "model": "gpt-4o",
                                    "enabled": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String modelId = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(delete("/api/v1/workspaces/{workspaceId}/models/{id}", workspaceId, modelId))
                .andExpect(status().isNoContent());

        // 验证已删除
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/models/{id}", workspaceId, modelId))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(15)
    void shouldReturnNotFoundWhenDeletingUnknownModel() throws Exception {
        mockMvc.perform(delete("/api/v1/workspaces/{workspaceId}/models/{id}", workspaceId, 99999L))
                .andExpect(status().isNotFound());
    }

    // =============================================================
    // GET /api/v1/workspaces/{workspaceId}/models — 空租户查询
    // =============================================================
    @Test
    @Order(16)
    void shouldReturnEmptyForUnknownTenant() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/models", workspaceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
