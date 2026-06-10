package com.agenthub.test.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.agenthub.test.TestAgentHubApplication;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static com.agenthub.test.common.TestCommonTools.getRequestBuilder;
import static com.agenthub.test.common.TestCommonTools.WORKSPACE_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TestAgentHubApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AgentConfigTypeControllerIntegrationTest {
    @Autowired
    private WebApplicationContext context;
    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeAll
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .defaultRequest(getRequestBuilder())
                .build();
    }

    @Test
    @Order(1)
    void getConfigTypes_shouldReturnAllCategories() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/agent-config-types", WORKSPACE_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        verifyConfigTypes(result);
    }

    private void verifyConfigTypes(MvcResult result) throws Exception {
        String json = result.getResponse().getContentAsString();
        List<?> types = objectMapper.readValue(json, List.class);
        assertThat(json).contains("STRATEGY", "TOOL", "PROMPT", "MODEL");
    }

    @Test
    @Order(2)
    void getAvailableConfigs_retrieval_shouldReturnStrategies() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/agent-config-types/available", WORKSPACE_ID)
                        .param("type", "RETRIEVAL_STRATEGY")
                        .param("category", "STRATEGY")
                        .param("workspaceId", WORKSPACE_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    @Order(3)
    void getAvailableConfigs_modelStrategy_shouldReturnStrategies() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/agent-config-types/available", WORKSPACE_ID)
                        .param("type", "MODEL_STRATEGY")
                        .param("category", "STRATEGY")
                        .param("workspaceId", WORKSPACE_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    @Order(4)
    void getAvailableConfigs_toolStrategy_shouldReturnStrategies() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/agent-config-types/available", WORKSPACE_ID)
                        .param("type", "TOOL_STRATEGY")
                        .param("category", "STRATEGY")
                        .param("workspaceId", WORKSPACE_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    @Order(5)
    void getAvailableConfigs_guardrail_shouldReturnStrategies() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/agent-config-types/available", WORKSPACE_ID)
                        .param("type", "GUARDRAIL_STRATEGY")
                        .param("category", "STRATEGY")
                        .param("workspaceId", WORKSPACE_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    @Order(6)
    void getAvailableConfigs_chatModel_shouldReturnModelConfigs() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/agent-config-types/available", WORKSPACE_ID)
                        .param("type", "CHAT_MODEL")
                        .param("category", "MODEL")
                        .param("workspaceId", WORKSPACE_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    @Order(7)
    void getAvailableConfigs_withoutWorkspaceId_shouldReturnEmpty() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/agent-config-types/available", WORKSPACE_ID)
                        .param("type", "RETRIEVAL_STRATEGY")
                        .param("category", "STRATEGY")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @Order(8)
    void getAvailableConfigs_unknownType_shouldReturnEmpty() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/agent-config-types/available", WORKSPACE_ID)
                        .param("type", "UNKNOWN")
                        .param("category", "UNKNOWN")
                        .param("workspaceId", WORKSPACE_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andReturn();
        String json = result.getResponse().getContentAsString();
    }

    private void verifyAvailableConfigs(MvcResult result, String expectedId) throws Exception {
        String json = result.getResponse().getContentAsString();
        List<?> configs = objectMapper.readValue(json, List.class);
        assertThat(configs).isNotEmpty();
        assertThat(json).contains(expectedId);
    }
}
