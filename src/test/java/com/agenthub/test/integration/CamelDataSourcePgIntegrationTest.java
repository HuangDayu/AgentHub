package com.agenthub.test.integration;

import com.agenthub.test.TestAgentHubApplication;
import com.agenthub.test.common.TestCommonTools;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 端到端 Camel 数据源连接测试 - 使用 application.yml 的 PostgreSQL 数据源验证 /test 端点
 */
@SpringBootTest(classes = TestAgentHubApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CamelDataSourcePgIntegrationTest {

    private final String workspaceId = "100000002";
    private final String tenantId = "100000002";
    private final ObjectMapper objectMapper = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    @Autowired private WebApplicationContext wac;

    @Value("${spring.datasource.url}") private String jdbcUrl;
    @Value("${spring.datasource.username}") private String username;
    @Value("${spring.datasource.password}") private String password;

    private MockMvc mockMvc;
    private String createdId;
    private final String uniqueName = "app-pg-ds-" + UUID.randomUUID().toString().substring(0, 8);

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .defaultRequest(TestCommonTools.getRequestBuilder()).build();
    }

    @Test
    @Order(1)
    void shouldCreatePgDataSource() throws Exception {
        String body = mockMvc.perform(post("/api/v1/workspaces/{w}/agent-data-sources", workspaceId)
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                    "name": "%s",
                                    "description": "uses app primary PG",
                                    "protocol": "JDBC",
                                    "endpointUri": "%s",
                                    "propertiesJson": "{\\"user\\":\\"%s\\",\\"password\\":\\"%s\\"}",
                                    "permissionPolicyId": null,
                                    "schemaId": null
                                }
                                """, uniqueName, jdbcUrl, username, password)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        createdId = objectMapper.readTree(body).get("id").asText();
    }

    @Test
    @Order(2)
    void shouldTestConnectionToRealPg() throws Exception {
        org.junit.jupiter.api.Assertions.assertNotNull(createdId);
        MvcResult result = mockMvc.perform(post(
                        "/api/v1/workspaces/{w}/agent-data-sources/{id}/test",
                        workspaceId, createdId))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        org.junit.jupiter.api.Assertions.assertTrue(json.get("success").asBoolean(),
                "expected success=true, got body=" + result.getResponse().getContentAsString());
    }
}
