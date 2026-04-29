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
class PromptTemplateControllerIntegrationTest {

    private String createdTemplateId = null;
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
    void shouldCreatePromptTemplate() throws Exception {
        String shortId = randomShortId();
        String responseBody = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/prompt-templates", workspaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                    "name": "system-content-%s",
                                    "description": "System content template",
                                    "category": "system",
                                    "content": "You are a helpful assistant.",
                                    "variables": [],
                                    "isActive": true
                                }
                                """, shortId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("system-content-" + shortId))
                .andReturn().getResponse().getContentAsString();

        createdTemplateId = objectMapper.readTree(responseBody).get("id").asText();
    }

    @Test
    @Order(2)
    void shouldListPromptTemplates() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/prompt-templates", workspaceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(3)
    void shouldGetPromptTemplateById() throws Exception {
        Assertions.assertNotNull(createdTemplateId, "Template should be created first");
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/prompt-templates/{id}", workspaceId, createdTemplateId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdTemplateId));
    }

    @Test
    @Order(4)
    void shouldUpdatePromptTemplate() throws Exception {
        Assertions.assertNotNull(createdTemplateId, "Template should be created first");
        mockMvc.perform(put("/api/v1/workspaces/{workspaceId}/prompt-templates/{id}", workspaceId, createdTemplateId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "updated-content",
                                    "description": "Updated description",
                                    "category": "system",
                                    "content": "You are an expert assistant.",
                                    "variables": [],
                                    "isActive": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("updated-content"))
                .andExpect(jsonPath("$.content").value("You are an expert assistant."));
    }

    @Test
    @Order(5)
    void shouldReturnNotFoundForUnknownId() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/prompt-templates/{id}", workspaceId, "non-existent-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(6)
    void shouldDeletePromptTemplate() throws Exception {
        Assertions.assertNotNull(createdTemplateId, "Template should be created first");
        mockMvc.perform(delete("/api/v1/workspaces/{workspaceId}/prompt-templates/{id}", workspaceId, createdTemplateId))
                .andExpect(status().isNoContent());
    }
}
