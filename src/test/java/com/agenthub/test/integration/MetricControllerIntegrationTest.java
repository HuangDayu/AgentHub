package com.agenthub.test.integration;

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

/**
 * Metric Controller 集成测试.
 */
@SpringBootTest(classes = TestAgentHubApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MetricControllerIntegrationTest {

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
    void shouldCreateMetric() throws Exception {
        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/metrics", WORKSPACE_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "metricType": "LATENCY_NS",
                    "metricName": "request_latency",
                    "metricValue": 1234567.0,
                    "runId": "test-run-001",
                    "agentId": "test-agent-001"
                }
                """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.metricType").value("LATENCY_NS"))
            .andExpect(jsonPath("$.metricName").value("request_latency"));
    }

    @Test
    @Order(2)
    void shouldListMetrics() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/metrics", WORKSPACE_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(3)
    void shouldListMetricsByRun() throws Exception {
        String runId = "test-run-001";
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/metrics/runs/{runId}", WORKSPACE_ID, runId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(4)
    void shouldListMetricsByAgent() throws Exception {
        String agentId = "test-agent-001";
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/metrics/agents/{agentId}", WORKSPACE_ID, agentId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(5)
    void shouldListMetricsByType() throws Exception {
        String metricType = "LATENCY_NS";
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/metrics/types/{metricType}", WORKSPACE_ID, metricType))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(6)
    void shouldDeleteMetric() throws Exception {
        String id = "test-metric-to-delete";
        mockMvc.perform(delete("/api/v1/workspaces/{workspaceId}/metrics/{id}", WORKSPACE_ID, id))
            .andExpect(status().isOk());
    }
}
