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
class ToolStrategyControllerIntegrationTest {
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
    void shouldCreateToolStrategy() throws Exception {
        String responseBody = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/httpTool-strategies", workspaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "default-httpTool-strategy",
                                    "description": "默认工具策略",
                                    "maxConcurrentCalls": 5,
                                    "timeoutSeconds": 30,
                                    "retryCount": 3,
                                    "fallbackEnabled": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("default-httpTool-strategy"))
                .andReturn().getResponse().getContentAsString();

        createdStrategyId = objectMapper.readTree(responseBody).get("id").asText();
    }

    @Test
    @Order(2)
    void shouldListToolStrategies() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/httpTool-strategies", workspaceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(3)
    void shouldGetToolStrategyById() throws Exception {
        Assertions.assertNotNull(createdStrategyId);

        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/httpTool-strategies/{id}", workspaceId, createdStrategyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdStrategyId));
    }

    @Test
    @Order(4)
    void shouldUpdateToolStrategy() throws Exception {
        Assertions.assertNotNull(createdStrategyId);

        mockMvc.perform(put("/api/v1/workspaces/{workspaceId}/httpTool-strategies/{id}", workspaceId, createdStrategyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "updated-httpTool-strategy",
                                    "description": "更新后的工具策略"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("updated-httpTool-strategy"));
    }

    @Test
    @Order(5)
    void shouldReturnNotFoundForUnknownId() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/httpTool-strategies/{id}", workspaceId, "00000000-0000-0000-0000-000000000001"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(6)
    void shouldDeleteToolStrategy() throws Exception {
        Assertions.assertNotNull(createdStrategyId);

        mockMvc.perform(delete("/api/v1/workspaces/{workspaceId}/httpTool-strategies/{id}", workspaceId, createdStrategyId))
                .andExpect(status().isNoContent());
    }
}
