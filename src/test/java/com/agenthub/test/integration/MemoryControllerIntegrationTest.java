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
class MemoryControllerIntegrationTest {
    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private String createdMemoryId;
    private final String workspaceId = "100000002";
    private final String agentId = "test-agent";
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
    void shouldCreateMemory() throws Exception {
        String responseBody = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/memories", workspaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tenantId": "100000001",
                                    "workspaceId": "100000002",
                                    "agentId": "test-agent",
                                    "memoryType": "EPISODIC",
                                    "content": "Test memory content",
                                    "metadata": "{}",
                                    "importance": 0.8
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.content").value("Test memory content"))
                .andReturn().getResponse().getContentAsString();

        createdMemoryId = objectMapper.readTree(responseBody).get("id").asText();
    }

    @Test
    @Order(2)
    void shouldGetMemoryById() throws Exception {
        Assertions.assertNotNull(createdMemoryId, "Memory should be created first");
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/memories/{memoryId}", workspaceId, createdMemoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdMemoryId));
    }

    @Test
    @Order(3)
    void shouldListMemoriesByAgent() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/memories/agents/{agentId}", workspaceId, agentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(4)
    void shouldUpdateMemory() throws Exception {
        Assertions.assertNotNull(createdMemoryId, "Memory should be created first");
        mockMvc.perform(put("/api/v1/workspaces/{workspaceId}/memories/{memoryId}", workspaceId, createdMemoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "content": "Updated memory content",
                                    "metadata": "{}",
                                    "importance": 0.9
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Updated memory content"));
    }

    @Test
    @Order(5)
    void shouldReturnNotFoundForUnknownId() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/memories/{memoryId}", workspaceId, "non-existent-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(6)
    void shouldDeleteMemory() throws Exception {
        Assertions.assertNotNull(createdMemoryId, "Memory should be created first");
        mockMvc.perform(delete("/api/v1/workspaces/{workspaceId}/memories/{memoryId}", workspaceId, createdMemoryId))
                .andExpect(status().isNoContent());
    }
}
