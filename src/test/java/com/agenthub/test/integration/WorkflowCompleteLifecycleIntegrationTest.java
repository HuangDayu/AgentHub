package com.agenthub.test.integration;

import com.agenthub.test.TestAgentHubApplication;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * 覆盖所有节点类型、复杂逻辑、完整流程。
 */
@SpringBootTest(classes = TestAgentHubApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("工作流完整生命周期集成测试")
public class WorkflowCompleteLifecycleIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(WorkflowCompleteLifecycleIntegrationTest.class);
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
    @DisplayName("Step 1: 创建复杂工作流")
    void step1_createComplexWorkflow() throws Exception {
        String workflowJson = """
            {
                "name": "复杂AI工作流",
                "description": "包含LLM对话、条件判断、循环、并行执行的完整工作流",
                "workflowCode": "complex-ai-workflow",
                "workspaceId": "100000002",
                "tenantId": "100000002",
                "graphDefinition": "{}"
            }
            """;

        String response = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/workflows", WORKSPACE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(workflowJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        workflowId = json.get("id").asText();
        Assertions.assertNotNull(workflowId);
        log.info("✅ 创建工作流成功: {}", workflowId);
    }

    @Test
    @Order(2)
    @DisplayName("Step 2: 添加START节点")
    void step2_addStartNode() throws Exception {
        String nodeJson = """
            {
                "type": "START",
                "name": "工作流开始",
                "position": {"x": 100, "y": 200}
            }
            """;

        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/workflows/{workflowId}/nodes", 
                WORKSPACE_ID, workflowId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(nodeJson))
                .andExpect(status().isCreated());
        
        log.info("✅ 添加START节点成功");
    }

    @Test
    @Order(3)
    @DisplayName("Step 3: 添加LLM对话节点")
    void step3_addLlmNode() throws Exception {
        String nodeJson = """
            {
                "type": "LLM",
                "name": "AI对话分析",
                "position": {"x": 300, "y": 200},
                "config": {
                    "agentId": "default",
                    "prompt": "请分析以下内容：${input}",
                    "streaming": false
                }
            }
            """;

        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/workflows/{workflowId}/nodes", 
                WORKSPACE_ID, workflowId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(nodeJson))
                .andExpect(status().isCreated());
        
        log.info("✅ 添加LLM节点成功");
    }

    @Test
    @Order(4)
    @DisplayName("Step 4: 添加条件判断节点")
    void step4_addConditionNode() throws Exception {
        String nodeJson = """
            {
                "type": "CONDITION",
                "name": "结果判断",
                "position": {"x": 500, "y": 200},
                "config": {
                    "expression": "${score > 0.8}",
                    "trueBranch": "high-score-branch",
                    "falseBranch": "low-score-branch"
                }
            }
            """;

        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/workflows/{workflowId}/nodes", 
                WORKSPACE_ID, workflowId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(nodeJson))
                .andExpect(status().isCreated());
        
        log.info("✅ 添加CONDITION节点成功");
    }

    @Test
    @Order(5)
    @DisplayName("Step 5: 添加变量赋值节点")
    void step5_addVariableNode() throws Exception {
        String nodeJson = """
            {
                "type": "VARIABLE",
                "name": "设置上下文变量",
                "position": {"x": 700, "y": 200},
                "config": {
                    "variables": {
                        "processed": true,
                        "timestamp": "${sys.now}",
                        "workflowName": "复杂AI工作流"
                    }
                }
            }
            """;

        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/workflows/{workflowId}/nodes", 
                WORKSPACE_ID, workflowId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(nodeJson))
                .andExpect(status().isCreated());
        
        log.info("✅ 添加VARIABLE节点成功");
    }

    @Test
    @Order(6)
    @DisplayName("Step 6: 添加API调用节点")
    void step6_addApiNode() throws Exception {
        String nodeJson = """
            {
                "type": "API",
                "name": "调用外部API",
                "position": {"x": 900, "y": 200},
                "config": {
                    "url": "https://api.example.com/data",
                    "method": "POST",
                    "headers": {
                        "Content-Type": "application/json"
                    },
                    "body": {
                        "query": "${input}"
                    }
                }
            }
            """;

        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/workflows/{workflowId}/nodes", 
                WORKSPACE_ID, workflowId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(nodeJson))
                .andExpect(status().isCreated());
        
        log.info("✅ 添加API节点成功");
    }

    @Test
    @Order(7)
    @DisplayName("Step 7: 添加工具调用节点")
    void step7_addToolNode() throws Exception {
        String nodeJson = """
            {
                "type": "TOOL",
                "name": "执行工具",
                "position": {"x": 1100, "y": 200},
                "config": {
                    "toolName": "data-processor",
                    "parameters": {
                        "input": "${previousResult}",
                        "mode": "advanced"
                    }
                }
            }
            """;

        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/workflows/{workflowId}/nodes", 
                WORKSPACE_ID, workflowId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(nodeJson))
                .andExpect(status().isCreated());
        
        log.info("✅ 添加TOOL节点成功");
    }

    @Test
    @Order(8)
    @DisplayName("Step 8: 添加代码执行节点")
    void step8_addCodeNode() throws Exception {
        String nodeJson = """
            {
                "type": "CODE",
                "name": "执行脚本",
                "position": {"x": 1300, "y": 200},
                "config": {
                    "script": "result = data.map(x => x * 2);",
                    "language": "javascript"
                }
            }
            """;

        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/workflows/{workflowId}/nodes", 
                WORKSPACE_ID, workflowId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(nodeJson))
                .andExpect(status().isCreated());
        
        log.info("✅ 添加CODE节点成功");
    }

    @Test
    @Order(9)
    @DisplayName("Step 9: 添加循环节点")
    void step9_addLoopNode() throws Exception {
        String nodeJson = """
            {
                "type": "LOOP",
                "name": "批量处理",
                "position": {"x": 1500, "y": 200},
                "config": {
                    "items": "${dataList}",
                    "maxIterations": 100,
                    "body": {
                        "type": "LLM",
                        "name": "处理单项",
                        "config": {
                            "prompt": "处理：${loop.item}"
                        }
                    }
                }
            }
            """;

        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/workflows/{workflowId}/nodes", 
                WORKSPACE_ID, workflowId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(nodeJson))
                .andExpect(status().isCreated());
        
        log.info("✅ 添加LOOP节点成功");
    }

    @Test
    @Order(10)
    @DisplayName("Step 10: 添加并行节点")
    void step10_addParallelNode() throws Exception {
        String nodeJson = """
            {
                "type": "PARALLEL",
                "name": "并行执行",
                "position": {"x": 1700, "y": 200},
                "config": {
                    "concurrency": 4,
                    "nodes": [
                        {
                            "type": "LLM",
                            "name": "并行任务1",
                            "config": {"prompt": "任务1"}
                        },
                        {
                            "type": "LLM",
                            "name": "并行任务2",
                            "config": {"prompt": "任务2"}
                        }
                    ]
                }
            }
            """;

        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/workflows/{workflowId}/nodes", 
                WORKSPACE_ID, workflowId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(nodeJson))
                .andExpect(status().isCreated());
        
        log.info("✅ 添加PARALLEL节点成功");
    }

    @Test
    @Order(11)
    @DisplayName("Step 11: 添加END节点")
    void step11_addEndNode() throws Exception {
        String nodeJson = """
            {
                "type": "END",
                "name": "工作流结束",
                "position": {"x": 1900, "y": 200}
            }
            """;

        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/workflows/{workflowId}/nodes", 
                WORKSPACE_ID, workflowId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(nodeJson))
                .andExpect(status().isCreated());
        
        log.info("✅ 添加END节点成功");
    }

    @Test
    @Order(12)
    @DisplayName("Step 12: 查询所有节点")
    void step12_listAllNodes() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/workflows/{workflowId}/nodes", 
                WORKSPACE_ID, workflowId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        
        log.info("✅ 查询所有节点成功");
    }

    @Test
    @Order(13)
    @DisplayName("Step 13: 发布工作流")
    void step13_publishWorkflow() throws Exception {
        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/workflows/{workflowId}/publish", 
                WORKSPACE_ID, workflowId))
                .andExpect(status().isOk());
        
        log.info("✅ 发布工作流成功");
    }

    @Test
    @Order(14)
    @DisplayName("Step 14: 取消发布工作流")
    void step14_unpublishWorkflow() throws Exception {
        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/workflows/{workflowId}/unpublish", 
                WORKSPACE_ID, workflowId))
                .andExpect(status().isOk());
        
        log.info("✅ 取消发布工作流成功");
    }

    @Test
    @Order(15)
    @DisplayName("Step 15: 删除工作流")
    void step15_deleteWorkflow() throws Exception {
        mockMvc.perform(delete("/api/v1/workspaces/{workspaceId}/workflows/{workflowId}", 
                WORKSPACE_ID, workflowId))
                .andExpect(status().isNoContent());
        
        log.info("✅ 删除工作流成功");
    }
}
