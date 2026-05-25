package com.agenthub.test.integration;

import com.agenthub.test.TestAgentHubApplication;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static com.agenthub.test.common.TestCommonTools.getRequestBuilder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Trace Controller 集成测试.
 */
@SpringBootTest(classes = TestAgentHubApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TraceControllerIntegrationTest {

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
    void shouldListTraces() throws Exception {
        mockMvc.perform(get("/api/v1/traces"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(2)
    void shouldListTracesByRun() throws Exception {
        String runId = "test-run-001";
        mockMvc.perform(get("/api/v1/traces/runs/{runId}", runId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(3)
    void shouldReturn404WhenTraceNotFound() throws Exception {
        String traceId = "non-existent-trace";
        mockMvc.perform(get("/api/v1/traces/{traceId}", traceId))
            .andExpect(status().isNotFound());
    }

    @Test
    @Order(4)
    void shouldDeleteTrace() throws Exception {
        String traceId = "test-trace-to-delete";
        mockMvc.perform(delete("/api/v1/traces/{traceId}", traceId))
            .andExpect(status().isOk());
    }
}
