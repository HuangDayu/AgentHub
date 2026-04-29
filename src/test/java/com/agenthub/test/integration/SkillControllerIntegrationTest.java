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
class SkillControllerIntegrationTest {
    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private String createdSkillId;
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
    void shouldCreateSkill() throws Exception {
        String responseBody = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/skills", workspaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tenantId": "100000001",
                                    "workspaceId": "100000002",
                                    "skillCode": "test-skill-001",
                                    "name": "Test Skill",
                                    "description": "Test skill description",
                                    "skillType": "FUNCTION",
                                    "definition": "{}",
                                    "parameters": "{}"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Test Skill"))
                .andReturn().getResponse().getContentAsString();

        createdSkillId = objectMapper.readTree(responseBody).get("id").asText();
    }

    @Test
    @Order(2)
    void shouldListSkills() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/skills", workspaceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(3)
    void shouldGetSkillById() throws Exception {
        Assertions.assertNotNull(createdSkillId, "Skill should be created first");
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/skills/{skillId}", workspaceId, createdSkillId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdSkillId));
    }

    @Test
    @Order(4)
    void shouldUpdateSkill() throws Exception {
        Assertions.assertNotNull(createdSkillId, "Skill should be created first");
        mockMvc.perform(put("/api/v1/workspaces/{workspaceId}/skills/{skillId}", workspaceId, createdSkillId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Updated Skill",
                                    "description": "Updated description",
                                    "definition": "{}",
                                    "parameters": "{}"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Skill"));
    }

    @Test
    @Order(5)
    void shouldEnableSkill() throws Exception {
        Assertions.assertNotNull(createdSkillId, "Skill should be created first");
        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/skills/{skillId}/enable", workspaceId, createdSkillId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    @Order(6)
    void shouldDisableSkill() throws Exception {
        Assertions.assertNotNull(createdSkillId, "Skill should be created first");
        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/skills/{skillId}/disable", workspaceId, createdSkillId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));
    }

    @Test
    @Order(7)
    void shouldReturnNotFoundForUnknownId() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/skills/{skillId}", workspaceId, "non-existent-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(8)
    void shouldDeleteSkill() throws Exception {
        Assertions.assertNotNull(createdSkillId, "Skill should be created first");
        mockMvc.perform(delete("/api/v1/workspaces/{workspaceId}/skills/{skillId}", workspaceId, createdSkillId))
                .andExpect(status().isNoContent());
    }
}
