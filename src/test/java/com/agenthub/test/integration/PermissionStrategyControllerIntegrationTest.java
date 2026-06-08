package com.agenthub.test.integration;

import com.agenthub.test.TestAgentHubApplication;
import com.agenthub.test.common.TestCommonTools;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 权限策略 Controller 集成测试
 */
@SpringBootTest(classes = TestAgentHubApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PermissionStrategyControllerIntegrationTest {
    private final String workspaceId = "100000002";
    private final String tenantId = "100000002";
    private String policyId;

    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockMvc mockMvc;

    @org.junit.jupiter.api.BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .defaultRequest(TestCommonTools.getRequestBuilder())
                .build();
    }

    @Test
    @Order(1)
    void shouldCreatePermissionStrategy() throws Exception {
        String body = mockMvc.perform(put("/api/v1/workspaces/{w}/permission-strategies", workspaceId)
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "test-policy-%s",
                                    "description": "integration test",
                                    "allowedRoles": ["admin", "user"],
                                    "allowedOperations": ["CREATE", "READ"],
                                    "rateLimitPerMinute": 60,
                                    "rateLimitPerHour": 1000,
                                    "dangerousSqlBlock": true
                                }
                                """.formatted(java.util.UUID.randomUUID().toString().substring(0, 8))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        policyId = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(body).get("id").asText();
    }

    @Test
    @Order(2)
    void shouldGetPermissionStrategy() throws Exception {
        org.junit.jupiter.api.Assertions.assertNotNull(policyId);
        mockMvc.perform(get("/api/v1/workspaces/{w}/permission-strategies/{id}", workspaceId, policyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(policyId));
    }

    @Test
    @Order(3)
    void shouldListPermissionStrategies() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces/{w}/permission-strategies", workspaceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(4)
    void shouldUpdatePermissionStrategy() throws Exception {
        org.junit.jupiter.api.Assertions.assertNotNull(policyId);
        mockMvc.perform(put("/api/v1/workspaces/{w}/permission-strategies", workspaceId)
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "id": "%s",
                                    "name": "test-policy-%s",
                                    "description": "updated",
                                    "allowedRoles": ["admin"],
                                    "allowedOperations": ["READ"],
                                    "rateLimitPerMinute": 30,
                                    "rateLimitPerHour": 500,
                                    "dangerousSqlBlock": true
                                }
                                """.formatted(policyId, java.util.UUID.randomUUID().toString().substring(0, 8))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("updated"));
    }
}
