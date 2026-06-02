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
class WorkflowControllerIntegrationTest {
    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private String createdWorkflowId;
    private final String workspaceId = "100000002";
    private final ObjectMapper objectMapper = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .defaultRequest(getRequestBuilder())
                .build();
    }

    @Test
    @Order(1)
    void shouldCreateWorkflow() throws Exception {
        String responseBody = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/dag-workflows", workspaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tenantId": "100000001",
                                    "workspaceId": "100000002",
                                    "workflowCode": "test-workflow-001",
                                    "name": "Test Workflow",
                                    "description": "Test workflow description",
                                    "graphDefinition": "{}"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Test Workflow"))
                .andReturn().getResponse().getContentAsString();

        createdWorkflowId = objectMapper.readTree(responseBody).get("id").asText();
    }

    @Test
    @Order(2)
    void shouldListWorkflows() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/dag-workflows", workspaceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(3)
    void shouldGetWorkflowById() throws Exception {
        Assertions.assertNotNull(createdWorkflowId, "Workflow should be created first");
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/dag-workflows/{workflowId}", workspaceId, createdWorkflowId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdWorkflowId));
    }

    @Test
    @Order(4)
    void shouldUpdateWorkflow() throws Exception {
        Assertions.assertNotNull(createdWorkflowId, "Workflow should be created first");
        mockMvc.perform(put("/api/v1/workspaces/{workspaceId}/dag-workflows/{workflowId}", workspaceId, createdWorkflowId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Updated Workflow",
                                    "description": "Updated description",
                                    "graphDefinition": "{}"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Workflow"));
    }

    @Test
    @Order(5)
    void shouldPublishWorkflow() throws Exception {
        Assertions.assertNotNull(createdWorkflowId, "Workflow should be created first");
        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/dag-workflows/{workflowId}/publish", workspaceId, createdWorkflowId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
    }

    @Test
    @Order(6)
    void shouldUnpublishWorkflow() throws Exception {
        Assertions.assertNotNull(createdWorkflowId, "Workflow should be created first");
        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/dag-workflows/{workflowId}/unpublish", workspaceId, createdWorkflowId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    @Order(7)
    void shouldReturnNotFoundForUnknownId() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/dag-workflows/{workflowId}", workspaceId, "non-existent-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(8)
    void shouldDeleteWorkflow() throws Exception {
        Assertions.assertNotNull(createdWorkflowId, "Workflow should be created first");
        mockMvc.perform(delete("/api/v1/workspaces/{workspaceId}/dag-workflows/{workflowId}", workspaceId, createdWorkflowId))
                .andExpect(status().isNoContent());
    }
}
