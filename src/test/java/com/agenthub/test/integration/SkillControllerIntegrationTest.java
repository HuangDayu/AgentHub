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
    private final String tenantId = "100000001";
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
    void shouldCreateSyncedSkill() throws Exception {
        String skillCode = "test-synced-skill-" + randomShortId();
        var result = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/skills", workspaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                    "tenantId": "100000001",
                                    "skillCode": "%s",
                                    "name": "Test Synced Skill",
                                    "description": "A synced skill for testing",
                                    "skillPath": "C:/Users/huang/.agents/skills/weather"
                                }
                                """, skillCode)))
                .andReturn();

        int status = result.getResponse().getStatus();
        String body = result.getResponse().getContentAsString();
        System.out.println("=== CREATE STATUS: " + status);
        System.out.println("=== CREATE BODY: " + body);
        Assertions.assertEquals(201, status, "Expected 201 but got " + status + ": " + body);
        Assertions.assertTrue(body.contains("id"), "Response should contain id: " + body);
        createdSkillId = objectMapper.readTree(body).get("id").asText();
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
    void shouldSearchSkillsByKeyword() throws Exception {
        Assertions.assertNotNull(createdSkillId, "Skill should be created first");
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/skills/search", workspaceId)
                        .param("keyword", "Test Synced"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$[0].name").value("Test Synced Skill"));
    }

    @Test
    @Order(4)
    void shouldReturnEmptyWhenSearchNoMatch() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/skills/search", workspaceId)
                        .param("keyword", "zzz-no-match-keyword-zzz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @Order(5)
    void shouldGetSkillById() throws Exception {
        Assertions.assertNotNull(createdSkillId, "Skill should be created first");
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/skills/{skillId}", workspaceId, createdSkillId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdSkillId))
                .andExpect(jsonPath("$.skillCode").value(org.hamcrest.Matchers.startsWith("test-synced-skill-")));
    }

    @Test
    @Order(6)
    void shouldUpdateSkill() throws Exception {
        Assertions.assertNotNull(createdSkillId, "Skill should be created first");
        mockMvc.perform(put("/api/v1/workspaces/{workspaceId}/skills/{skillId}", workspaceId, createdSkillId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tenantId": "100000001",
                                    "skillCode": "test-synced-skill",
                                    "name": "Updated Synced Skill",
                                    "description": "Updated description",
                                    "skillPath": "/tmp/test-skill-updated"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Synced Skill"));
    }

    @Test
    @Order(7)
    void shouldEnableSkill() throws Exception {
        Assertions.assertNotNull(createdSkillId, "Skill should be created first");
        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/skills/{skillId}/enable", workspaceId, createdSkillId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    @Order(8)
    void shouldDisableSkill() throws Exception {
        Assertions.assertNotNull(createdSkillId, "Skill should be created first");
        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/skills/{skillId}/disable", workspaceId, createdSkillId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));
    }

    @Test
    @Order(9)
    void shouldSyncAllSkills() throws Exception {
        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/skills/sync-all", workspaceId))
                .andExpect(status().isOk());
    }

    @Test
    @Order(10)
    void shouldReturnNotFoundForUnknownId() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/skills/{skillId}", workspaceId, "non-existent-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(11)
    void shouldDeleteSkill() throws Exception {
        Assertions.assertNotNull(createdSkillId, "Skill should be created first");
        mockMvc.perform(delete("/api/v1/workspaces/{workspaceId}/skills/{skillId}", workspaceId, createdSkillId))
                .andExpect(status().isNoContent());
    }
}
