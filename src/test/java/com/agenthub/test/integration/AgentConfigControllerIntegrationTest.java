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

class AgentConfigControllerIntegrationTest {

    private String createdConfigId = null;
    private final String agentId = "100000002";
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
    void shouldSetAgentConfig() throws Exception {
        String responseBody = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/agents/{agentId}/configs", workspaceId, agentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "category": "STRATEGY",
                                    "type": "RETRIEVAL_STRATEGY",
                                    "configId": "retrieval-strategy-001",
                                    "description": "Default retrieval strategy",
                                    "priority": 1,
                                    "enabled": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.category").value("STRATEGY"))
                .andExpect(jsonPath("$.type").value("RETRIEVAL_STRATEGY"))
                .andReturn().getResponse().getContentAsString();

        createdConfigId = objectMapper.readTree(responseBody).get("id").asText();
    }

    @Test
    @Order(2)
    void shouldListAgentConfigs() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/agents/{agentId}/configs", workspaceId, agentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(3)
    void shouldListConfigsByCategory() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/agents/{agentId}/configs", workspaceId, agentId)
                        .param("category", "STRATEGY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(4)
    void shouldGetAgentConfigById() throws Exception {
        Assertions.assertNotNull(createdConfigId, "Config should be created first");
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/agents/{agentId}/configs/{id}", workspaceId, agentId, createdConfigId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdConfigId))
                .andExpect(jsonPath("$.category").value("STRATEGY"));
    }

    @Test
    @Order(5)
    void shouldUpdateExistingConfig() throws Exception {
        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/agents/{agentId}/configs", workspaceId, agentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "category": "STRATEGY",
                                    "type": "RETRIEVAL_STRATEGY",
                                    "configId": "retrieval-strategy-002",
                                    "description": "Updated retrieval strategy",
                                    "priority": 2,
                                    "enabled": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.configId").value("retrieval-strategy-002"));
    }

    @Test
    @Order(6)
    void shouldSetMultipleConfigs() throws Exception {
        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/agents/{agentId}/configs", workspaceId, agentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "category": "STRATEGY",
                                    "type": "MODEL_STRATEGY",
                                    "configId": "model-strategy-001",
                                    "description": "Model strategy config",
                                    "priority": 1,
                                    "enabled": true
                                }
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    @Order(7)
    void shouldReturnNotFoundForUnknownId() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/agents/{agentId}/configs/{id}", workspaceId, agentId, "non-existent-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(8)
    void shouldDeleteAgentConfig() throws Exception {
        Assertions.assertNotNull(createdConfigId, "Config should be created first");
        mockMvc.perform(delete("/api/v1/workspaces/{workspaceId}/agents/{agentId}/configs/{id}", workspaceId, agentId, createdConfigId))
                .andExpect(status().isNoContent());
    }
}
