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

import static com.agenthub.common.utils.RandomUtils.randomShortId;
import static com.agenthub.test.common.TestCommonTools.getRequestBuilder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 子智能体集成测试。
 *
 * <p>Subagent 由 Agent 运行时自动创建，不通过 REST API 创建。
 * 测试覆盖 Subsession 管理（通过 SessionController）和 Subagent 监控查询。</p>
 */
@SpringBootTest(classes = TestAgentHubApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SubagentIntegrationTest {

    private String parentAgentId;
    private String subagentId;
    private String subagentIdWithSession;
    private String sessionId;
    private String subsessionId;
    private String autoSubsessionId;
    private final String workspaceId = "100000002";
    private final String uniqueSuffix = randomShortId();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

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

    // ── 准备：创建Agent和Session ────────────────────────────

    @Test
    @Order(1)
    void shouldCreateParentAgent() throws Exception {
        String responseBody = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/agents", workspaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "agentCode": "parent-agent-%s",
                                    "name": "Parent Agent %s",
                                    "description": "Parent agent for subagent testing"
                                }
                                """.formatted(uniqueSuffix, uniqueSuffix)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Parent Agent " + uniqueSuffix))
                .andReturn().getResponse().getContentAsString();

        parentAgentId = objectMapper.readTree(responseBody).get("id").asText();
    }

    @Test
    @Order(2)
    void shouldCreateSessionForParentAgent() throws Exception {
        Assertions.assertNotNull(parentAgentId, "Parent agent should be created first");

        String responseBody = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/agents/{agentId}/sessions",
                        workspaceId, parentAgentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "name": "Test Session %s" }
                                """.formatted(uniqueSuffix)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn().getResponse().getContentAsString();

        sessionId = objectMapper.readTree(responseBody).get("id").asText();
    }

    // ── Subagent 监控查询 ────────────────────────────────────

    @Test
    @Order(3)
    void shouldListSubagentsWhenEmpty() throws Exception {
        Assertions.assertNotNull(parentAgentId, "Parent agent should be created first");

        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/agents/{agentId}/subagents",
                        workspaceId, parentAgentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ── Subsession 管理（通过 SessionController）──────────────

    @Test
    @Order(4)
    void shouldCreateSubsession() throws Exception {
        Assertions.assertNotNull(sessionId, "Session should be created first");

        // 使用一个占位的subagentId测试subsession创建流程
        String testSubagentId = "test-subagent-" + System.currentTimeMillis();

        String responseBody = mockMvc.perform(post(
                        "/api/v1/workspaces/{workspaceId}/agents/{agentId}/sessions/{sessionId}/subsessions",
                        workspaceId, parentAgentId, sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "subagentId": "%s",
                                    "name": "Test Subsession %s"
                                }
                                """.formatted(testSubagentId, uniqueSuffix)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.parentSessionId").value(sessionId))
                .andExpect(jsonPath("$.subagentId").value(testSubagentId))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn().getResponse().getContentAsString();

        subsessionId = objectMapper.readTree(responseBody).get("id").asText();
        subagentId = testSubagentId;
    }

    @Test
    @Order(5)
    void shouldListSubsessions() throws Exception {
        Assertions.assertNotNull(sessionId, "Session should be created first");

        mockMvc.perform(get(
                        "/api/v1/workspaces/{workspaceId}/agents/{agentId}/sessions/{sessionId}/subsessions",
                        workspaceId, parentAgentId, sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].subagentId").value(subagentId));
    }

    @Test
    @Order(6)
    void shouldGetSubsessionById() throws Exception {
        Assertions.assertNotNull(subsessionId, "Subsession should be created first");
        Assertions.assertNotNull(sessionId, "Session should exist");

        mockMvc.perform(get(
                        "/api/v1/workspaces/{workspaceId}/agents/{agentId}/sessions/{sessionId}/subsessions/{subsessionId}",
                        workspaceId, parentAgentId, sessionId, subsessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(subsessionId))
                .andExpect(jsonPath("$.parentSessionId").value(sessionId))
                .andExpect(jsonPath("$.subagentId").value(subagentId))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @Order(7)
    void shouldCloseSubsession() throws Exception {
        Assertions.assertNotNull(subsessionId, "Subsession should be created first");
        Assertions.assertNotNull(sessionId, "Session should exist");

        mockMvc.perform(post(
                        "/api/v1/workspaces/{workspaceId}/agents/{agentId}/sessions/{sessionId}/subsessions/{subsessionId}/close",
                        workspaceId, parentAgentId, sessionId, subsessionId))
                .andExpect(status().isNoContent());

        // Verify status changed to CLOSED
        mockMvc.perform(get(
                        "/api/v1/workspaces/{workspaceId}/agents/{agentId}/sessions/{sessionId}/subsessions/{subsessionId}",
                        workspaceId, parentAgentId, sessionId, subsessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));
    }

    // ── 404 场景 ─────────────────────────────────────────────

    @Test
    @Order(8)
    void shouldReturnNotFoundForUnknownSubsession() throws Exception {
        mockMvc.perform(get(
                        "/api/v1/workspaces/{workspaceId}/agents/{agentId}/sessions/{sessionId}/subsessions/{subsessionId}",
                        workspaceId, parentAgentId, sessionId, "non-existent-id"))
                .andExpect(status().isNotFound());
    }

    // ── 清理 ─────────────────────────────────────────────────

    @Test
    @Order(9)
    void shouldDeleteParentAgent() throws Exception {
        Assertions.assertNotNull(parentAgentId, "Parent agent should be created first");

        mockMvc.perform(delete(
                        "/api/v1/workspaces/{workspaceId}/agents/{agentId}",
                        workspaceId, parentAgentId))
                .andExpect(status().isNoContent());
    }
}
