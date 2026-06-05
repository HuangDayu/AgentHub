package com.agenthub.test.integration;

import cn.hutool.core.io.FileUtil;
import com.agenthub.common.utils.RandomUtils;
import com.agenthub.test.TestAgentHubApplication;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.sql.DataSource;
import java.util.UUID;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.agenthub.common.utils.RandomUtils.randomShortId;
import static com.agenthub.test.common.TestCommonTools.getRequestBuilder;
import static org.springframework.ai.util.json.JsonParser.toJson;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 工作流完整生命周期集成测试。
 * 测试工作流从创建到执行的完整流程，
 * 使用覆盖所有节点类型的复杂graphDefinition JSON。
 *
 * @author huangdayu
 */
@SpringBootTest(classes = TestAgentHubApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("工作流完整生命周期集成测试")
public class WorkflowFullLifecycleIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(WorkflowFullLifecycleIntegrationTest.class);
    private static final String WORKSPACE_ID = "100000002";
    private static final String TENANT_ID = "100000002";
    public static final String key = FileUtil.readUtf8String(System.getProperty("user.dir") + "/keys/key.txt");

    // 真实配置ID（动态创建）
    private static String vectorStoreConfigId;
    private static String chatModelConfigId;
    private static String embeddingModelConfigId;
    private static String knowledgeBaseId;
    private static String retrievalStrategyId;
    private static String modelStrategyId;
    private static String agentId;
    private static String mcpToolId;

    /*
     * ================================================================
     * 复杂工作流 graphDefinition JSON
     * 覆盖所有12种节点类型:
     *   START, VARIABLE, LLM, CONDITION, LOOP, TOOL,
     *   PARALLEL, API, CODE, RETRIEVAL, SUB_WORKFLOW, END
     *
     * DAG拓扑（线性链 + 收敛）:
     *   start → variable → llm → condition → loop → tool
     *     → parallel → api → code → retrieval → sub-workflow → end
     *
     * 特殊节点内部:
     *   - Loop: body包含内嵌LLM节点
     *   - Parallel: body嵌套[API, CODE]并行执行
     *   - Condition: 配置3个条件分支
     * ================================================================
     */
    private static final String COMPLEX_GRAPH_DEFINITION = """
        {
            "nodes": [
                {
                    "id": "start-001",
                    "type": "start",
                    "position": {"x": 50, "y": 320},
                    "data": {
                        "label": "工作流开始",
                        "input_params": [],
                        "output_params": [
                            {"key": "output", "type": "Object", "desc": "开始节点输出"}
                        ],
                        "node_param": {}
                    }
                },
                {
                    "id": "variable-001",
                    "type": "variable",
                    "position": {"x": 280, "y": 320},
                    "data": {
                        "label": "初始化变量",
                        "input_params": [],
                        "output_params": [
                            {"key": "name", "type": "String", "name": "变量名"},
                            {"key": "value", "type": "Object", "name": "变量值"}
                        ],
                        "node_param": {
                            "assignments": [
                                {"name": "userInput", "value": "测试数据分析请求"},
                                {"name": "threshold", "value": 0.8},
                                {"name": "dataList", "value": [1, 2, 3, 4, 5]}
                            ]
                        }
                    }
                },
                {
                    "id": "llm-001",
                    "type": "llm",
                    "position": {"x": 510, "y": 320},
                    "data": {
                        "label": "AI对话分析",
                        "input_params": [
                            {"key": "prompt", "type": "String", "name": "提示词", "required": true, "description": "发送给LLM的提示词模板"}
                        ],
                        "output_params": [
                            {"key": "content", "type": "String", "name": "LLM回复", "description": "LLM返回的文本内容"},
                            {"key": "success", "type": "Boolean", "name": "是否成功", "description": "调用是否成功"}
                        ],
                        "node_param": {
                            "agentId": "${AGENT_ID}",
                            "prompt": "请分析以下内容：${userInput}，并给出评分",
                            "streaming": false
                        }
                    }
                },
                {
                    "id": "condition-001",
                    "type": "condition",
                    "position": {"x": 740, "y": 320},
                    "data": {
                        "label": "评分条件判断",
                        "input_params": [],
                        "output_params": [
                            {"key": "selectedBranch", "type": "String", "name": "选中分支", "description": "匹配的条件分支名称"},
                            {"key": "branchCount", "type": "Number", "name": "分支数量"},
                            {"key": "evaluated", "type": "Boolean", "name": "已评估"}
                        ],
                        "node_param": {
                            "branches": [
                                {"name": "高分", "expression": "threshold > 0.5", "targetNodeId": "loop-001"},
                                {"name": "中分", "expression": "threshold == 0.5", "targetNodeId": "api-001"},
                                {"name": "低分", "expression": "threshold <= 0.3", "targetNodeId": "code-001"}
                            ]
                        }
                    }
                },
                {
                    "id": "loop-001",
                    "type": "loop",
                    "position": {"x": 970, "y": 160},
                    "data": {
                        "label": "批量循环处理",
                        "input_params": [],
                        "output_params": [
                            {"key": "results", "type": "Array<Object>", "name": "循环结果", "description": "每次迭代的执行结果"},
                            {"key": "iterations", "type": "Number", "name": "迭代次数"}
                        ],
                        "node_param": {
                            "items": "${dataList}",
                            "maxIterations": 100
                        }
                    }
                },
                {
                    "id": "tool-001",
                    "type": "tool",
                    "position": {"x": 1200, "y": 160},
                    "data": {
                        "label": "数据工具处理",
                        "input_params": [],
                        "output_params": [
                            {"key": "result", "type": "Object", "name": "工具输出", "description": "工具执行返回的结果"},
                            {"key": "toolName", "type": "String", "name": "工具名称"},
                            {"key": "success", "type": "Boolean", "name": "是否成功"}
                        ],
                        "node_param": {
                            "toolName": "${MCP_TOOL_ID}",
                            "parameters": {
                                "mode": "batch",
                                "input": "${userInput}"
                            }
                        }
                    }
                },
                {
                    "id": "parallel-001",
                    "type": "parallel",
                    "position": {"x": 1430, "y": 100},
                    "data": {
                        "label": "并行执行分支",
                        "input_params": [],
                        "output_params": [
                            {"key": "results", "type": "Array<Object>", "name": "执行结果", "description": "各分支的执行结果列表"},
                            {"key": "totalNodes", "type": "Number", "name": "节点总数"}
                        ],
                        "node_param": {
                            "concurrency": 2
                        }
                    }
                },
                {
                    "id": "api-001",
                    "type": "api",
                    "position": {"x": 970, "y": 480},
                    "data": {
                        "label": "外部API调用",
                        "input_params": [
                            {"key": "url", "type": "String", "name": "请求URL", "required": true, "description": "HTTP请求URL，支持变量引用"}
                        ],
                        "output_params": [
                            {"key": "success", "type": "Boolean", "name": "是否成功", "description": "API调用是否成功"},
                            {"key": "error", "type": "String", "name": "错误信息", "description": "调用失败时的错误信息"}
                        ],
                        "node_param": {
                            "url": "https://api.example.com/process",
                            "method": "POST",
                            "body": {"data": "${userInput}"},
                            "timeoutMs": 30000
                        }
                    }
                },
                {
                    "id": "code-001",
                    "type": "code",
                    "position": {"x": 970, "y": 640},
                    "data": {
                        "label": "JavaScript代码执行",
                        "input_params": [
                            {"key": "data", "type": "Object", "name": "输入数据", "description": "传递给脚本的上下文变量"}
                        ],
                        "output_params": [
                            {"key": "result", "type": "Object", "name": "执行结果", "description": "代码执行返回的结果"},
                            {"key": "success", "type": "Boolean", "name": "是否成功"}
                        ],
                        "node_param": {
                            "script": "var result = variables.dataList.map(function(x) { return x * 2; }); result;"
                        }
                    }
                },
                {
                    "id": "retrieval-001",
                    "type": "retrieval",
                    "position": {"x": 1200, "y": 560},
                    "data": {
                        "label": "知识库检索",
                        "input_params": [
                            {"key": "query", "type": "String", "name": "检索查询", "required": true, "description": "检索的查询文本，支持变量引用"}
                        ],
                        "output_params": [
                            {"key": "documentCount", "type": "Number", "name": "检索文档数", "description": "检索到的文档数量"},
                            {"key": "documents", "type": "Array<Object>", "name": "文档列表", "description": "检索到的文档列表"},
                            {"key": "content", "type": "String", "name": "拼接内容", "description": "拼接模式下的文档内容"},
                            {"key": "success", "type": "Boolean", "name": "是否成功"}
                        ],
                        "node_param": {
                            "knowledgeBaseId": "${KNOWLEDGE_BASE_ID}",
                            "query": "${userInput}",
                            "topK": 10,
                            "scoreThreshold": 0.6,
                            "retrievalType": "hybrid",
                            "processMode": "list",
                            "includeMetadata": true,
                            "includeScores": true,
                            "separator": "\\n\\n",
                            "outputVariable": "retrievedDocs"
                        }
                    }
                },
                {
                    "id": "sub-workflow-001",
                    "type": "sub-workflow",
                    "position": {"x": 1660, "y": 320},
                    "data": {
                        "label": "子工作流调用",
                        "input_params": [],
                        "output_params": [
                            {"key": "success", "type": "Boolean", "name": "是否成功"},
                            {"key": "executionId", "type": "String", "name": "执行ID"},
                            {"key": "output", "type": "Object", "name": "子工作流输出"}
                        ],
                        "node_param": {
                            "subWorkflowId": "sub-wf-001",
                            "inputMapping": "{\\"input\\":\\"${userInput}\\"}",
                            "outputMapping": "{\\"result\\":\\"${output}\\"}",
                            "timeout": 300
                        }
                    }
                },
                {
                    "id": "end-001",
                    "type": "end",
                    "position": {"x": 1890, "y": 320},
                    "data": {
                        "label": "工作流结束",
                        "input_params": [
                            {"key": "input", "type": "Object", "value_from": "refer"}
                        ],
                        "output_params": [],
                        "node_param": {}
                    }
                }
            ],
            "edges": [
                {"id": "e-start-var", "source": "start-001", "target": "variable-001"},
                {"id": "e-var-llm", "source": "variable-001", "target": "llm-001"},
                {"id": "e-llm-cond", "source": "llm-001", "target": "condition-001"},
                {"id": "e-cond-loop", "source": "condition-001", "target": "loop-001", "label": "高分"},
                {"id": "e-loop-tool", "source": "loop-001", "target": "tool-001"},
                {"id": "e-tool-parallel", "source": "tool-001", "target": "parallel-001"},
                {"id": "e-cond-api", "source": "condition-001", "target": "api-001", "label": "中分"},
                {"id": "e-cond-code", "source": "condition-001", "target": "code-001", "label": "低分"},
                {"id": "e-code-retrieval", "source": "code-001", "target": "retrieval-001"},
                {"id": "e-api-parallel", "source": "api-001", "target": "parallel-001"},
                {"id": "e-retrieval-parallel", "source": "retrieval-001", "target": "parallel-001"},
                {"id": "e-parallel-sub", "source": "parallel-001", "target": "sub-workflow-001"},
                {"id": "e-sub-end", "source": "sub-workflow-001", "target": "end-001"}
            ]
        }
        """;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private static MockMvc mockMvc;
    private static ObjectMapper objectMapper;
    private static String workflowId;
    private static String executionId;

    @Autowired
    private DataSource dataSource;

    @BeforeAll
    void initDatabase() {
        try (var conn = dataSource.getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute("ALTER TABLE app.dag_workflow ALTER COLUMN graph_definition TYPE TEXT");
            log.info("\u2705 已确保 workflow.graph_definition 列类型为 TEXT");
        } catch (Exception e) {
            log.warn("\u26a0\ufe0f 修改 graph_definition 列类型失败: {}", e.getMessage());
        }
    }

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

    // ==================== 前置步骤：创建真实配置 ====================

    @Test
    @Order(1)
    @DisplayName("前置Step 1: 创建向量存储配置")
    void preStep1_shouldCreateVectorStoreConfig() throws Exception {
        String response = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/vector-stores", WORKSPACE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                    "name": "test-qdrant-%s",
                                    "type": "QDRANT",
                                    "host": "localhost",
                                    "port": 6334,
                                    "collectionName": "test-workflow-collection"
                                }
                                """, randomShortId())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        vectorStoreConfigId = extractId(response);
        Assertions.assertNotNull(vectorStoreConfigId);
        log.info("✅ 创建向量存储配置: {}", vectorStoreConfigId);
    }

    @Test
    @Order(2)
    @DisplayName("前置Step 2: 创建聊天模型配置")
    void preStep2_shouldCreateChatModelConfig() throws Exception {
        String response = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/models", WORKSPACE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                    "name": "test-workflow-chat-model",
                                    "type": "CHAT",
                                    "supplier": "OPENROUTER",
                                    "apiKey": "%s",
                                    "model": "openrouter/free",
                                    "baseUrl": "https://openrouter.ai/api/v1",
                                    "temperature": 0.7,
                                    "maxTokens": 2048,
                                    "enabled": true
                                }
                                """, key)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        chatModelConfigId = extractId(response);
        Assertions.assertNotNull(chatModelConfigId);
        log.info("✅ 创建聊天模型配置: {}", chatModelConfigId);
    }

    @Test
    @Order(3)
    @DisplayName("前置Step 3: 创建嵌入模型配置")
    void preStep3_shouldCreateEmbeddingModelConfig() throws Exception {
        String response = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/models", WORKSPACE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "test-workflow-embedding",
                                    "type": "EMBEDDING",
                                    "supplier": "OPENAI",
                                    "model": "qwen3-embedding",
                                    "baseUrl": "http://127.0.0.1:9090",
                                    "apiKey": "xxx",
                                    "enabled": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        embeddingModelConfigId = extractId(response);
        Assertions.assertNotNull(embeddingModelConfigId);
        log.info("✅ 创建嵌入模型配置: {}", embeddingModelConfigId);
    }

    @Test
    @Order(4)
    @DisplayName("前置Step 4: 创建知识库")
    void preStep4_shouldCreateKnowledgeBase() throws Exception {
        String response = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/knowledge-bases", WORKSPACE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                    "tenantId": "%s",
                                    "workspaceId": "%s",
                                    "kbCode": "test-workflow-%s",
                                    "name": "Test Workflow Knowledge Base",
                                    "description": "Knowledge base for workflow integration test",
                                    "indexProvider": "QDRANT",
                                    "vectorStoreConfigId": "%s",
                                    "embeddingModelConfigId": "%s",
                                    "chatModelConfigId": "%s"
                                }
                                """, TENANT_ID, WORKSPACE_ID,randomShortId(), vectorStoreConfigId, embeddingModelConfigId, chatModelConfigId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        knowledgeBaseId = extractId(response);
        Assertions.assertNotNull(knowledgeBaseId);
        log.info("✅ 创建知识库: {}", knowledgeBaseId);
    }

    @Test
    @Order(5)
    @DisplayName("前置Step 5: 上传测试文件到知识库")
    void preStep5_shouldUploadFileToKnowledgeBase() throws Exception {
        String testContent = generateTestMarkdown();
        MockMultipartFile file = new MockMultipartFile(
                "file", "test-workflow-knowledge.md", "text/markdown",
                testContent.getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/v1/workspaces/{workspaceId}/knowledge-bases/{kbId}/documents", WORKSPACE_ID, knowledgeBaseId)
                        .file(file))
                .andExpect(status().is2xxSuccessful());

        log.info("✅ 上传测试文档到知识库");
    }

    @Test
    @Order(6)
    @DisplayName("前置Step 6: 创建检索策略")
    void preStep6_shouldCreateRetrievalStrategy() throws Exception {
        String response = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/retrieval-strategies", WORKSPACE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                    "name": "test-workflow-retrieval-strategy",
                                    "description": "Test retrieval strategy for workflow",
                                    "retrievalType": "HYBRID",
                                    "topK": 10,
                                    "similarityThreshold": 0.75,
                                    "rerankEnabled": true,
                                    "knowledgeBaseIds": ["%s"]
                                }
                                """, knowledgeBaseId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        retrievalStrategyId = extractId(response);
        Assertions.assertNotNull(retrievalStrategyId);
        log.info("✅ 创建检索策略: {}", retrievalStrategyId);
    }

    @Test
    @Order(7)
    @DisplayName("前置Step 7: 创建模型策略")
    void preStep7_shouldCreateModelStrategy() throws Exception {
        String response = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/model-strategies", WORKSPACE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                    "name": "test-workflow-model-strategy",
                                    "description": "Test model strategy for workflow",
                                    "chatModelConfigId": "%s",
                                    "embeddingModelConfigId": "%s",
                                    "temperature": 0.7,
                                    "maxTokens": 2048,
                                    "enabled": true
                                }
                                """, chatModelConfigId, embeddingModelConfigId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        modelStrategyId = extractId(response);
        Assertions.assertNotNull(modelStrategyId);
        log.info("✅ 创建模型策略: {}", modelStrategyId);
    }

    @Test
    @Order(8)
    @DisplayName("前置Step 8: 创建Agent")
    void preStep8_shouldCreateAgent() throws Exception {
        String response = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/agents", WORKSPACE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                    "tenantId": "%s",
                                    "workspaceId": "%s",
                                    "agentCode": "test-workflow-agent",
                                    "name": "test-workflow-lifecycle-agent-%s",
                                    "description": "Agent for workflow integration test"
                                }
                                """, TENANT_ID, WORKSPACE_ID, RandomUtils.randomShortId())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        agentId = extractId(response);
        Assertions.assertNotNull(agentId);
        log.info("✅ 创建Agent: {}", agentId);
    }

    @Test
    @Order(9)
    @DisplayName("前置Step 9: 配置Agent关联策略")
    void preStep9_shouldConfigureAgentWithStrategies() throws Exception {
        Assertions.assertNotNull(agentId);
        Assertions.assertNotNull(retrievalStrategyId);

        // 配置检索策略
        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/agents/{agentId}/configs", WORKSPACE_ID, agentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                    "category": "STRATEGY",
                                    "type": "RETRIEVAL_STRATEGY",
                                    "configId": "%s",
                                    "description": "Retrieval strategy config",
                                    "priority": 1,
                                    "enabled": true
                                }
                                """, retrievalStrategyId)))
                .andExpect(status().isCreated());

        // 配置模型策略
        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/agents/{agentId}/configs", WORKSPACE_ID, agentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                    "category": "STRATEGY",
                                    "type": "MODEL_STRATEGY",
                                    "configId": "%s",
                                    "description": "Model strategy config",
                                    "priority": 1,
                                    "enabled": true
                                }
                                """, modelStrategyId)))
                .andExpect(status().isCreated());

        // 配置聊天模型
        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/agents/{agentId}/configs", WORKSPACE_ID, agentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                    "category": "MODEL",
                                    "type": "CHAT_MODEL",
                                    "configId": "%s",
                                    "description": "Chat model config",
                                    "priority": 1,
                                    "enabled": true
                                }
                                """, chatModelConfigId)))
                .andExpect(status().isCreated());

        // 配置嵌入模型
        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/agents/{agentId}/configs", WORKSPACE_ID, agentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                    "category": "MODEL",
                                    "type": "EMBEDDING_MODEL",
                                    "configId": "%s",
                                    "description": "Embedding model config",
                                    "priority": 1,
                                    "enabled": true
                                }
                                """, embeddingModelConfigId)))
                .andExpect(status().isCreated());

        log.info("✅ 配置Agent关联策略完成");
    }

    @Test
    @Order(10)
    @DisplayName("前置Step 10: 发布Agent")
    void preStep10_shouldPublishAgent() throws Exception {
        Assertions.assertNotNull(agentId);

        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/agents/{agentId}/enabled", WORKSPACE_ID, agentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));

        log.info("✅ 发布Agent成功");
    }

    @Test
    @Order(11)
    @DisplayName("前置Step 11: 创建MCP工具")
    void preStep11_shouldCreateMcpTool() throws Exception {
        String responseBody = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/mcp-tools", WORKSPACE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                    "name": "server-filesystem",
                                    "description": "File system MCP server for workflow test",
                                    "serverUrl": "npx",
                                    "serverType": "STDIO",
                                    "command": "npx",
                                    "args": %s,
                                    "env": %s,
                                    "enabled": true
                                }
                                """, toJson(List.of("-y", "@modelcontextprotocol/server-filesystem", "E:\\Code\\vibe\\AgentHub")),
                                toJson(Map.of("NODE_ENV", "production")))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn().getResponse().getContentAsString();

        mcpToolId = objectMapper.readTree(responseBody).get("id").asText();
        Assertions.assertNotNull(mcpToolId);
        log.info("✅ 创建MCP工具: {}", mcpToolId);
    }

    @Test
    @Order(12)
    @DisplayName("Step 1: 创建空工作流")
    void step1_shouldCreateWorkflow() throws Exception {
        String response = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/dag-workflows", WORKSPACE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                    {
                        "name": "复杂工作流集成测试",
                        "description": "覆盖所有12种节点类型的完整生命周期测试",
                        "workflowCode": "complex-full-lifecycle",
                        "workspaceId": "100000002",
                        "tenantId": "100000002",
                        "graphDefinition": "{}"
                    }
                    """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        workflowId = json.get("id").asText();
        Assertions.assertNotNull(workflowId);
        log.info("✅ 创建工作流成功: {}, name={}", workflowId, json.get("name").asText());
    }

    @Test
    @Order(13)
    @DisplayName("Step 2: 更新工作流 - 设置复杂graphDefinition")
    void step2_shouldUpdateWorkflowWithComplexGraph() throws Exception {
        String requestBody = buildRequestBodyWithGraphDef(
                "复杂工作流集成测试",
                "覆盖所有12种节点类型的完整生命周期测试");

        String response = mockMvc.perform(put("/api/v1/workspaces/{workspaceId}/dag-workflows/{workflowId}",
                WORKSPACE_ID, workflowId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        String storedGraphDef = json.get("graphDefinition").asText();
        Assertions.assertNotNull(storedGraphDef);
        Assertions.assertTrue(storedGraphDef.contains("start-001"),
                "graphDefinition应包含start-001");
        log.info("✅ 更新工作流 - 设置复杂graphDefinition成功");
    }

    @Test
    @Order(14)
    @DisplayName("Step 3: 验证graphDefinition包含所有节点类型")
    void step3_shouldVerifyComplexGraphDefinition() throws Exception {
        String response = mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/dag-workflows/{workflowId}",
                WORKSPACE_ID, workflowId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(workflowId))
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        String graphDef = json.get("graphDefinition").asText();

        // 解析graphDefinition
        JsonNode graphJson = objectMapper.readTree(graphDef);
        JsonNode nodes = graphJson.get("nodes");
        JsonNode edges = graphJson.get("edges");

        Assertions.assertNotNull(nodes, "nodes不能为空");
        Assertions.assertEquals(12, nodes.size(), "应该包含12个节点");
        Assertions.assertNotNull(edges, "edges不能为空");
        Assertions.assertEquals(13, edges.size(), "应该包含13条边");

        // 验证所有12种节点类型
        java.util.Map<String, Boolean> typeMap = new java.util.HashMap<>();
        typeMap.put("start", false);
        typeMap.put("variable", false);
        typeMap.put("llm", false);
        typeMap.put("condition", false);
        typeMap.put("loop", false);
        typeMap.put("tool", false);
        typeMap.put("parallel", false);
        typeMap.put("api", false);
        typeMap.put("code", false);
        typeMap.put("retrieval", false);
        typeMap.put("sub-workflow", false);
        typeMap.put("end", false);

        for (JsonNode node : nodes) {
            String nodeType = node.get("type").asText();
            Assertions.assertTrue(typeMap.containsKey(nodeType),
                    "不支持的节点类型: " + nodeType);
            typeMap.put(nodeType, true);

            // 验证每个节点的数据结构
            Assertions.assertNotNull(node.get("id"), "节点ID不能为空");
            Assertions.assertNotNull(node.get("position"), "节点position不能为空");
            Assertions.assertTrue(node.get("position").has("x"), "position必须有x");
            Assertions.assertTrue(node.get("position").has("y"), "position必须有y");
            Assertions.assertNotNull(node.get("data"), "节点data不能为空");
            Assertions.assertTrue(node.get("data").has("label"), "data必须有label");
            Assertions.assertTrue(node.get("data").has("node_param"), "data必须有node_param");
        }

        // 验证所有类型都已覆盖
        for (java.util.Map.Entry<String, Boolean> entry : typeMap.entrySet()) {
            Assertions.assertTrue(entry.getValue(),
                    "缺少节点类型: " + entry.getKey());
        }

        // 验证边结构
        for (JsonNode edge : edges) {
            Assertions.assertNotNull(edge.get("id"), "边ID不能为空");
            Assertions.assertNotNull(edge.get("source"), "边的source不能为空");
            Assertions.assertNotNull(edge.get("target"), "边的target不能为空");
        }

        log.info("✅ 验证graphDefinition: {}个节点, {}条边, 覆盖所有类型", nodes.size(), edges.size());
    }

    @Test
    @Order(15)
    @DisplayName("Step 4: 验证复杂graphDefinition的JSON结构完整性")
    void step4_shouldVerifyGraphDefinitionStructure() throws Exception {
        String response = mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/dag-workflows/{workflowId}",
                WORKSPACE_ID, workflowId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        String graphDef = json.get("graphDefinition").asText();

        // 验证graphDefinition是合法JSON且可以被多次解析
        JsonNode graphJson = objectMapper.readTree(graphDef);

        // 验证节点顺序（线性链的正确性）
        JsonNode nodes = graphJson.get("nodes");
        String[] expectedOrder = {"start", "variable", "llm", "condition", "loop",
                "tool", "parallel", "api", "code", "retrieval", "sub-workflow", "end"};
        for (int i = 0; i < nodes.size(); i++) {
            String actualType = nodes.get(i).get("type").asText();
            Assertions.assertEquals(expectedOrder[i], actualType,
                    "节点顺序错误: 位置" + i + "应该是" + expectedOrder[i] + "但实际是" + actualType);
        }

        // 验证特殊节点的参数结构
        JsonNode variableNode = findNodeByType(nodes, "variable");
        Assertions.assertTrue(
                variableNode.get("data").get("node_param").has("assignments"),
                "variable节点必须有assignments参数");

        JsonNode conditionNode = findNodeByType(nodes, "condition");
        Assertions.assertTrue(
                conditionNode.get("data").get("node_param").has("branches"),
                "condition节点必须有branches参数");
        Assertions.assertEquals(3,
                conditionNode.get("data").get("node_param").get("branches").size(),
                "condition应该有3个分支");

        JsonNode llmNode = findNodeByType(nodes, "llm");
        Assertions.assertNotNull(
                llmNode.get("data").get("node_param").get("agentId"),
                "LLM节点agentId不应为空");
        Assertions.assertNotEquals("",
                llmNode.get("data").get("node_param").get("agentId").asText(),
                "LLM节点agentId应为真实ID");

        JsonNode retrievalNode = findNodeByType(nodes, "retrieval");
        Assertions.assertNotNull(
                retrievalNode.get("data").get("node_param").get("knowledgeBaseId"),
                "检索节点knowledgeBaseId不应为空");
        Assertions.assertNotEquals("",
                retrievalNode.get("data").get("node_param").get("knowledgeBaseId").asText(),
                "检索节点knowledgeBaseId应为真实ID");

        JsonNode parallelNode = findNodeByType(nodes, "parallel");
        Assertions.assertEquals(2,
                parallelNode.get("data").get("node_param").get("concurrency").asInt(),
                "并行节点concurrency应为2");

        log.info("✅ graphDefinition结构验证通过 - 所有12种节点类型参数完整");
    }

    @Test
    @Order(16)
    @DisplayName("Step 5: 再次更新graphDefinition并验证持久化")
    void step5_shouldReUpdateAndVerifyPersistence() throws Exception {
        String requestBody = buildRequestBodyWithGraphDef(
                "复杂工作流-已更新",
                "验证graphDefinition持久化-更新版本");

        mockMvc.perform(put("/api/v1/workspaces/{workspaceId}/dag-workflows/{workflowId}",
                WORKSPACE_ID, workflowId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("复杂工作流-已更新"));

        // 验证更新持久化
        String response = mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/dag-workflows/{workflowId}",
                WORKSPACE_ID, workflowId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        Assertions.assertEquals("复杂工作流-已更新", json.get("name").asText());

        String graphDef = json.get("graphDefinition").asText();
        JsonNode graphJson = objectMapper.readTree(graphDef);
        Assertions.assertEquals(12, graphJson.get("nodes").size(),
                "更新后graphDefinition仍应包含12个节点");

        log.info("✅ graphDefinition持久化验证通过");
    }

    @Test
    @Order(17)
    @DisplayName("Step 6: 发布工作流")
    void step6_shouldPublishWorkflow() throws Exception {
        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/dag-workflows/{workflowId}/publish",
                WORKSPACE_ID, workflowId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));

        log.info("✅ 发布工作流成功");
    }

    @Test
    @Order(18)
    @DisplayName("Step 7: 发布后查询 - 验证graphDefinition不变")
    void step7_shouldVerifyGraphAfterPublish() throws Exception {
        String response = mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/dag-workflows/{workflowId}",
                WORKSPACE_ID, workflowId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        String graphDef = json.get("graphDefinition").asText();
        JsonNode graphJson = objectMapper.readTree(graphDef);

        // 发布后graphDefinition完整性和内容不变
        Assertions.assertEquals(12, graphJson.get("nodes").size(),
                "发布后graphDefinition节点数不变");
        Assertions.assertEquals(13, graphJson.get("edges").size(),
                "发布后graphDefinition边数不变");

        log.info("✅ 发布后验证通过 - graphDefinition完整保留");
    }

    @Test
    @Order(19)
    @DisplayName("Step 8: 执行已发布的工作流")
    void step8_shouldExecutePublishedWorkflow() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/dag-workflows/{workflowId}/execute",
                WORKSPACE_ID, workflowId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"input\":{},\"triggeredBy\":\"test\"}"))
                .andExpect(request().asyncStarted())
                .andReturn();

        String response = mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        executionId = json.get("task_id").asText();
        Assertions.assertNotNull(executionId, "executionId不应为空");
        Assertions.assertNotNull(json.get("status"), "status不应为空");
        log.info("✅ 执行工作流成功: executionId={}, status={}", executionId, json.get("status").asText());
    }

    @Test
    @Order(20)
    @DisplayName("Step 9: 查询工作流执行结果")
    void step9_shouldGetExecutionResult() throws Exception {
        Assertions.assertNotNull(executionId, "executionId不能为空，请先执行Step 8");

        String finalStatus = null;
        long deadline = System.currentTimeMillis() + 120000; // 增加到2分钟，因为要真实调用LLM
        while (System.currentTimeMillis() < deadline) {
            MvcResult pollResult = mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/dag-workflows/{workflowId}/executions/{executionId}",
                    WORKSPACE_ID, workflowId, executionId))
                    .andExpect(request().asyncStarted())
                    .andReturn();
            pollResult.getAsyncResult(TimeUnit.SECONDS.toMillis(100));
            String response = mockMvc.perform(asyncDispatch(pollResult))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            JsonNode json = objectMapper.readTree(response);
            finalStatus = json.get("status").asText();

            if ("success".equalsIgnoreCase(finalStatus) || "failed".equalsIgnoreCase(finalStatus)) {
                log.info("✅ 执行完成: status={}", finalStatus);
                break;
            }
            log.info("⏳ 工作流执行中: status={}", finalStatus);
            Thread.sleep(2000);
        }

        // 🔴 关键修复：必须断言状态为 success，不能是 failed
        Assertions.assertNotNull(finalStatus, "执行结果不应为空");
        Assertions.assertEquals("success", finalStatus.toLowerCase(),
                "工作流执行必须成功，但实际状态为: " + finalStatus + ". 请检查日志中的节点执行错误");
        
        log.info("✅ 查询执行结果成功: executionId={}, status={}", executionId, finalStatus);
    }

    @Test
    @Order(21)
    @DisplayName("Step 10: 取消发布工作流")
    void step10_shouldUnpublishWorkflow() throws Exception {
        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/dag-workflows/{workflowId}/unpublish",
                WORKSPACE_ID, workflowId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DRAFT"));

        log.info("✅ 取消发布成功");
    }

    @Test
    @Order(22)
    @DisplayName("Step 11: 取消发布后查询 - 验证状态回滚")
    void step11_shouldVerifyGraphAfterUnpublish() throws Exception {
        String response = mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/dag-workflows/{workflowId}",
                WORKSPACE_ID, workflowId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        String graphDef = json.get("graphDefinition").asText();
        JsonNode graphJson = objectMapper.readTree(graphDef);

        // 取消发布后graphDefinition保持完整
        Assertions.assertEquals(12, graphJson.get("nodes").size());
        Assertions.assertEquals(13, graphJson.get("edges").size());

        log.info("✅ 取消发布后验证通过");
    }

    @Test
    @Order(23)
    @DisplayName("Step 12: 删除工作流")
    void step12_shouldDeleteWorkflow() throws Exception {
        mockMvc.perform(delete("/api/v1/workspaces/{workspaceId}/dag-workflows/{workflowId}",
                WORKSPACE_ID, workflowId))
                .andExpect(status().isNoContent());

        // 验证删除后查询返回404
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/dag-workflows/{workflowId}",
                WORKSPACE_ID, workflowId))
                .andExpect(status().isNotFound());

        log.info("✅ 删除工作流成功");
    }

    @Test
    @Order(24)
    @DisplayName("Step 13: 创建并立即设置复杂graphDefinition工作流")
    void step13_shouldCreateWorkflowWithComplexGraphDirectly() throws Exception {
        String requestBody = buildCreateRequestBody(
                "复杂工作流-一步创建",
                "complex-direct-create");

        String response = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/dag-workflows", WORKSPACE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        String createdWorkflowId = json.get("id").asText();
        Assertions.assertNotNull(createdWorkflowId);

        // 验证创建时已包含graphDefinition
        String graphDef = json.get("graphDefinition").asText();
        Assertions.assertNotNull(graphDef);
        Assertions.assertTrue(graphDef.contains("start-001"),
                "graphDefinition应包含start-001节点");

        // 清理
        mockMvc.perform(delete("/api/v1/workspaces/{workspaceId}/dag-workflows/{workflowId}",
                WORKSPACE_ID, createdWorkflowId))
                .andExpect(status().isNoContent());

        log.info("✅ 一步创建复杂工作流成功并清理");
    }

    // ==================== 辅助方法 ====================

    /**
     * 从响应中提取ID。
     */
    private String extractId(String response) throws Exception {
        JsonNode json = objectMapper.readTree(response);
        return json.get("id").asText();
    }

    /**
     * 生成测试用的Markdown文档内容。
     */
    private String generateTestMarkdown() {
        return """
                # AgentHub 工作流测试文档
                
                ## 简介
                这是一个用于工作流集成测试的知识库文档。
                
                ## 功能特性
                - 支持多种节点类型：LLM、Retrieval、Tool、API、Code等
                - 支持条件判断、循环、并行执行
                - 支持变量引用和上下文传递
                
                ## 测试数据
                本文档包含以下测试信息：
                1. AgentHub 是一个AI Agent平台
                2. 支持工作流编排和自动化执行
                3. 集成RAG知识库检索能力
                4. 支持MCP工具调用
                
                ## 结论
                工作流测试文档生成成功。
                """;
    }

    /**
     * 替换graphDefinition中的占位符为真实ID。
     */
    private String replacePlaceholders(String graphDef) {
        return graphDef
                .replace("${AGENT_ID}", agentId != null ? agentId : "")
                .replace("${KNOWLEDGE_BASE_ID}", knowledgeBaseId != null ? knowledgeBaseId : "")
                .replace("${MCP_TOOL_ID}", mcpToolId != null ? mcpToolId : "");
    }

    /**
     * 构建包含graphDefinition的请求体。
     * 将COMPLEX_GRAPH_DEFINITION（JSON对象）序列化为JSON字符串后嵌入请求体。
     */
    private String buildRequestBodyWithGraphDef(String name, String description) throws Exception {
        String graphDefWithPlaceholders = COMPLEX_GRAPH_DEFINITION;
        String graphDefWithRealIds = replacePlaceholders(graphDefWithPlaceholders);
        
        JsonNode graphJson = objectMapper.readTree(graphDefWithRealIds);
        String graphDefString = objectMapper.writeValueAsString(graphJson);
        return """
            {
                "name": "%s",
                "description": "%s",
                "graphDefinition": "%s"
            }
            """.formatted(name, description, graphDefString
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\""));
    }

    /**
     * 构建创建工作流请求体。
     */
    private String buildCreateRequestBody(String name, String workflowCode) throws Exception {
        String graphDefWithPlaceholders = COMPLEX_GRAPH_DEFINITION;
        String graphDefWithRealIds = replacePlaceholders(graphDefWithPlaceholders);
        
        JsonNode graphJson = objectMapper.readTree(graphDefWithRealIds);
        String graphDefString = objectMapper.writeValueAsString(graphJson);
        return """
            {
                "name": "%s",
                "description": "创建时直接包含复杂graphDefinition",
                "workflowCode": "%s",
                "workspaceId": "100000002",
                "tenantId": "100000002",
                "graphDefinition": "%s"
            }
            """.formatted(name, workflowCode, graphDefString
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\""));
    }

    private JsonNode findNodeByType(JsonNode nodes, String type) {
        for (JsonNode node : nodes) {
            if (node.get("type").asText().equals(type)) {
                return node;
            }
        }
        throw new AssertionError("未找到节点类型: " + type);
    }

    // ==================== 清理逻辑 ====================

    @AfterAll
    void cleanup() throws Exception {
        if (mockMvc == null) return;

        log.info("\ud83e\uddf9 开始清理测试资源...");

        cleanupMcpTool();
        cleanupAgent();
        cleanupStrategies();
        cleanupKnowledgeBase();
        cleanupModelConfigs();
        cleanupVectorStoreConfig();

        log.info("\u2705 测试资源清理完成");
    }

    private void cleanupMcpTool() throws Exception {
        if (mcpToolId != null) {
            try {
                mockMvc.perform(delete("/api/v1/workspaces/{workspaceId}/mcp-tools/{id}", WORKSPACE_ID, mcpToolId))
                        .andExpect(status().isNoContent());
                log.info("  已删除MCP工具: {}", mcpToolId);
            } catch (Exception e) {
                log.warn("  删除MCP工具失败: {}", e.getMessage());
            }
        }
    }

    private void cleanupAgent() throws Exception {
        if (agentId != null) {
            try {
                mockMvc.perform(delete("/api/v1/workspaces/{workspaceId}/agents/{agentId}", WORKSPACE_ID, agentId))
                        .andExpect(status().isNoContent());
                log.info("  已删除Agent: {}", agentId);
            } catch (Exception e) {
                log.warn("  删除Agent失败: {}", e.getMessage());
            }
        }
    }

    private void cleanupStrategies() throws Exception {
        try {
            if (retrievalStrategyId != null) {
                mockMvc.perform(delete("/api/v1/workspaces/{workspaceId}/retrieval-strategies/{id}", WORKSPACE_ID, retrievalStrategyId))
                        .andExpect(status().isNoContent());
                log.info("  已删除检索策略: {}", retrievalStrategyId);
            }
        } catch (Exception e) {
            log.warn("  删除检索策略失败: {}", e.getMessage());
        }
        try {
            if (modelStrategyId != null) {
                mockMvc.perform(delete("/api/v1/workspaces/{workspaceId}/model-strategies/{id}", WORKSPACE_ID, modelStrategyId))
                        .andExpect(status().isNoContent());
                log.info("  已删除模型策略: {}", modelStrategyId);
            }
        } catch (Exception e) {
            log.warn("  删除模型策略失败: {}", e.getMessage());
        }
    }

    private void cleanupKnowledgeBase() throws Exception {
        if (knowledgeBaseId != null) {
            try {
                mockMvc.perform(delete("/api/v1/workspaces/{workspaceId}/knowledge-bases/{kbId}", WORKSPACE_ID, knowledgeBaseId))
                        .andExpect(status().isNoContent());
                log.info("  已删除知识库: {}", knowledgeBaseId);
            } catch (Exception e) {
                log.warn("  删除知识库失败: {}", e.getMessage());
            }
        }
    }

    private void cleanupModelConfigs() throws Exception {
        if (chatModelConfigId != null) {
            try {
                mockMvc.perform(delete("/api/v1/workspaces/{workspaceId}/models/{id}", WORKSPACE_ID, chatModelConfigId))
                        .andExpect(status().isNoContent());
                log.info("  已删除聊天模型: {}", chatModelConfigId);
            } catch (Exception e) {
                log.warn("  删除聊天模型失败: {}", e.getMessage());
            }
        }
        if (embeddingModelConfigId != null) {
            try {
                mockMvc.perform(delete("/api/v1/workspaces/{workspaceId}/models/{id}", WORKSPACE_ID, embeddingModelConfigId))
                        .andExpect(status().isNoContent());
                log.info("  已删除嵌入模型: {}", embeddingModelConfigId);
            } catch (Exception e) {
                log.warn("  删除嵌入模型失败: {}", e.getMessage());
            }
        }
    }

    private void cleanupVectorStoreConfig() throws Exception {
        if (vectorStoreConfigId != null) {
            try {
                mockMvc.perform(delete("/api/v1/workspaces/{workspaceId}/vector-stores/{id}", WORKSPACE_ID, vectorStoreConfigId))
                        .andExpect(status().isNoContent());
                log.info("  已删除向量存储: {}", vectorStoreConfigId);
            } catch (Exception e) {
                log.warn("  删除向量存储失败: {}", e.getMessage());
            }
        }
    }
}
