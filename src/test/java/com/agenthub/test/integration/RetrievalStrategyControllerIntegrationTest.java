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
class RetrievalStrategyControllerIntegrationTest {

    private String createdStrategyId = null;
    private final String workspaceId = "100000002";
    private final ObjectMapper objectMapper = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);

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
    void shouldCreateRetrievalStrategy() throws Exception {
        String responseBody = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/retrieval-strategies", workspaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "hybrid-retrieval",
                                    "description": "混合检索策略",
                                    "retrievalType": "HYBRID",
                                    "topK": 10,
                                    "similarityThreshold": 0.75,
                                    "rerankEnabled": true,
                                    "rerankModel": "bge-reranker",
                                    "vectorWeight": 0.7,
                                    "keywordWeight": 0.3
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("hybrid-retrieval"))
                .andExpect(jsonPath("$.retrievalType").value("HYBRID"))
                .andReturn().getResponse().getContentAsString();

        createdStrategyId = objectMapper.readTree(responseBody).get("id").asText();
    }

    @Test
    @Order(2)
    void shouldListRetrievalStrategies() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/retrieval-strategies", workspaceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(3)
    void shouldGetRetrievalStrategyById() throws Exception {
        Assertions.assertNotNull(createdStrategyId);

        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/retrieval-strategies/{id}", workspaceId, createdStrategyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdStrategyId))
                .andExpect(jsonPath("$.name").value("hybrid-retrieval"));
    }

    @Test
    @Order(4)
    void shouldUpdateRetrievalStrategy() throws Exception {
        Assertions.assertNotNull(createdStrategyId);

        mockMvc.perform(put("/api/v1/workspaces/{workspaceId}/retrieval-strategies/{id}", workspaceId, createdStrategyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "updated-retrieval",
                                    "description": "更新后的检索策略"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("updated-retrieval"));
    }

    @Test
    @Order(5)
    void shouldReturnNotFoundForUnknownId() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/retrieval-strategies/{id}", workspaceId, "00000000-0000-0000-0000-000000000001"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(6)
    void shouldDeleteRetrievalStrategy() throws Exception {
        Assertions.assertNotNull(createdStrategyId);

        mockMvc.perform(delete("/api/v1/workspaces/{workspaceId}/retrieval-strategies/{id}", workspaceId, createdStrategyId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/retrieval-strategies/{id}", workspaceId, createdStrategyId))
                .andExpect(status().isNotFound());
    }
}
