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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * OtlpController集成测试
 */
@SpringBootTest(classes = TestAgentHubApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OtlpControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .defaultRequest(getRequestBuilder())
                .build();
    }

    @Test
    @Order(1)
    void shouldReceiveTraces() throws Exception {
        mockMvc.perform(post("/api/v1/otlp/traces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "spanId": "test-span-001",
                                    "traceId": "test-trace-001",
                                    "operationName": "test-operation",
                                    "serviceName": "test-service",
                                    "kind": "INTERNAL",
                                    "startTimestamp": 1234567890000000,
                                    "endTimestamp": 1234567891000000,
                                    "status": "OK"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    @Order(2)
    void shouldReceiveMetrics() throws Exception {
        mockMvc.perform(post("/api/v1/otlp/metrics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "metricName": "test-metric",
                                    "metricType": "COUNTER",
                                    "serviceName": "test-service",
                                    "value": "100",
                                    "timestamp": 1234567890000000
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    @Order(3)
    void shouldReceiveLogs() throws Exception {
        mockMvc.perform(post("/api/v1/otlp/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "logId": "test-log-001",
                                    "serviceName": "test-service",
                                    "severity": "INFO",
                                    "body": "Test log message",
                                    "timestamp": 1234567890000000
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    @Order(4)
    void shouldReceiveComplexTraceData() throws Exception {
        mockMvc.perform(post("/api/v1/otlp/traces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "resourceSpans": [
                                        {
                                            "scopeSpans": [
                                                {
                                                    "spans": [
                                                        {
                                                            "spanId": "span-001",
                                                            "traceId": "trace-001",
                                                            "operationName": "operation-1",
                                                            "serviceName": "service-1"
                                                        }
                                                    ]
                                                }
                                            ]
                                        }
                                    ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    @Order(5)
    void shouldReceiveMultipleSpans() throws Exception {
        mockMvc.perform(post("/api/v1/otlp/traces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "spans": [
                                        {
                                            "spanId": "span-002",
                                            "traceId": "trace-002",
                                            "operationName": "operation-2",
                                            "serviceName": "service-2"
                                        },
                                        {
                                            "spanId": "span-003",
                                            "traceId": "trace-002",
                                            "operationName": "operation-3",
                                            "serviceName": "service-2"
                                        }
                                    ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(2));
    }

    @Test
    @Order(6)
    void shouldQuerySpans() throws Exception {
        mockMvc.perform(get("/api/v1/otlp/spans")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(7)
    void shouldQueryTraceById() throws Exception {
        mockMvc.perform(get("/api/v1/otlp/traces/{traceId}", "test-trace-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(8)
    void shouldQueryMetrics() throws Exception {
        mockMvc.perform(get("/api/v1/otlp/metrics/query")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(9)
    void shouldQueryLogs() throws Exception {
        mockMvc.perform(get("/api/v1/otlp/logs/query")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(10)
    void shouldGetStatistics() throws Exception {
        mockMvc.perform(get("/api/v1/otlp/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSpans").exists())
                .andExpect(jsonPath("$.totalMetrics").exists())
                .andExpect(jsonPath("$.totalLogs").exists());
    }
}
