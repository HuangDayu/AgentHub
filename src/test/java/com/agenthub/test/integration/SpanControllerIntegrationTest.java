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
 * Span Controller 集成测试.
 */
@SpringBootTest(classes = TestAgentHubApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SpanControllerIntegrationTest {

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
    void shouldListSpans() throws Exception {
        mockMvc.perform(get("/api/v1/spans"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(2)
    void shouldListSpansByTrace() throws Exception {
        String traceId = "test-trace-001";
        mockMvc.perform(get("/api/v1/spans/traces/{traceId}", traceId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(3)
    void shouldListSpansByRun() throws Exception {
        String runId = "test-run-001";
        mockMvc.perform(get("/api/v1/spans/runs/{runId}", runId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(4)
    void shouldReturn404WhenSpanNotFound() throws Exception {
        String spanId = "non-existent-span";
        mockMvc.perform(get("/api/v1/spans/{spanId}", spanId))
            .andExpect(status().isNotFound());
    }

    @Test
    @Order(5)
    void shouldDeleteSpan() throws Exception {
        String spanId = "test-span-to-delete";
        mockMvc.perform(delete("/api/v1/spans/{spanId}", spanId))
            .andExpect(status().isOk());
    }
}
