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

@SpringBootTest(classes = TestAgentHubApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AgentHubControllerIntegrationTest {

    private String createdAgentId = null;
    private final String workspaceId = "100000002";
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

    @Test
    @Order(1)
    void shouldCreateAgent() throws Exception {
        String responseBody = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/agents", workspaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tenantId": "tenant-001",
                                    "workspaceId": "workspace-001",
                                    "agentCode": "test-agent",
                                    "name": "Test Agent",
                                    "description": "Test agent description"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Test Agent"))
                .andReturn().getResponse().getContentAsString();

        createdAgentId = objectMapper.readTree(responseBody).get("id").asText();
    }

    @Test
    @Order(2)
    void shouldListAgents() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/agents", workspaceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(3)
    void shouldGetAgentById() throws Exception {
        Assertions.assertNotNull(createdAgentId, "Agent should be created first");
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/agents/{agentId}", workspaceId, createdAgentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdAgentId))
                .andExpect(jsonPath("$.name").value("Test Agent"));
    }

    @Test
    @Order(4)
    void shouldUpdateAgent() throws Exception {
        Assertions.assertNotNull(createdAgentId, "Agent should be created first");
        mockMvc.perform(put("/api/v1/workspaces/{workspaceId}/agents/{agentId}", workspaceId, createdAgentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Updated Agent",
                                    "description": "Updated description"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Agent"));
    }

    @Test
    @Order(5)
    void shouldEnabledAgent() throws Exception {
        Assertions.assertNotNull(createdAgentId, "Agent should be created first");
        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/agents/{agentId}/enabled", workspaceId, createdAgentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
    }

    @Test
    @Order(6)
    void shouldUnenabledAgent() throws Exception {
        Assertions.assertNotNull(createdAgentId, "Agent should be created first");
        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/agents/{agentId}/unenabled", workspaceId, createdAgentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    @Order(7)
    void shouldReturnNotFoundForUnknownId() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/agents/{agentId}", workspaceId, "non-existent-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(8)
    void shouldDeleteAgent() throws Exception {
        Assertions.assertNotNull(createdAgentId, "Agent should be created first");
        mockMvc.perform(delete("/api/v1/workspaces/{workspaceId}/agents/{agentId}", workspaceId, createdAgentId))
                .andExpect(status().isNoContent());
    }
}
