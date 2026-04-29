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
class ModelStrategyControllerIntegrationTest {

    private String createdStrategyId = null;
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
    void shouldCreateModelStrategy() throws Exception {
        String responseBody = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/model-strategies", workspaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "default-model-strategy",
                                    "description": "默认模型策略",
                                    "temperature": 0.7,
                                    "maxTokens": 2048,
                                    "topP": 1.0,
                                    "frequencyPenalty": 0.0,
                                    "presencePenalty": 0.0
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("default-model-strategy"))
                .andReturn().getResponse().getContentAsString();

        createdStrategyId = objectMapper.readTree(responseBody).get("id").asText();
    }

    @Test
    @Order(2)
    void shouldListModelStrategies() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/model-strategies", workspaceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(3)
    void shouldGetModelStrategyById() throws Exception {
        Assertions.assertNotNull(createdStrategyId);

        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/model-strategies/{id}", workspaceId, createdStrategyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdStrategyId));
    }

    @Test
    @Order(4)
    void shouldUpdateModelStrategy() throws Exception {
        Assertions.assertNotNull(createdStrategyId);

        mockMvc.perform(put("/api/v1/workspaces/{workspaceId}/model-strategies/{id}", workspaceId, createdStrategyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "updated-model-strategy",
                                    "description": "更新后的模型策略"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("updated-model-strategy"));
    }

    @Test
    @Order(5)
    void shouldReturnNotFoundForUnknownId() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/model-strategies/{id}", workspaceId, "00000000-0000-0000-0000-000000000001"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(6)
    void shouldDeleteModelStrategy() throws Exception {
        Assertions.assertNotNull(createdStrategyId);

        mockMvc.perform(delete("/api/v1/workspaces/{workspaceId}/model-strategies/{id}", workspaceId, createdStrategyId))
                .andExpect(status().isNoContent());
    }
}
