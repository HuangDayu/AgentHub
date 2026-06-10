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

import static com.agenthub.test.common.TestCommonTools.*;
import static com.agenthub.test.common.TestCommonTools.getRequestBuilder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TestAgentHubApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AgentTeamControllerIntegrationTest {
    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private String createdTeamId;
    
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
    void shouldCreateTeam() throws Exception {
        String responseBody = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/teams", WORKSPACE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "teamCode": "test-team-001",
                                    "name": "Test Team",
                                    "description": "Test team description",
                                    "coordinationMode": "SEQUENTIAL",
                                    "memberConfig": "{}"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Test Team"))
                .andReturn().getResponse().getContentAsString();

        createdTeamId = objectMapper.readTree(responseBody).get("id").asText();
    }

    @Test
    @Order(2)
    void shouldListTeams() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/teams", WORKSPACE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(3)
    void shouldGetTeamById() throws Exception {
        Assertions.assertNotNull(createdTeamId, "Team should be created first");
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/teams/{teamId}", WORKSPACE_ID, createdTeamId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdTeamId));
    }

    @Test
    @Order(4)
    void shouldUpdateTeam() throws Exception {
        Assertions.assertNotNull(createdTeamId, "Team should be created first");
        mockMvc.perform(put("/api/v1/workspaces/{workspaceId}/teams/{teamId}", WORKSPACE_ID, createdTeamId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Updated Team",
                                    "description": "Updated description",
                                    "coordinationMode": "PARALLEL",
                                    "memberConfig": "{}"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Team"));
    }

    @Test
    @Order(5)
    void shouldActivateTeam() throws Exception {
        Assertions.assertNotNull(createdTeamId, "Team should be created first");
        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/teams/{teamId}/activate", WORKSPACE_ID, createdTeamId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @Order(6)
    void shouldDeactivateTeam() throws Exception {
        Assertions.assertNotNull(createdTeamId, "Team should be created first");
        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/teams/{teamId}/deactivate", WORKSPACE_ID, createdTeamId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    @Order(7)
    void shouldReturnNotFoundForUnknownId() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/teams/{teamId}", WORKSPACE_ID, "non-existent-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(8)
    void shouldDeleteTeam() throws Exception {
        Assertions.assertNotNull(createdTeamId, "Team should be created first");
        mockMvc.perform(delete("/api/v1/workspaces/{workspaceId}/teams/{teamId}", WORKSPACE_ID, createdTeamId))
                .andExpect(status().isNoContent());
    }
}
