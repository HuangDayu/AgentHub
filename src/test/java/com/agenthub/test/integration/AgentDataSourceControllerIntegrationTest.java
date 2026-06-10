package com.agenthub.test.integration;

import com.agenthub.test.TestAgentHubApplication;
import com.agenthub.test.common.TestCommonTools;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static com.agenthub.test.common.TestCommonTools.TENANT_ID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static com.agenthub.test.common.TestCommonTools.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TestAgentHubApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AgentDataSourceControllerIntegrationTest {

    
    
    private final String dsName = "test-ds-" + System.currentTimeMillis();
    private String createdId;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .defaultRequest(TestCommonTools.getRequestBuilder())
                .build();
    }

    @Test
    @Order(1)
    void shouldCreateAgentDataSource() throws Exception {
        String body = mockMvc.perform(post("/api/v1/workspaces/{w}/agent-data-sources", WORKSPACE_ID)
                        .header("X-Tenant-Id", TENANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                    "name": "%s",
                                    "description": "test description",
                                    "protocol": "JDBC",
                                    "endpointUri": "jdbc:postgresql://localhost:5432/test",
                                    "propertiesJson": "{}",
                                    "permissionPolicyId": null,
                                    "schemaId": null
                                }
                                """, dsName)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value(dsName))
                .andExpect(jsonPath("$.protocol").value("JDBC"))
                .andExpect(jsonPath("$.enabled").value(false))
                .andReturn().getResponse().getContentAsString();
        createdId = objectMapper.readTree(body).get("id").asText();
    }

    @Test
    @Order(2)
    void shouldListAgentDataSources() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces/{w}/agent-data-sources", WORKSPACE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(3)
    void shouldGetAgentDataSourceById() throws Exception {
        Assertions.assertNotNull(createdId);
        mockMvc.perform(get("/api/v1/workspaces/{w}/agent-data-sources/{id}", WORKSPACE_ID, createdId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdId));
    }

    @Test
    @Order(4)
    void shouldUpdateAgentDataSource() throws Exception {
        Assertions.assertNotNull(createdId);
        mockMvc.perform(patch("/api/v1/workspaces/{w}/agent-data-sources/{id}", WORKSPACE_ID, createdId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "description": "updated description"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("updated description"));
    }

    @Test
    @Order(5)
    void shouldEnableAgentDataSource() throws Exception {
        Assertions.assertNotNull(createdId);
        mockMvc.perform(post("/api/v1/workspaces/{w}/agent-data-sources/{id}/enable", WORKSPACE_ID, createdId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    @Order(6)
    void shouldTestAgentDataSource() throws Exception {
        Assertions.assertNotNull(createdId);
        mockMvc.perform(post("/api/v1/workspaces/{w}/agent-data-sources/{id}/test", WORKSPACE_ID, createdId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").exists());
    }

    @Test
    @Order(7)
    void shouldInvokeAgentDataSource() throws Exception {
        Assertions.assertNotNull(createdId);
        mockMvc.perform(post("/api/v1/workspaces/{w}/agent-data-sources/{id}/invoke", WORKSPACE_ID, createdId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userId": "user-001",
                                    "agentId": "agent-001",
                                    "sessionId": "session-001",
                                    "body": {"sql": "SELECT 1"},
                                    "headers": {"X-Trace": "trace-1"}
                                }
                                """))
                .andExpect(result -> {
                    int sc = result.getResponse().getStatus();
                    Assertions.assertTrue(sc == 200 || sc == 500 || sc == 502 || sc == 503,
                        "expected 200 (real backend) or 5xx (stubbed), got " + sc);
                });
    }

    @Test
    @Order(8)
    void shouldDisableAgentDataSource() throws Exception {
        Assertions.assertNotNull(createdId);
        mockMvc.perform(post("/api/v1/workspaces/{w}/agent-data-sources/{id}/disable", WORKSPACE_ID, createdId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));
    }

    @Test
    @Order(9)
    void shouldReturnNotFoundForUnknownId() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces/{w}/agent-data-sources/{id}", WORKSPACE_ID, "non-existent"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(10)
    void shouldDeleteAgentDataSource() throws Exception {
        Assertions.assertNotNull(createdId);
        mockMvc.perform(delete("/api/v1/workspaces/{w}/agent-data-sources/{id}", WORKSPACE_ID, createdId))
                .andExpect(status().isNoContent());
    }
}
