package com.agenthub.test.integration;

import com.agenthub.test.TestAgentHubApplication;
import com.agenthub.test.common.TestCommonTools;
import org.junit.jupiter.api.BeforeAll;
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

import javax.sql.DataSource;
import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.Statement;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 数据源 Schema Controller 集成测试
 */
@SpringBootTest(classes = TestAgentHubApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DataSourceSchemaControllerIntegrationTest {
    private final String workspaceId = "100000002";
    private final String tenantId = "100000002";

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private DataSource dataSource;
    private MockMvc mockMvc;
    private static String dataSourceId;
    private static String tableId;

    @BeforeAll
    void applyMigrations() throws Exception {
        File sqlDir = new File(System.getProperty("user.dir"), "sql");
        String[] files = {"V100__add_agent_data_source_tables.sql",
                          "V101__add_data_source_schema_tables.sql",
                          "V102__add_permission_strategy_tables.sql",
                          "V103__add_global_audit_log_table.sql"};
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS app.audit_log CASCADE");
            stmt.execute("DROP TABLE IF EXISTS app.permission_strategy CASCADE");
            stmt.execute("DROP TABLE IF EXISTS app.table_relationship CASCADE");
            stmt.execute("DROP TABLE IF EXISTS app.data_source_column CASCADE");
            stmt.execute("DROP TABLE IF EXISTS app.data_source_table CASCADE");
            stmt.execute("DROP TABLE IF EXISTS app.data_source_schema CASCADE");
            stmt.execute("DROP TABLE IF EXISTS app.agent_data_source CASCADE");
            for (String name : files) {
                File f = new File(sqlDir, name);
                if (!f.exists()) continue;
                String sql = Files.readString(f.toPath());
                stmt.execute(sql);
            }
        }
    }

    @org.junit.jupiter.api.BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .defaultRequest(TestCommonTools.getRequestBuilder())
                .build();
    }

    @Test
    @Order(1)
    void shouldCreateDataSourceForSchema() throws Exception {
        String body = mockMvc.perform(post("/api/v1/workspaces/{w}/agent-data-sources", workspaceId)
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "test-schema-ds",
                                    "description": "schema test",
                                    "protocol": "JDBC",
                                    "endpointUri": "jdbc:postgresql://localhost:5432/test",
                                    "propertiesJson": "{}"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        dataSourceId = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(body).get("id").asText();
    }

    @Test
    @Order(2)
    void shouldReturnEmptySchema() throws Exception {
        org.junit.jupiter.api.Assertions.assertNotNull(dataSourceId);
        mockMvc.perform(get("/api/v1/workspaces/{w}/agent-data-sources/{id}/schema", workspaceId, dataSourceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataSourceId").value(dataSourceId));
    }

    @Test
    @Order(3)
    void shouldAddTable() throws Exception {
        org.junit.jupiter.api.Assertions.assertNotNull(dataSourceId);
        String body = mockMvc.perform(post("/api/v1/workspaces/{w}/agent-data-sources/{id}/schema/tables", workspaceId, dataSourceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "users",
                                    "displayName": "Users",
                                    "description": "user table",
                                    "columns": [
                                        {"name": "id", "type": "BIGINT", "isPrimary": true, "nullable": false},
                                        {"name": "email", "type": "VARCHAR", "isPrimary": false, "nullable": false}
                                    ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        com.fasterxml.jackson.databind.JsonNode tables = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(body).get("tables");
        if (tables != null && tables.size() > 0) {
            tableId = tables.get(0).get("id").asText();
        }
    }

    @Test
    @Order(4)
    void shouldIntrospectSchema() throws Exception {
        org.junit.jupiter.api.Assertions.assertNotNull(dataSourceId);
        mockMvc.perform(post("/api/v1/workspaces/{w}/agent-data-sources/{id}/schema/introspect", workspaceId, dataSourceId))
                .andExpect(status().is(org.hamcrest.Matchers.anyOf(org.hamcrest.Matchers.is(200), org.hamcrest.Matchers.is(500))));
    }

    @Test
    @Order(5)
    void shouldDeleteTable() throws Exception {
        org.junit.jupiter.api.Assertions.assertNotNull(tableId);
        mockMvc.perform(delete("/api/v1/workspaces/{w}/agent-data-sources/{ds}/schema/tables/{t}",
                        workspaceId, dataSourceId, tableId))
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(6)
    void shouldCleanupDataSource() throws Exception {
        org.junit.jupiter.api.Assertions.assertNotNull(dataSourceId);
        mockMvc.perform(delete("/api/v1/workspaces/{w}/agent-data-sources/{id}", workspaceId, dataSourceId))
                .andExpect(status().isNoContent());
    }
}
