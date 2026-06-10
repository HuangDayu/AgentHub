package com.agenthub.test.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.agenthub.test.TestAgentHubApplication;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static com.agenthub.common.utils.RandomUtils.randomShortId;
import static com.agenthub.test.common.TestCommonTools.*;
import static com.agenthub.test.common.TestCommonTools.getRequestBuilder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Retrieval Service 集成测试 — 覆盖 KnowledgeBaseController 全部端点。
 * <p>
 * 每个测试独立运行，互不依赖。
 */
@SpringBootTest(classes = TestAgentHubApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KnowledgeBaseControllerIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private String createdKbId; // 用于存储创建的知识库ID

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
    // GET /api/v1/workspaces/{workspaceId}/knowledge-bases — 列出知识库
    // =============================================================
    @Test
    @Order(1)
    void shouldListKnowledgeBases() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/knowledge-bases", WORKSPACE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());
    }

    // =============================================================
    // POST /api/v1/workspaces/{workspaceId}/knowledge-bases — 创建知识库 (201)
    // =============================================================
    @Test
    @Order(2)
    void shouldCreateKnowledgeBase() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/knowledge-bases", WORKSPACE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                    "kbCode": "kb-%s",
                                    "name": "Create Test KB",
                                    "description": "Test knowledge base"
                                }
                                """,randomShortId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.kbCode").isString())
                .andExpect(jsonPath("$.kbCode").value(org.hamcrest.Matchers.startsWith("kb-")))
                .andExpect(jsonPath("$.name").value("Create Test KB")).andReturn();
        // 提取id供可能的后续使用
        createdKbId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    // =============================================================
    // GET /api/v1/workspaces/{workspaceId}/knowledge-bases/{kbCode}/documents — 列出文档
    // =============================================================
    @Test
    @Order(3)
    void shouldListDocuments() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/knowledge-bases/{createdKbId}/documents", WORKSPACE_ID,createdKbId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // =============================================================
    // POST /api/v1/workspaces/{workspaceId}/knowledge-bases/{kbCode}/documents — 上传文档 (multipart, 202)
    // =============================================================
    @Test
    @Order(4)
    void shouldUploadDocument() throws Exception {
        byte[] fileContent = "test document content".getBytes();
        mockMvc.perform(multipart("/api/v1/workspaces/{workspaceId}/knowledge-bases/{createdKbId}/documents", WORKSPACE_ID,createdKbId)
                        .file("file", fileContent)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.jobId").isNotEmpty());
    }

    // =============================================================
    // DELETE /api/v1/workspaces/{workspaceId}/knowledge-bases/{kbCode}/documents/{docId} — 删除文档 (204)
    // =============================================================
    @Test
    @Order(5)
    void shouldDeleteDocument() throws Exception {
        mockMvc.perform(delete("/api/v1/workspaces/{workspaceId}/knowledge-bases/{createdKbId}/documents/doc-nonexistent", WORKSPACE_ID,createdKbId))
                .andExpect(status().isNotFound());
    }

    // =============================================================
    // GET /api/v1/workspaces/{workspaceId}/ingestion-jobs/{jobId} — 查询任务状态
    // =============================================================
    @Test
    @Order(6)
    void shouldReturnNotFoundForUnknownJob() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/knowledge-bases/ingestion-jobs/job-nonexistent", WORKSPACE_ID))
                .andExpect(status().isNotFound());
    }

    // =============================================================
    // GET → PATCH → DELETE — 端到端 CRUD 测试
    // =============================================================
    @Test
    @Order(7)
    void shouldGetPatchAndDeleteKnowledgeBase() throws Exception {
        String kbCode = "kb-crud-" + randomShortId();
        // 先创建一个 KB
        MvcResult createResult = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/knowledge-bases", WORKSPACE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                    "kbCode": "%s",
                                    "name": "CRUD KB",
                                    "description": "For CRUD testing"
                                }
                                """, kbCode)))
                .andExpect(status().isCreated())
                .andReturn();
        String kbId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

        // GET — 查询
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/knowledge-bases/{kbId}", WORKSPACE_ID, kbId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(kbId))
                .andExpect(jsonPath("$.kbCode").value(kbCode))
                .andExpect(jsonPath("$.name").value("CRUD KB"));

        // PATCH — 更新
        mockMvc.perform(patch("/api/v1/workspaces/{workspaceId}/knowledge-bases/{kbId}", WORKSPACE_ID, kbId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Updated KB"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(kbId))
                .andExpect(jsonPath("$.name").value("Updated KB"));

        // DELETE — 删除
        mockMvc.perform(delete("/api/v1/workspaces/{workspaceId}/knowledge-bases/{kbId}", WORKSPACE_ID, kbId))
                .andExpect(status().isNoContent());

        // 验证已删除
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/knowledge-bases/{kbId}", WORKSPACE_ID, kbId))
                .andExpect(status().isNotFound());
    }

    // =============================================================
    // GET /api/v1/workspaces/{workspaceId}/knowledge-bases/{kbCode} — 查询不存在的知识库 (404)
    // =============================================================
    @Test
    @Order(8)
    void shouldReturnNotFoundForUnknownKnowledgeBase() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/knowledge-bases/kb-nonexistent", WORKSPACE_ID))
                .andExpect(status().isNotFound());
    }

    // =============================================================
    // POST /api/v1/workspaces/{workspaceId}/knowledge-bases — 创建重复知识库 (409)
    // =============================================================
    @Test
    @Order(9)
    void shouldReturnConflictForDuplicateKnowledgeBase() throws Exception {
        String kbCode = "kb-dup-" + randomShortId();
        // 创建
        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/knowledge-bases", WORKSPACE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                    "kbCode": "%s",
                                    "name": "Dup KB"
                                }
                                """, kbCode)))
                .andExpect(status().isCreated());

        // 再创建同 ID — 409
        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/knowledge-bases", WORKSPACE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                    "kbCode": "%s",
                                    "name": "Duplicate KB"
                                }
                                """, kbCode)))
                .andExpect(status().isConflict());
    }

    // =============================================================
}
