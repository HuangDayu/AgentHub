package com.agenthub.test.integration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.agenthub.test.TestAgentHubApplication;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static com.agenthub.test.common.TestCommonTools.*;
import static com.agenthub.test.common.TestCommonTools.getRequestBuilder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TestAgentHubApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SystemToolsControllerIntegrationTest {

    private String createdToolId = null;
    
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
    void shouldSyncTools() throws Exception {
        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/system-tools/sync", WORKSPACE_ID))
                .andExpect(status().isOk());
    }

    @Test
    @Order(2)
    void shouldListTools() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/system-tools", WORKSPACE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(3)
    void shouldListEnabledTools() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/system-tools/enabled", WORKSPACE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(4)
    void shouldEnableTool() throws Exception {
        // 先同步确保有工具
        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/system-tools/sync", WORKSPACE_ID))
                .andExpect(status().isOk());
        
        // 获取工具列表
        String responseBody = mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/system-tools", WORKSPACE_ID))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        
        // 如果有工具，测试启用
        if (objectMapper.readTree(responseBody).size() > 0) {
            createdToolId = objectMapper.readTree(responseBody).get(0).get("id").asText();
            
            mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/system-tools/{id}/enable", WORKSPACE_ID, createdToolId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.enabled").value(true));
        }
    }

    @Test
    @Order(5)
    void shouldDisableTool() throws Exception {
        if (createdToolId == null) {
            // 尝试获取一个工具ID
            String responseBody = mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/system-tools", WORKSPACE_ID))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            
            if (objectMapper.readTree(responseBody).size() > 0) {
                createdToolId = objectMapper.readTree(responseBody).get(0).get("id").asText();
            }
        }
        
        if (createdToolId != null) {
            mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/system-tools/{id}/disable", WORKSPACE_ID, createdToolId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.enabled").value(false));
        }
    }

    @Test
    @Order(6)
    void shouldGetToolById() throws Exception {
        if (createdToolId == null) {
            // 尝试获取一个工具ID
            String responseBody = mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/system-tools", WORKSPACE_ID))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            
            if (objectMapper.readTree(responseBody).size() > 0) {
                createdToolId = objectMapper.readTree(responseBody).get(0).get("id").asText();
            }
        }
        
        if (createdToolId != null) {
            mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/system-tools/{id}", WORKSPACE_ID, createdToolId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(createdToolId));
        }
    }

    @Test
    @Order(7)
    void shouldDeleteTool() throws Exception {
        if (createdToolId == null) {
            // 尝试获取一个工具ID
            String responseBody = mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/system-tools", WORKSPACE_ID))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            
            if (objectMapper.readTree(responseBody).size() > 0) {
                createdToolId = objectMapper.readTree(responseBody).get(0).get("id").asText();
            }
        }
        
        if (createdToolId != null) {
            mockMvc.perform(delete("/api/v1/workspaces/{workspaceId}/system-tools/{id}", WORKSPACE_ID, createdToolId))
                    .andExpect(status().isNoContent());
        }
    }
}
