package com.agenthub.test.integration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
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

@SpringBootTest(classes = TestAgentHubApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ScheduledTaskControllerIntegrationTest {
    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private String createdTaskId;
    private final String workspaceId = "100000002";
    private final ObjectMapper objectMapper = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .defaultRequest(getRequestBuilder())
                .build();
    }

    @Test
    @Order(1)
    void shouldCreateScheduledTask() throws Exception {
        String responseBody = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/scheduled-tasks", workspaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tenantId": "100000001",
                                    "workspaceId": "100000002",
                                    "taskCode": "TEST_TASK_001",
                                    "name": "Test Task",
                                    "description": "Test task description",
                                    "taskType": "AGENT_CALL",
                                    "cronExpression": "0 0 2 * * ?",
                                    "executorConfig": "{}",
                                    "prompt": "请帮我分析数据并生成报告"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.taskCode").value("TEST_TASK_001"))
                .andExpect(jsonPath("$.prompt").value("请帮我分析数据并生成报告"))
                .andReturn().getResponse().getContentAsString();

        createdTaskId = objectMapper.readTree(responseBody).get("id").asText();
    }

    @Test
    @Order(2)
    void shouldGetScheduledTaskById() throws Exception {
        Assertions.assertNotNull(createdTaskId, "Task should be created first");
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/scheduled-tasks/{taskId}", workspaceId, createdTaskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdTaskId))
                .andExpect(jsonPath("$.prompt").value("请帮我分析数据并生成报告"));
    }

    @Test
    @Order(3)
    void shouldListScheduledTasks() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/scheduled-tasks", workspaceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(4)
    void shouldUpdateScheduledTask() throws Exception {
        Assertions.assertNotNull(createdTaskId, "Task should be created first");
        mockMvc.perform(put("/api/v1/workspaces/{workspaceId}/scheduled-tasks/{taskId}", workspaceId, createdTaskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Updated Task",
                                    "description": "Updated description",
                                    "cronExpression": "0 0 3 * * ?",
                                    "executorConfig": "{}",
                                    "prompt": "更新后的提示词内容"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Task"))
                .andExpect(jsonPath("$.prompt").value("更新后的提示词内容"));
    }

    @Test
    @Order(5)
    void shouldEnableScheduledTask() throws Exception {
        Assertions.assertNotNull(createdTaskId, "Task should be created first");
        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/scheduled-tasks/{taskId}/enable", workspaceId, createdTaskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    @Order(6)
    void shouldDisableScheduledTask() throws Exception {
        Assertions.assertNotNull(createdTaskId, "Task should be created first");
        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/scheduled-tasks/{taskId}/disable", workspaceId, createdTaskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));
    }

    @Test
    @Order(7)
    void shouldExecuteScheduledTask() throws Exception {
        Assertions.assertNotNull(createdTaskId, "Task should be created first");
        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/scheduled-tasks/{taskId}/execute", workspaceId, createdTaskId))
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(8)
    void shouldDeleteScheduledTask() throws Exception {
        Assertions.assertNotNull(createdTaskId, "Task should be created first");
        mockMvc.perform(delete("/api/v1/workspaces/{workspaceId}/scheduled-tasks/{taskId}", workspaceId, createdTaskId))
                .andExpect(status().isNoContent());
    }
}
