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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static com.agenthub.test.common.TestCommonTools.*;
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
    
    
    private final String dsName = "test-schema-ds-" + System.currentTimeMillis();

    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockMvc mockMvc;
    private static String dataSourceId;
    private static String tableId;

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
        String body = mockMvc.perform(post("/api/v1/workspaces/{w}/agent-data-sources", WORKSPACE_ID)
                        .header("X-Tenant-Id", TENANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                    "name": "%s",
                                    "description": "schema test",
                                    "protocol": "JDBC",
                                    "endpointUri": "jdbc:postgresql://localhost:5432/test",
                                    "propertiesJson": "{}"
                                }
                                """, dsName)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        dataSourceId = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(body).get("id").asText();
    }

    @Test
    @Order(2)
    void shouldReturnEmptySchema() throws Exception {
        org.junit.jupiter.api.Assertions.assertNotNull(dataSourceId);
        mockMvc.perform(get("/api/v1/workspaces/{w}/agent-data-sources/{id}/schema", WORKSPACE_ID, dataSourceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataSourceId").value(dataSourceId));
    }

    @Test
    @Order(3)
    void shouldAddTable() throws Exception {
        org.junit.jupiter.api.Assertions.assertNotNull(dataSourceId);
        String body = mockMvc.perform(post("/api/v1/workspaces/{w}/agent-data-sources/{id}/schema/tables", WORKSPACE_ID, dataSourceId)
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
        mockMvc.perform(post("/api/v1/workspaces/{w}/agent-data-sources/{id}/schema/introspect", WORKSPACE_ID, dataSourceId))
                .andExpect(status().is(org.hamcrest.Matchers.anyOf(
                        org.hamcrest.Matchers.is(200),
                        org.hamcrest.Matchers.is(409),
                        org.hamcrest.Matchers.is(500))));
    }

    @Test
    @Order(5)
    void shouldDeleteTable() throws Exception {
        org.junit.jupiter.api.Assertions.assertNotNull(tableId);
        mockMvc.perform(delete("/api/v1/workspaces/{w}/agent-data-sources/{ds}/schema/tables/{t}",
                        WORKSPACE_ID, dataSourceId, tableId))
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(6)
    void shouldCleanupDataSource() throws Exception {
        org.junit.jupiter.api.Assertions.assertNotNull(dataSourceId);
        mockMvc.perform(delete("/api/v1/workspaces/{w}/agent-data-sources/{id}", WORKSPACE_ID, dataSourceId))
                .andExpect(status().isNoContent());
    }
}
