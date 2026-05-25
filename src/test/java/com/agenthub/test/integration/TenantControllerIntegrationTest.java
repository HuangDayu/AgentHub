package com.agenthub.test.integration;

import com.agenthub.infrastructure.store.db.mapper.TenantMybatisMapper;
import com.agenthub.test.TestAgentHubApplication;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static com.agenthub.test.common.TestCommonTools.extractJsonValue;
import static com.agenthub.test.common.TestCommonTools.getRequestBuilder;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(classes = TestAgentHubApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TenantControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private TenantMybatisMapper tenantMybatisMapper;

    private MockMvc mockMvc;
    private static String testTenantCode;

    @BeforeEach
    void setUpMockMvc() {
        // 生成唯一的租户编码
        testTenantCode = "acme-" + UUID.randomUUID().toString().substring(0, 8);
        
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .defaultRequest(getRequestBuilder())
                .build();
    }

    @Test
    @Order(1)
    void shouldSupportAllTenantWorkspaceAndMemberEndpoints() throws Exception {
        // Create tenant with unique code
        MvcResult tenantResult = mockMvc.perform(post("/api/v1/tenants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tenantCode\":\"" + testTenantCode + "\",\"name\":\"Acme\",\"planCode\":\"free\",\"region\":\"cn-east\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Acme"))
                .andReturn();

        String tenantId = extractJsonValue(tenantResult.getResponse().getContentAsString(), "id");

        // List tenants
        mockMvc.perform(get("/api/v1/tenants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));

        // Get tenant by ID
        mockMvc.perform(get("/api/v1/tenants/{tenantId}", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tenantId));

        // Patch tenant
        mockMvc.perform(patch("/api/v1/tenants/{tenantId}", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Acme Updated\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Acme Updated"));

        // Create workspace under tenant
        MvcResult workspaceResult = mockMvc.perform(post("/api/v1/tenants/{tenantId}/workspaces", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"workspaceCode\":\"main\",\"name\":\"Main\",\"region\":\"cn-east\"}"))
                .andExpect(status().isCreated())
                .andReturn();

        String workspaceId = extractJsonValue(workspaceResult.getResponse().getContentAsString(), "id");

        // List workspaces
        mockMvc.perform(get("/api/v1/tenants/{tenantId}/workspaces", tenantId))
                .andExpect(status().isOk());
    }

    @Test
    @Order(2)
    void shouldReturn404ForMissingTenant() throws Exception {
        mockMvc.perform(get("/api/v1/tenants/{tenantId}", "missing"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(3)
    void shouldReturn404ForMissingWorkspaceTenant() throws Exception {
        mockMvc.perform(post("/api/v1/tenants/{tenantId}/workspaces", "nonexistent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"workspaceCode\":\"main\",\"name\":\"Main\",\"region\":\"cn-east\"}"))
                .andExpect(status().isNotFound());
    }


}
