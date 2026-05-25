package com.agenthub.test.integration;

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

/**
 * Alert Controller 集成测试.
 */
@SpringBootTest(classes = TestAgentHubApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AlertControllerIntegrationTest {

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
    void shouldCreateAlert() throws Exception {
        mockMvc.perform(post("/api/v1/alerts")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "alertLevel": "ERROR",
                    "alertType": "PERFORMANCE",
                    "title": "High Latency Alert",
                    "message": "Request latency exceeded threshold",
                    "runId": "test-run-001"
                }
                """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.alertLevel").value("ERROR"))
            .andExpect(jsonPath("$.alertType").value("PERFORMANCE"));
    }

    @Test
    @Order(2)
    void shouldListAlerts() throws Exception {
        mockMvc.perform(get("/api/v1/alerts"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(3)
    void shouldListAlertsByRun() throws Exception {
        String runId = "test-run-001";
        mockMvc.perform(get("/api/v1/alerts/runs/{runId}", runId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(4)
    void shouldListUnresolvedAlerts() throws Exception {
        mockMvc.perform(get("/api/v1/alerts/unresolved"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(5)
    void shouldReturn404WhenAlertNotFound() throws Exception {
        String id = "non-existent-alert";
        mockMvc.perform(get("/api/v1/alerts/{id}", id))
            .andExpect(status().isNotFound());
    }

    @Test
    @Order(6)
    void shouldDeleteAlert() throws Exception {
        String id = "test-alert-to-delete";
        mockMvc.perform(delete("/api/v1/alerts/{id}", id))
            .andExpect(status().isOk());
    }
}
