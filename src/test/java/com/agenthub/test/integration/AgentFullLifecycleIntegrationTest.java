package com.agenthub.test.integration;

import cn.hutool.core.io.FileUtil;
import com.agenthub.test.TestAgentHubApplication;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static com.agenthub.test.common.TestCommonTools.getRequestBuilder;
import static org.springframework.ai.util.json.JsonParser.toJson;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TestAgentHubApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Agent完整生命周期集成测试")
public class AgentFullLifecycleIntegrationTest {
    private static final Logger log = LoggerFactory.getLogger(AgentFullLifecycleIntegrationTest.class);
    public static final String key = FileUtil.readUtf8String(System.getProperty("user.dir") + "/keys/key.txt");
    @Autowired
    private WebApplicationContext webApplicationContext;
    public static final String WORKSPACE_ID = "100000002";
    public static final String TENANT_ID = "100000002";
    private static MockMvc mockMvc;
    private static ObjectMapper objectMapper;

    private static String vectorStoreConfigId;
    private static String chatModelConfigId;
    private static String embeddingModelConfigId;
    private static String knowledgeBaseId;
    private static String retrievalStrategyId;
    private static String modelStrategyId;
    private static String toolStrategyId;
    private static String guardrailStrategyId;
    private static String createdTemplateId;
    private static String createdToolId;
    private static String agentId;
    private static String sessionId;

    @BeforeEach
    void setUp() {
        if (mockMvc == null) {
            mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                    .defaultRequest(getRequestBuilder())
                    .build();
        }
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }
    }

    @Test
    @Order(1)
    @DisplayName("Step 1: 创建向量存储配置")
    void step1_shouldCreateVectorStoreConfig() throws Exception {
        String response = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/vector-stores", WORKSPACE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "test-qdrant-config",
                                    "type": "QDRANT",
                                    "host": "localhost",
                                    "port": 6334,
                                    "collectionName": "test-collection"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        vectorStoreConfigId = extractId(response);
        Assertions.assertNotNull(vectorStoreConfigId);
    }

    @Test
    @Order(2)
    @DisplayName("Step 2: 创建聊天模型配置")
    void step2_shouldCreateChatModelConfig() throws Exception {
        String response = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/models", WORKSPACE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                    "name": "test-chat-model",
                                    "type": "CHAT",
                                    "supplier": "OPENROUTER",
                                    "apiKey": "%s",
                                    "model": "openrouter/free",
                                    "baseUrl": "https://openrouter.ai/api",
                                    "temperature": 0.7,
                                    "maxTokens": 2048,
                                    "enabled": true
                                }
                                """, key)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        chatModelConfigId = extractId(response);
        Assertions.assertNotNull(chatModelConfigId);
    }

    @Test
    @Order(3)
    @DisplayName("Step 3: 创建嵌入模型配置")
    void step3_shouldCreateEmbeddingModelConfig() throws Exception {
        String response = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/models", WORKSPACE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "test-qwen3-embedding",
                                    "type": "EMBEDDING",
                                    "supplier": "OPENAI",
                                    "model": "qwen3-embedding",
                                    "baseUrl": "http://127.0.0.1:9090",
                                    "apiKey": "xxx",
                                    "enabled": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        embeddingModelConfigId = extractId(response);
        Assertions.assertNotNull(embeddingModelConfigId);
    }

    @Test
    @Order(4)
    @DisplayName("Step 4: 创建知识库")
    void step4_shouldCreateKnowledgeBase() throws Exception {
        String response = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/knowledge-bases", WORKSPACE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                    "tenantId": "%s",
                                    "workspaceId": "%s",
                                    "kbCode": "test-kb-full",
                                    "name": "Test Knowledge Base",
                                    "description": "Knowledge base for full lifecycle test",
                                    "indexProvider": "QDRANT",
                                    "vectorStoreConfigId": "%s",
                                    "embeddingModelConfigId": "%s",
                                    "chatModelConfigId": "%s"
                                }
                                """, TENANT_ID, WORKSPACE_ID, vectorStoreConfigId, embeddingModelConfigId, chatModelConfigId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        knowledgeBaseId = extractId(response);
        Assertions.assertNotNull(knowledgeBaseId);
    }

    @Test
    @Order(5)
    @DisplayName("Step 5: 上传测试文件到知识库")
    void step5_shouldUploadFileToKnowledgeBase() throws Exception {
        String testContent = generateTestMarkdown();
        MockMultipartFile file = new MockMultipartFile(
                "file", "test-knowledge.md", "text/markdown",
                testContent.getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/v1/workspaces/{workspaceId}/knowledge-bases/{kbId}/documents", WORKSPACE_ID, knowledgeBaseId)
                        .file(file))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @Order(6)
    @DisplayName("Step 6: 创建检索策略")
    void step6_shouldCreateRetrievalStrategy() throws Exception {
        String response = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/retrieval-strategies", WORKSPACE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                    "name": "test-retrieval-strategy",
                                    "description": "Test retrieval strategy",
                                    "retrievalType": "HYBRID",
                                    "topK": 10,
                                    "similarityThreshold": 0.75,
                                    "rerankEnabled": true,
                                    "knowledgeBaseIds": ["%s"]
                                }
                                """, knowledgeBaseId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        retrievalStrategyId = extractId(response);
        Assertions.assertNotNull(retrievalStrategyId);
    }

    @Test
    @Order(7)
    @DisplayName("Step 7: 创建模型策略")
    void step7_shouldCreateModelStrategy() throws Exception {
        String response = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/model-strategies", WORKSPACE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                    "name": "test-model-strategy",
                                    "description": "Test model strategy",
                                    "chatModelConfigId": "%s",
                                    "embeddingModelConfigId": "%s",
                                    "temperature": 0.7,
                                    "maxTokens": 2048,
                                    "enabled": true
                                }
                                """, chatModelConfigId, embeddingModelConfigId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        modelStrategyId = extractId(response);
        Assertions.assertNotNull(modelStrategyId);
    }

    @Test
    @Order(8)
    @DisplayName("Step 8: 创建工具策略")
    void step8_shouldCreateToolStrategy() throws Exception {
        String response = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/tool-strategies", WORKSPACE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "test-httpTool-strategy",
                                    "description": "Test httpTool strategy",
                                    "maxConcurrentCalls": 5,
                                    "timeoutSeconds": 30
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        toolStrategyId = extractId(response);
        Assertions.assertNotNull(toolStrategyId);
    }

    @Test
    @Order(9)
    @DisplayName("Step 9: 创建护栏策略")
    void step9_shouldCreateGuardrailStrategy() throws Exception {
        String response = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/guardrail-strategies", WORKSPACE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "test-guardrail-strategy",
                                    "description": "Test guardrail strategy",
                                    "inputValidationEnabled": true,
                                    "outputValidationEnabled": true,
                                    "maxInputLength": 10000,
                                    "maxOutputLength": 4000
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        guardrailStrategyId = extractId(response);
        Assertions.assertNotNull(guardrailStrategyId);
    }

    @Test
    @Order(10)
    @DisplayName("Step 10: 创建提示词模板")
    void shouldCreatePromptTemplate() throws Exception {
        String responseBody = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/prompt-templates", WORKSPACE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "system-content",
                                    "description": "System content template",
                                    "category": "SYSTEM",
                                    "content": "You are a helpful assistant.",
                                    "variables": [],
                                    "isActive": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("system-content"))
                .andReturn().getResponse().getContentAsString();

        createdTemplateId = objectMapper.readTree(responseBody).get("id").asText();
    }

    @Test
    @Order(11)
    @DisplayName("Step 10: 创建工具")
    void shouldCreateMcpTool() throws Exception {
        String responseBody = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/mcp-tools", WORKSPACE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                    "name": "server-filesystem",
                                    "description": "File system MCP server",
                                    "serverUrl": "npx",
                                    "serverType": "STDIO",
                                    "command": "npx",
                                    "args": %s,
                                    "env": %s,
                                    "enabled": true
                                }
                                """,toJson(List.of("-y","@modelcontextprotocol/server-filesystem","E:\\Code\\vibe\\AgentHub")),
                                toJson(Map.of("NODE_ENV", "production")))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("server-filesystem"))
                .andReturn().getResponse().getContentAsString();

        createdToolId = objectMapper.readTree(responseBody).get("id").asText();
    }


    @Test
    @Order(12)
    @DisplayName("Step 10: 创建Agent")
    void step10_shouldCreateAgent() throws Exception {
        String response = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/agents", WORKSPACE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                    "tenantId": "%s",
                                    "workspaceId": "%s",
                                    "agentCode": "test-agent-full",
                                    "name": "test-full-lifecycle-agent",
                                    "description": "Agent for full lifecycle integration test"
                                }
                                """, TENANT_ID, WORKSPACE_ID)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        agentId = extractId(response);
        Assertions.assertNotNull(agentId);
    }

    @Test
    @Order(13)
    @DisplayName("Step 11: 配置Agent关联策略")
    void step11_shouldConfigureAgentWithStrategies() throws Exception {
        Assertions.assertNotNull(agentId);
        Assertions.assertNotNull(retrievalStrategyId);

        // 配置检索策略
        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/agents/{agentId}/configs", WORKSPACE_ID, agentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                    "category": "STRATEGY",
                                    "type": "RETRIEVAL_STRATEGY",
                                    "configId": "%s",
                                    "description": "Retrieval strategy config",
                                    "priority": 1,
                                    "enabled": true
                                }
                                """, retrievalStrategyId)))
                .andExpect(status().isCreated());

        // 配置模型策略
        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/agents/{agentId}/configs", WORKSPACE_ID, agentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                    "category": "STRATEGY",
                                    "type": "MODEL_STRATEGY",
                                    "configId": "%s",
                                    "description": "Model strategy config",
                                    "priority": 1,
                                    "enabled": true
                                }
                                """, modelStrategyId)))
                .andExpect(status().isCreated());

        // 配置工具策略
        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/agents/{agentId}/configs", WORKSPACE_ID, agentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                    "category": "STRATEGY",
                                    "type": "TOOL_STRATEGY",
                                    "configId": "%s",
                                    "description": "Tool strategy config",
                                    "priority": 1,
                                    "enabled": true
                                }
                                """, toolStrategyId)))
                .andExpect(status().isCreated());

        // 配置护栏策略
        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/agents/{agentId}/configs", WORKSPACE_ID, agentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                    "category": "STRATEGY",
                                    "type": "GUARDRAIL_STRATEGY",
                                    "configId": "%s",
                                    "description": "Guardrail strategy config",
                                    "priority": 1,
                                    "enabled": true
                                }
                                """, guardrailStrategyId)))
                .andExpect(status().isCreated());

        // 配置提示词
        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/agents/{agentId}/configs", WORKSPACE_ID, agentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                    "category": "PROMPT",
                                    "type": "SYSTEM_PROMPT",
                                    "configId": "%s",
                                    "description": "System content config",
                                    "priority": 1,
                                    "enabled": true
                                }
                                """, createdTemplateId)))
                .andExpect(status().isCreated());

        // 配置工具
        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/agents/{agentId}/configs", WORKSPACE_ID, agentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                    "category": "TOOL",
                                    "type": "MCP_TOOL",
                                    "configId": "%s",
                                    "description": "Mcp httpTool config",
                                    "priority": 1,
                                    "enabled": true
                                }
                                """, createdToolId)))
                .andExpect(status().isCreated());

        // 配置工具
        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/agents/{agentId}/configs", WORKSPACE_ID, agentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                    "category": "MODEL",
                                    "type": "CHAT_MODEL",
                                    "configId": "%s",
                                    "description": "Chat model config",
                                    "priority": 1,
                                    "enabled": true
                                }
                                """, chatModelConfigId)))
                .andExpect(status().isCreated());

        // 配置工具
        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/agents/{agentId}/configs", WORKSPACE_ID, agentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                    "category": "MODEL",
                                    "type": "EMBEDDING_MODEL",
                                    "configId": "%s",
                                    "description": "Chat model config",
                                    "priority": 1,
                                    "enabled": true
                                }
                                """, embeddingModelConfigId)))
                .andExpect(status().isCreated());
    }

    @Test
    @Order(14)
    @DisplayName("Step 12: 发布Agent")
    void step12_shouldEnabledAgent() throws Exception {
        Assertions.assertNotNull(agentId);

        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/agents/{agentId}/enabled", WORKSPACE_ID, agentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", WORKSPACE_ID).value("PUBLISHED"));
    }

    @Test
    @Order(15)
    @DisplayName("Step 13: 创建会话")
    void step13_shouldCreateSession() throws Exception {
        Assertions.assertNotNull(agentId);

        String response = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/agents/{agentId}/sessions", WORKSPACE_ID, agentId))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        sessionId = extractId(response);
        Assertions.assertNotNull(sessionId);
    }

    @Test
    @Order(16)
    @DisplayName("Step 14: 发送消息并验证策略应用")
    void step14_shouldSendMessageAndVerifyStrategyApplication() throws Exception {
        Assertions.assertNotNull(agentId);
        Assertions.assertNotNull(sessionId);

        String response = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/agents/{agentId}/sessions/{sessionId}/messages", WORKSPACE_ID, agentId, sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "content": "Hello, this is a test message."
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.role").value("assistant"))
                .andReturn().getResponse().getContentAsString();
        log.info("step14 Response: {}", response);
    }

    @Test
    @Order(17)
    @DisplayName("Step 15: 验证Agent配置")
    void step15_shouldVerifyAgentConfigs() throws Exception {
        Assertions.assertNotNull(agentId);

        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/agents/{agentId}/configs", WORKSPACE_ID, agentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(18)
    @DisplayName("Step 16: 验证Agent配置完整性")
    void step16_shouldVerifyAgentConfigCompleteness() throws Exception {
        Assertions.assertNotNull(agentId);

        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/agents/{agentId}/configs", WORKSPACE_ID, agentId)
                        .param("category", "STRATEGY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }


    static void cleanup() throws Exception {
        if (mockMvc == null) return;

        cleanupAgent();
        cleanupStrategies();
        cleanupKnowledgeBase();
        cleanupModelConfigs();
        cleanupVectorStoreConfig();
    }

    private static void cleanupAgent() throws Exception {
        if (agentId != null) {
            try {
                mockMvc.perform(delete("/api/v1/workspaces/{workspaceId}/agents/{agentId}", WORKSPACE_ID, agentId))
                        .andExpect(status().isNoContent());
            } catch (Exception e) {
                System.err.println("Failed to delete agent: " + e.getMessage());
            }
        }
    }

    private static void cleanupStrategies() throws Exception {
        try {
            mockMvc.perform(delete("/api/v1/workspaces/{workspaceId}/retrieval-strategies/{id}", WORKSPACE_ID, retrievalStrategyId))
                    .andExpect(status().isNoContent());
            mockMvc.perform(delete("/api/v1/workspaces/{workspaceId}/guardrail-strategies/{id}", WORKSPACE_ID, guardrailStrategyId))
                    .andExpect(status().isNoContent());
            mockMvc.perform(delete("/api/v1/workspaces/{workspaceId}/tool-strategies/{id}", WORKSPACE_ID, toolStrategyId))
                    .andExpect(status().isNoContent());
            mockMvc.perform(delete("/api/v1/workspaces/{workspaceId}/model-strategies/{id}", WORKSPACE_ID, modelStrategyId))
                    .andExpect(status().isNoContent());
        } catch (Exception e) {
            System.err.println("Failed to delete retrieval strategy: " + e.getMessage());
        }
    }

    private static void cleanupKnowledgeBase() throws Exception {
        if (knowledgeBaseId != null) {
            try {
                mockMvc.perform(delete("/api/v1/workspaces/{workspaceId}/knowledge-bases/{kbId}", WORKSPACE_ID, knowledgeBaseId))
                        .andExpect(status().isNoContent());
            } catch (Exception e) {
                System.err.println("Failed to delete knowledge base: " + e.getMessage());
            }
        }
    }

    private static void cleanupModelConfigs() throws Exception {
        if (chatModelConfigId != null) {
            try {
                mockMvc.perform(delete("/api/v1/workspaces/{workspaceId}/models/{id}", WORKSPACE_ID, chatModelConfigId))
                        .andExpect(status().isNoContent());
            } catch (Exception e) {
                System.err.println("Failed to delete chat model config: " + e.getMessage());
            }
        }
    }

    private static void cleanupVectorStoreConfig() throws Exception {
        if (vectorStoreConfigId != null) {
            try {
                mockMvc.perform(delete("/api/v1/workspaces/{workspaceId}/vector-stores/{configId}", WORKSPACE_ID, vectorStoreConfigId))
                        .andExpect(status().isNoContent());
            } catch (Exception e) {
                System.err.println("Failed to delete vector store config: " + e.getMessage());
            }
        }
    }

    private String generateTestMarkdown() {
        return """
                # Test Knowledge Document
                
                This is a test document for integration testing.
                
                ## Section 1
                The quick brown fox jumps over the lazy dog.
                
                ## Section 2
                Machine learning is a subset of artificial intelligence.
                """;
    }

    private String extractId(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);
            return node.get("id").asText();
        } catch (Exception e) {
            int start = json.indexOf("\"id\":\"") + 6;
            if (start < 6) return null;
            int end = json.indexOf("\"", start);
            return json.substring(start, end);
        }
    }
}
