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
class McpToolControllerIntegrationTest {

    private String createdToolId = null;
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
    void shouldCreateMcpTool() throws Exception {
        String shortId = randomShortId();
        String responseBody = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/mcp-tools", workspaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                    "name": "filesystem-mcp-%s",
                                    "description": "File system MCP server",
                                    "serverUrl": "/usr/local/bin/mcp-filesystem",
                                    "serverType": "STDIO",
                                    "command": "node",
                                    "args": ["server.js"],
                                    "env": {"NODE_ENV": "production"},
                                    "enabled": true
                                }
                                """, shortId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("filesystem-mcp-" + shortId))
                .andReturn().getResponse().getContentAsString();

        createdToolId = objectMapper.readTree(responseBody).get("id").asText();
    }

    @Test
    @Order(2)
    void shouldListMcpTools() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/mcp-tools", workspaceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(3)
    void shouldGetMcpToolById() throws Exception {
        Assertions.assertNotNull(createdToolId);

        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/mcp-tools/{id}", workspaceId, createdToolId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdToolId));
    }

    @Test
    @Order(4)
    void shouldUpdateMcpTool() throws Exception {
        Assertions.assertNotNull(createdToolId);

        mockMvc.perform(put("/api/v1/workspaces/{workspaceId}/mcp-tools/{id}", workspaceId, createdToolId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "updated-mcp",
                                    "description": "Updated MCP server",
                                    "serverUrl": "/usr/local/bin/mcp-updated",
                                    "serverType": "HTTP",
                                    "enabled": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("updated-mcp"));
    }

    @Test
    @Order(5)
    void shouldReturnNotFoundForUnknownId() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/mcp-tools/{id}", workspaceId, "100000002"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(6)
    void shouldDeleteMcpTool() throws Exception {
        Assertions.assertNotNull(createdToolId);

        mockMvc.perform(delete("/api/v1/workspaces/{workspaceId}/mcp-tools/{id}", workspaceId, createdToolId))
                .andExpect(status().isNoContent());
    }
}
