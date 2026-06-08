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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 审计日志 Controller 集成测试
 */
@SpringBootTest(classes = TestAgentHubApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuditLogControllerIntegrationTest {
    private final String tenantId = "100000002";

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
    void shouldListResourceTypes() throws Exception {
        mockMvc.perform(get("/api/v1/audit-logs/resource-types")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@ == 'DATA_SOURCE')]").exists());
    }

    @Test
    @Order(2)
    void shouldListActions() throws Exception {
        mockMvc.perform(get("/api/v1/audit-logs/actions")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(3)
    void shouldQueryAuditLogs() throws Exception {
        mockMvc.perform(get("/api/v1/audit-logs")
                        .header("X-Tenant-Id", tenantId)
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.total").exists())
                .andExpect(jsonPath("$.page").value(1));
    }

    @Test
    @Order(4)
    void shouldQueryAuditLogsByResource() throws Exception {
        mockMvc.perform(get("/api/v1/audit-logs")
                        .header("X-Tenant-Id", tenantId)
                        .param("resourceType", "DATA_SOURCE")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());
    }
}
