//package com.agenthub.infrastructure.agents;
//
//import com.agenthub.infrastructure.agents.core.Agent;
//import com.agenthub.infrastructure.agents.core.AgentCommunicationBus;
//import com.agenthub.infrastructure.agents.core.SupervisorOrchestrator;
//import com.agenthub.infrastructure.agents.model.AgentMessage;
//import com.agenthub.infrastructure.agents.model.AgentResult;
//import com.agenthub.infrastructure.agents.protocol.A2AProtocol;
//import com.agenthub.infrastructure.agents.protocol.DefaultA2AProtocol;
//import com.agenthub.infrastructure.agents.tools.CodeAnalysisTools;
//import com.agenthub.infrastructure.agents.tools.FileSystemTools;
//import org.junit.jupiter.api.*;
//import org.springframework.ai.chat.client.ChatClient;
//import org.springframework.ai.chat.memory.ChatMemory;
//import org.springframework.ai.chat.memory.MessageWindowChatMemory;
//import org.springframework.context.ApplicationEventPublisher;
//import java.util.*;
//import java.util.concurrent.TimeUnit;
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
///**
// * Agent完整集成测试
// * 继承AgentTestBase，使用真实的ChatModel和Agent
// */
//@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//@Timeout(value = 120, unit = TimeUnit.SECONDS)
//@DisplayName("Agent完整集成测试")
//class AgentIntegratedTest extends AgentTestBase {
//
//    private AgentCommunicationBus communicationBus;
//    private A2AProtocol a2aProtocol;
//
//    @BeforeEach
//    void setUp() {
//        ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
//        communicationBus = new AgentCommunicationBus(eventPublisher);
//        a2aProtocol = new DefaultA2AProtocol(eventPublisher);
//    }
//
//    // ==================== 基础功能测试 (Order 1-10) ====================
//
//    @Test
//    @Order(1)
//    @DisplayName("1. Agent基础功能")
//    void testAgentBasicFunctionality() {
//        assertNotNull(agent, "Agent不应为null");
//        assertEquals("multi-tool-agent", agent.getName());
//        assertEquals("多工具Agent", agent.getDescription());
//    }
//
//    @Test
//    @Order(2)
//    @DisplayName("2. Agent执行功能")
//    void testAgentExecution() {
//        AgentResult result = agent.run("你好");
//        assertNotNull(result, "结果不应为null");
//        assertNotNull(result.steps(), "步骤列表不应为null");
//    }
//
//    @Test
//    @Order(3)
//    @DisplayName("3. 单轮对话")
//    void testSingleTurnConversation() {
//        AgentResult result = agent.run("请简单介绍一下你自己");
//        assertNotNull(result, "对话结果不应为null");
//        assertNotNull(result.content(), "回复内容不应为null");
//    }
//
//    @Test
//    @Order(4)
//    @DisplayName("4. 多轮对话")
//    void testMultiTurnConversation() {
//        AgentResult result1 = agent.run("我叫张三");
//        assertNotNull(result1, "第一轮结果不应为null");
//
//        AgentResult result2 = agent.run("我的名字是什么？");
//        assertNotNull(result2, "第二轮结果不应为null");
//    }
//
//    @Test
//    @Order(5)
//    @DisplayName("5. 工具调用")
//    void testToolInvocation() {
//        AgentResult result = agent.run("列出当前目录的文件");
//        assertNotNull(result, "工具调用结果不应为null");
//    }
//
//    @Test
//    @Order(6)
//    @DisplayName("6. 代码分析")
//    void testCodeAnalysis() {
//        AgentResult result = agent.run("分析代码：public void test() {}");
//        assertNotNull(result, "代码分析结果不应为null");
//    }
//
//    @Test
//    @Order(7)
//    @DisplayName("7. A2A协议通信")
//    void testA2AProtocol() {
//        String agentName = "test-agent";
//        ((DefaultA2AProtocol) a2aProtocol).registerAgent(agentName);
//
//        AgentMessage message = AgentMessage.task("sender", agentName, "测试消息");
//        a2aProtocol.send(message);
//
//        AgentMessage received = a2aProtocol.receive(agentName);
//        assertNotNull(received, "接收的消息不应为null");
//        assertEquals("sender", received.fromAgent());
//        assertEquals("task", received.type());
//    }
//
//    @Test
//    @Order(8)
//    @DisplayName("8. Agent状态管理")
//    void testAgentStatusManagement() {
//        String agentName = "status-agent";
//        ((DefaultA2AProtocol) a2aProtocol).registerAgent(agentName);
//
//        assertEquals(A2AProtocol.AgentStatus.IDLE, a2aProtocol.getStatus(agentName));
//
//        ((DefaultA2AProtocol) a2aProtocol).updateStatus(agentName, A2AProtocol.AgentStatus.BUSY);
//        assertEquals(A2AProtocol.AgentStatus.BUSY, a2aProtocol.getStatus(agentName));
//    }
//
//    @Test
//    @Order(9)
//    @DisplayName("9. AgentResult测试")
//    void testAgentResult() {
//        List<com.agenthub.infrastructure.agents.model.AgentStep> steps = new ArrayList<>();
//        steps.add(new com.agenthub.infrastructure.agents.model.AgentStep(0, "step1", 100));
//
//        AgentResult successResult = AgentResult.success("成功", steps);
//        assertTrue(successResult.success());
//        assertEquals("成功", successResult.content());
//        assertEquals(1, successResult.steps().size());
//
//        AgentResult errorResult = AgentResult.error("错误");
//        assertFalse(errorResult.success());
//        assertEquals("错误", errorResult.errorMessage());
//    }
//
//    @Test
//    @Order(10)
//    @DisplayName("10. AgentMessage测试")
//    void testAgentMessage() {
//        AgentMessage taskMessage = AgentMessage.task("from", "to", "任务");
//        assertEquals("from", taskMessage.fromAgent());
//        assertEquals("to", taskMessage.toAgent());
//        assertEquals("task", taskMessage.type());
//        assertEquals("任务", taskMessage.content());
//
//        AgentMessage resultMessage = AgentMessage.result("from", "to", "结果");
//        assertEquals("result", resultMessage.type());
//
//        AgentMessage errorMessage = AgentMessage.error("from", "to", "错误");
//        assertEquals("error", errorMessage.type());
//    }
//
//    // ==================== 高级功能测试 (Order 11-20) ====================
//
//    @Test
//    @Order(11)
//    @DisplayName("11. MCP工具发现")
//    void testMCPToolDiscovery() {
//        FileSystemTools fileTools = new FileSystemTools();
//        CodeAnalysisTools codeTools = new CodeAnalysisTools();
//
//        assertEquals("file_system", fileTools.getName());
//        assertEquals("code_analysis", codeTools.getName());
//        assertEquals("文件系统操作工具", fileTools.getDescription());
//        assertEquals("代码分析和统计工具", codeTools.getDescription());
//    }
//
//    @Test
//    @Order(12)
//    @DisplayName("12. Skill技能执行")
//    void testSkillExecution() {
//        AgentResult result = agent.run("审查代码质量");
//        assertNotNull(result, "技能执行结果不应为null");
//    }
//
//    @Test
//    @Order(13)
//    @DisplayName("13. RAG知识检索")
//    void testRAGKnowledgeRetrieval() {
//        AgentResult result = agent.run("检索Spring AI信息");
//        assertNotNull(result, "RAG检索结果不应为null");
//    }
//
//    @Test
//    @Order(14)
//    @DisplayName("14. 长期记忆存储")
//    void testLongTermMemoryStorage() {
//        AgentResult result = agent.run("记住：用户偏好Java");
//        assertNotNull(result, "记忆存储结果不应为null");
//    }
//
//    @Test
//    @Order(15)
//    @DisplayName("15. 长期记忆检索")
//    void testLongTermMemoryRetrieval() {
//        agent.run("记住：用户喜欢Python");
//        AgentResult result = agent.run("用户喜欢什么？");
//        assertNotNull(result, "记忆检索结果不应为null");
//    }
//
//    @Test
//    @Order(16)
//    @DisplayName("16. 上下文压缩")
//    void testContextCompression() {
//        String longContext = "长文本".repeat(20);
//        AgentResult result = agent.run("处理：" + longContext);
//        assertNotNull(result, "上下文压缩结果不应为null");
//    }
//
//    @Test
//    @Order(17)
//    @DisplayName("17. 上下文摘要")
//    void testContextSummarization() {
//        agent.run("讨论Java");
//        agent.run("讨论Python");
//        AgentResult result = agent.run("总结对话");
//        assertNotNull(result, "上下文摘要结果不应为null");
//    }
//
//    @Test
//    @Order(18)
//    @DisplayName("18. Multi-Agent协作")
//    void testMultiAgentCollaboration() {
//        ChatMemory memory = MessageWindowChatMemory.builder().build();
//        ChatClient chatClient = ChatClient.builder(chatModel).build();
//
//        Map<String, Agent> agents = new HashMap<>();
//        agents.put("agent-1", agent);
//        agents.put("agent-2", new com.agenthub.infrastructure.agents.core.DefaultReActAgent(
//            "agent-2", "测试Agent2", chatClient, memory, new ArrayList<>(), 3
//        ));
//
//        SupervisorOrchestrator orchestrator = new SupervisorOrchestrator(
//            chatModel, agents, communicationBus
//        );
//
//        String result = orchestrator.orchestrate("分析代码");
//        assertNotNull(result, "Multi-Agent协作结果不应为null");
//    }
//
//    @Test
//    @Order(19)
//    @DisplayName("19. Agent间通信")
//    void testInterAgentCommunication() {
//        String agent1 = "agent-1";
//        String agent2 = "agent-2";
//
//        ((DefaultA2AProtocol) a2aProtocol).registerAgent(agent1);
//        ((DefaultA2AProtocol) a2aProtocol).registerAgent(agent2);
//
//        AgentMessage task = AgentMessage.task(agent1, agent2, "分析代码");
//        a2aProtocol.send(task);
//
//        AgentMessage received = a2aProtocol.receive(agent2);
//        assertNotNull(received, "Agent2应收到消息");
//        assertEquals("task", received.type());
//
//        AgentMessage result = AgentMessage.result(agent2, agent1, "完成");
//        a2aProtocol.send(result);
//
//        AgentMessage finalResult = a2aProtocol.receive(agent1);
//        assertNotNull(finalResult, "Agent1应收到结果");
//        assertEquals("result", finalResult.type());
//    }
//
//    @Test
//    @Order(20)
//    @DisplayName("20. 全功能集成")
//    void testFullFeatureIntegration() {
//        AgentResult result = agent.run("使用所有功能完成分析");
//        assertNotNull(result, "全功能集成结果不应为null");
//    }
//
//    // ==================== 边界和错误处理测试 (Order 21-25) ====================
//
//    @Test
//    @Order(21)
//    @DisplayName("21. 空输入处理")
//    void testEmptyInput() {
//        AgentResult result = agent.run("");
//        assertNotNull(result, "空输入结果不应为null");
//    }
//
//    @Test
//    @Order(22)
//    @DisplayName("22. 特殊字符处理")
//    void testSpecialCharacterInput() {
//        AgentResult result = agent.run("测试特殊字符：\n\t");
//        assertNotNull(result, "特殊字符处理结果不应为null");
//    }
//
//    @Test
//    @Order(23)
//    @DisplayName("23. 步骤限制")
//    void testStepLimit() {
//        ChatMemory memory = MessageWindowChatMemory.builder().build();
//        ChatClient chatClient = ChatClient.builder(chatModel).build();
//
//        Agent limitedAgent = new com.agenthub.infrastructure.agents.core.DefaultReActAgent(
//            "limited-agent", "限制Agent", chatClient, memory, new ArrayList<>(), 2
//        );
//
//        AgentResult result = limitedAgent.run("执行任务");
//        assertNotNull(result, "步骤限制结果不应为null");
//        assertTrue(result.steps().size() <= 2, "步骤数应不超过限制");
//    }
//
//    @Test
//    @Order(24)
//    @DisplayName("24. 多Agent注册")
//    void testMultiAgentRegistration() {
//        String agent1 = "multi-agent-1";
//        String agent2 = "multi-agent-2";
//
//        ((DefaultA2AProtocol) a2aProtocol).registerAgent(agent1);
//        ((DefaultA2AProtocol) a2aProtocol).registerAgent(agent2);
//
//        assertEquals(A2AProtocol.AgentStatus.IDLE, a2aProtocol.getStatus(agent1));
//        assertEquals(A2AProtocol.AgentStatus.IDLE, a2aProtocol.getStatus(agent2));
//
//        ((DefaultA2AProtocol) a2aProtocol).updateStatus(agent1, A2AProtocol.AgentStatus.BUSY);
//        assertEquals(A2AProtocol.AgentStatus.BUSY, a2aProtocol.getStatus(agent1));
//        assertEquals(A2AProtocol.AgentStatus.IDLE, a2aProtocol.getStatus(agent2));
//    }
//
//    @Test
//    @Order(25)
//    @DisplayName("25. 通信总线测试")
//    void testCommunicationBus() {
//        AgentMessage message = AgentMessage.result("agent1", "agent2", "测试结果");
//        assertDoesNotThrow(() -> communicationBus.publish(message));
//    }
//}
