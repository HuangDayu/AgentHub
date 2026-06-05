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
class SkillConfigControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private String createdConfigId;
    private final String workspaceId = "100000002";
    private final String tenantId = "100000002";
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
    void shouldCreateSkillConfig() throws Exception {
        String responseBody = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/skill-configs", workspaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tenantId": "100000002",
                                    "name": "Test Skill Config",
                                    "description": "A test skill config",
                                    "skillPaths": ["/path/to/skills", "/another/path"],
                                    "syncEnabled": true,
                                    "syncInterval": 3600,
                                    "autoSync": false
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Test Skill Config"))
                .andExpect(jsonPath("$.skillPaths").isArray())
                .andExpect(jsonPath("$.syncEnabled").value(true))
                .andReturn().getResponse().getContentAsString();

        createdConfigId = objectMapper.readTree(responseBody).get("id").asText();
    }

    @Test
    @Order(2)
    void shouldListSkillConfigs() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/skill-configs", workspaceId)
                        .param("tenantId", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(3)
    void shouldGetSkillConfigById() throws Exception {
        Assertions.assertNotNull(createdConfigId, "Config should be created first");
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/skill-configs/{configId}", workspaceId, createdConfigId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdConfigId))
                .andExpect(jsonPath("$.name").value("Test Skill Config"));
    }

    @Test
    @Order(4)
    void shouldUpdateSkillConfig() throws Exception {
        Assertions.assertNotNull(createdConfigId, "Config should be created first");
        mockMvc.perform(put("/api/v1/workspaces/{workspaceId}/skill-configs/{configId}", workspaceId, createdConfigId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tenantId": "100000002",
                                    "name": "Updated Skill Config",
                                    "description": "Updated description",
                                    "skillPaths": ["/updated/path"],
                                    "syncEnabled": false,
                                    "syncInterval": 7200,
                                    "autoSync": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Skill Config"))
                .andExpect(jsonPath("$.syncEnabled").value(false))
                .andExpect(jsonPath("$.autoSync").value(true));
    }

    @Test
    @Order(5)
    void shouldAddSkillPath() throws Exception {
        Assertions.assertNotNull(createdConfigId, "Config should be created first");
        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/skill-configs/{configId}/paths", workspaceId, createdConfigId)
                        .param("path", "/new/path"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.skillPaths").isArray());
    }

    @Test
    @Order(6)
    void shouldRemoveSkillPath() throws Exception {
        Assertions.assertNotNull(createdConfigId, "Config should be created first");
        mockMvc.perform(delete("/api/v1/workspaces/{workspaceId}/skill-configs/{configId}/paths", workspaceId, createdConfigId)
                        .param("path", "/new/path"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.skillPaths").isArray());
    }

    @Test
    @Order(7)
    void shouldReturnNotFoundForUnknownId() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/skill-configs/{configId}", workspaceId, "non-existent-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(8)
    void shouldDeleteSkillConfig() throws Exception {
        Assertions.assertNotNull(createdConfigId, "Config should be created first");
        mockMvc.perform(delete("/api/v1/workspaces/{workspaceId}/skill-configs/{configId}", workspaceId, createdConfigId))
                .andExpect(status().isNoContent());
    }
}
