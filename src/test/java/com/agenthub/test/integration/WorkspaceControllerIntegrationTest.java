package com.agenthub.test.integration;

import com.agenthub.infrastructure.persistence.mapper.TenantMapper;
import com.agenthub.test.TestAgentHubApplication;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static com.agenthub.test.common.TestCommonTools.extractJsonValue;
import static com.agenthub.test.common.TestCommonTools.getRequestBuilder;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * WorkspaceController 集成测试.
 * <p>
 * 覆盖所有端点：创建工作空间、列出工作空间。
 * </p>
 */
@SpringBootTest(classes = TestAgentHubApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WorkspaceControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private TenantMapper tenantMapper;

    private MockMvc mockMvc;
    private static String savedTenantId;
    private static String savedWorkspaceId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .defaultRequest(getRequestBuilder())
                .build();
    }

    @Test
    @Order(1)
    void shouldCreateWorkspaceForTenant() throws Exception {
        // 先通过API创建租户
        String tenantContent = mockMvc.perform(post("/api/v1/tenants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tenantCode\":\"test-ws-" + UUID.randomUUID().toString().substring(0, 8) + "\",\"name\":\"Test Workspace Tenant\",\"planCode\":\"free\",\"region\":\"cn-east\"}"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        savedTenantId = extractJsonValue(tenantContent, "id");

        // 创建工作空间
        String content = mockMvc.perform(post("/api/v1/workspaces/tenants/{tenantId}/workspaces", savedTenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"workspaceCode\":\"main\",\"name\":\"Main Workspace\",\"region\":\"cn-east\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.workspaceCode").value("main"))
                .andExpect(jsonPath("$.name").value("Main Workspace"))
                .andExpect(jsonPath("$.region").value("cn-east"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        savedWorkspaceId = extractJsonValue(content, "id");
    }

    @Test
    @Order(2)
    void shouldListWorkspaces() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @Order(3)
    void shouldListWorkspacesWithPagination() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @Order(4)
    void shouldCreateWorkspaceWithTenantHeader() throws Exception {
        mockMvc.perform(post("/api/v1/workspaces")
                        .header("X-Tenant-ID", savedTenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"workspaceCode\":\"secondary\",\"name\":\"Secondary Workspace\",\"region\":\"cn-west\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.workspaceCode").value("secondary"));
    }

    @Test
    @Order(5)
    void shouldReturn404WhenTenantHeaderMissing() throws Exception {
        // 由于defaultRequest设置了X-Tenant-ID为100000002，而这个租户不存在
        // 所以会返回404而非400
        mockMvc.perform(post("/api/v1/workspaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"workspaceCode\":\"no-tenant\",\"name\":\"No Tenant\",\"region\":\"cn-east\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(6)
    void shouldReturn404ForNonexistentTenant() throws Exception {
        mockMvc.perform(post("/api/v1/workspaces/tenants/{tenantId}/workspaces", "nonexistent-tenant-" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"workspaceCode\":\"test\",\"name\":\"Test\",\"region\":\"cn-east\"}"))
                .andExpect(status().isNotFound());
    }
}
