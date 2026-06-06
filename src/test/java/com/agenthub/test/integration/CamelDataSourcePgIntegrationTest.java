package com.agenthub.test.integration;

import com.agenthub.test.TestAgentHubApplication;
import com.agenthub.test.common.TestCommonTools;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
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

import javax.sql.DataSource;
import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.Statement;

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
    @Autowired private DataSource dataSource;

    @Value("${spring.datasource.url}") private String jdbcUrl;
    @Value("${spring.datasource.username}") private String username;
    @Value("${spring.datasource.password}") private String password;

    private MockMvc mockMvc;
    private String createdId;

    @BeforeAll
    void applyMigrations() throws Exception {
        File sqlDir = new File(System.getProperty("user.dir"), "sql");
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS app.audit_log CASCADE");
            stmt.execute("DROP TABLE IF EXISTS app.permission_strategy CASCADE");
            stmt.execute("DROP TABLE IF EXISTS app.table_relationship CASCADE");
            stmt.execute("DROP TABLE IF EXISTS app.data_source_column CASCADE");
            stmt.execute("DROP TABLE IF EXISTS app.data_source_table CASCADE");
            stmt.execute("DROP TABLE IF EXISTS app.data_source_schema CASCADE");
            stmt.execute("DROP TABLE IF EXISTS app.agent_data_source CASCADE");
            String[] files = {"V100__add_agent_data_source_tables.sql",
                              "V101__add_data_source_schema_tables.sql",
                              "V102__add_permission_strategy_tables.sql",
                              "V103__add_global_audit_log_table.sql"};
            for (String name : files) {
                File f = new File(sqlDir, name);
                if (f.exists()) {
                    stmt.execute(Files.readString(f.toPath()));
                }
            }
        }
    }

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
                                    "name": "app-pg-ds",
                                    "description": "uses app primary PG",
                                    "protocol": "JDBC",
                                    "endpointUri": "%s",
                                    "propertiesJson": "{\\"user\\":\\"%s\\",\\"password\\":\\"%s\\"}",
                                    "permissionPolicyId": null,
                                    "schemaId": null
                                }
                                """, jdbcUrl, username, password)))
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
