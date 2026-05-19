package com.agenthub.test.workflow;

import com.agenthub.test.TestAgentHubApplication;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static com.agenthub.test.common.TestCommonTools.getRequestBuilder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 工作流完整生命周期集成测试。
 * 测试工作流从创建到执行的完整流程。
 *
 * @author huangdayu
 */
@SpringBootTest(classes = TestAgentHubApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("工作流完整生命周期集成测试")
public class WorkflowFullLifecycleIntegrationTest {

    private static final String WORKSPACE_ID = "100000002";

    @Autowired
    private WebApplicationContext webApplicationContext;

    private static MockMvc mockMvc;
    private static ObjectMapper objectMapper;
    private static String workflowId;

    @BeforeEach
    void setUp() {
        if (mockMvc == null) {
            mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                    .defaultRequest(getRequestBuilder())
                    .build();
        }
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }
    }

    @Test
    @Order(1)
    @DisplayName("Step 1: 创建工作流")
    void step1_shouldCreateWorkflow() throws Exception {
        String response = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/workflows", WORKSPACE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "测试工作流",
                        "description": "集成测试工作流"
                    }
                    """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        workflowId = json.get("id").asText();
        Assertions.assertNotNull(workflowId);
    }

    @Test
    @Order(2)
    @DisplayName("Step 2: 查询工作流")
    void step2_shouldGetWorkflow() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/workflows/{workflowId}", WORKSPACE_ID, workflowId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(workflowId));
    }

    @Test
    @Order(3)
    @DisplayName("Step 3: 发布工作流")
    void step3_shouldPublishWorkflow() throws Exception {
        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/workflows/{workflowId}/publish", WORKSPACE_ID, workflowId))
                .andExpect(status().isOk());
    }

    @Test
    @Order(4)
    @DisplayName("Step 4: 取消发布工作流")
    void step4_shouldUnpublishWorkflow() throws Exception {
        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/workflows/{workflowId}/unpublish", WORKSPACE_ID, workflowId))
                .andExpect(status().isOk());
    }
}
