# 基于 Spring AI 框架实现 Harness Engineering AI Agent 技术方案

## 1. 概述

### 1.1 方案定位

本方案聚焦于如何利用 **Spring AI 1.0.0+** 框架的核心能力，构建一个面向软件工程全生命周期的 **Harness Engineering AI Agent**。该 Agent 具备自主规划、工具调用、多轮推理、人机协作等核心能力，能够完成代码生成/审查、CI/CD 流水线编排、测试自动化、故障诊断与修复等工程任务。

### 1.2 核心设计原则

| 原则 | 说明 |
|------|------|
| **Agent-First** | 以 Agent 为核心抽象，而非简单的 LLM 调用封装 |
| **Tool-Native** | 工具是 Agent 的手，所有工程能力通过 Tool 暴露 |
| **Memory-Aware** | 短期对话记忆 + 长期知识记忆，支撑复杂多轮任务 |
| **Observable** | 全链路可观测，每步推理和工具调用可追溯 |
| **Composable** | Agent 可组合编排，支持单 Agent 和 Multi-Agent 协作 |
| **Governable** | 安全护栏、权限控制、成本治理贯穿始终 |

### 1.3 技术栈全景

```
┌─────────────────────────────────────────────────────────┐
│                    应用层 (Application)                   │
│  REST API / WebSocket / CLI / Harness Platform Plugin    │
├─────────────────────────────────────────────────────────┤
│                  Agent 编排层 (Orchestration)             │
│  AgentExecutor / RePlanner / MultiAgentCoordinator       │
├─────────────────────────────────────────────────────────┤
│                  Agent 核心层 (Core)                      │
│  ChatClient / ToolCallback / Memory / Advisor            │
├─────────────────────────────────────────────────────────┤
│                  Spring AI 框架层                         │
│  spring-ai-core / spring-ai-model / spring-ai-vector     │
├─────────────────────────────────────────────────────────┤
│                  基础设施层 (Infrastructure)              │
│  LLM Provider / VectorStore / MCP Server / MessageQueue  │
└─────────────────────────────────────────────────────────┘
```

---

## 2. Spring AI 核心机制深度解析

### 2.1 ChatClient — 统一模型调用入口

Spring AI 的 `ChatClient` 是与 LLM 交互的核心抽象，采用 Builder 模式构建请求：

```java
// Spring AI ChatClient 基本用法
ChatClient chatClient = ChatClient.builder(chatModel)
    .defaultSystem("你是一个 Harness 工程助手，专注于 {domain} 领域")
    .defaultAdvisors(
        MessageChatMemoryAdvisor.builder(chatMemory).build(),
        SimpleLoggerAdvisor.builder().build()
    )
    .build();

// 执行请求
String response = chatClient.prompt()
    .user("分析这个 CI 流水线的失败原因: {log}")
    .system("以结构化 JSON 格式输出分析结果")
    .advisors(advisor -> advisor
        .param("chat_memory_conversation_id", conversationId)
        .param("chat_memory_response_size", 20)
    )
    .call()
    .content();
```

**关键设计点**：
- `ChatModel` 可替换：OpenAI、DeepSeek、Ollama、Azure OpenAI 等，通过 `spring.ai.openai.*` 或 `spring.ai.ollama.*` 配置切换
- `Advisor` 机制：请求/响应拦截器，用于注入记忆、日志、护栏等横切关注点
- 流式调用 `.stream()` 替代 `.call()` 实现逐 token 输出

### 2.2 ToolCallback — 工具调用机制

Spring AI 的 Function Calling 通过 `ToolCallback` 接口实现：

```java
// 方式一：@Tool 注解（Spring AI 1.0+ 推荐）
public class FileSystemTools {

    @Tool(description = "读取指定路径的文件内容，支持文本文件和代码文件")
    public String readFile(@ToolParam(description = "文件绝对路径") String path,
                           @ToolParam(description = "最大读取行数，默认2000") int maxLines) {
        return Files.readString(Path.of(path), StandardCharsets.UTF_8);
    }

    @Tool(description = "在指定路径写入文件内容，如果文件存在则覆盖")
    public WriteResult writeFile(@ToolParam(description = "文件绝对路径") String path,
                                 @ToolParam(description = "文件内容") String content) {
        Files.writeString(Path.of(path), content, StandardCharsets.UTF_8);
        return new WriteResult(path, true, "文件写入成功");
    }
}

// 方式二：FunctionCallback.builder()（兼容旧版）
FunctionCallback tool = FunctionCallback.builder()
    .description("执行 Git 操作")
    .inputType(GitOperationRequest.class)
    .function(request -> gitService.execute(request))
    .build();
```

**注册到 ChatClient**：

```java
ChatClient agentClient = ChatClient.builder(chatModel)
    .defaultSystem(SYSTEM_PROMPT)
    .defaultTools(fileSystemTools, gitTools, ciTools, searchTools)
    .build();
```

### 2.3 Advisor — 拦截器/增强器机制

Advisor 是 Spring AI 的 AOP 机制，可在请求前后插入逻辑：

```java
// 自定义护栏 Advisor
public class SafetyGuardAdvisor implements CallAdvisor {

    private final List<String> blockedPatterns;
    private final PromptTemplate guardTemplate;

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest request, CallAdvisorChain chain) {
        // 前置检查：检测用户输入是否包含危险指令
        String userInput = extractUserMessage(request);
        SafetyCheckResult check = safetyChecker.check(userInput);
        if (check.isBlocked()) {
            throw new SafetyViolationException(check.getReason());
        }

        // 执行请求
        AdvisedResponse response = chain.nextAroundCall(request);

        // 后置检查：检测模型输出是否合规
        String modelOutput = extractContent(response);
        if (safetyChecker.isSensitive(modelOutput)) {
            return redact(response);
        }

        return response;
    }
}
```

### 2.4 Memory — 对话记忆机制

```java
// 短期记忆：对话窗口内记忆
ChatMemory chatMemory = new InMemoryChatMemory();  // 开发/测试
ChatMemory chatMemory = new RedisChatMemory(redisTemplate);  // 生产

// 通过 Advisor 注入
MessageChatMemoryAdvisor memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory)
    .conversationIdExtractor(request -> extractConversationId(request))
    .maxMessages(50)  // 保留最近50条消息
    .build();

// 长期记忆：向量检索增强（RAG）
VectorStore vectorStore = new QdrantVectorStore(qdrantClient, embeddingModel);
QuestionAnswerAdvisor ragAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
    .searchType(SearchType.SIMILARITY)
    .topK(5)
    .userTextAdvise("基于以下知识库内容回答问题:\n{question_answer_context}")
    .build();
```

---

## 3. Harness Engineering Agent 架构设计

### 3.1 Agent 分层架构

```
┌──────────────────────────────────────────────────────────────┐
│                    HarnessEngineeringAgent                     │
│  (顶层 Agent，接收用户任务，规划并协调子 Agent 执行)           │
├──────────┬──────────┬──────────┬──────────┬─────────────────┤
│ CodeAgent │ TestAgent │ CIAgent  │ IaaCAgent │ DiagnoseAgent │
│ 代码生成   │ 测试自动化 │ CI/CD    │ IaC管理   │ 故障诊断      │
│ 代码审查   │ 测试用例   │ 流水线   │ 配置管理  │ 根因分析      │
│ 重构建议   │ 覆盖率    │ 部署编排 │ 合规检查  │ 修复建议      │
├──────────┴──────────┴──────────┴──────────┴─────────────────┤
│                    共享基础设施层                              │
│  ToolRegistry │ MemoryStore │ SafetyGuard │ Observability    │
└──────────────────────────────────────────────────────────────┘
```

### 3.2 核心 Agent 实现

#### 3.2.1 基础 Agent 抽象

```java
public abstract class AbstractEngineeringAgent {

    protected final ChatClient chatClient;
    protected final ChatMemory chatMemory;
    protected final List<ToolCallback> tools;
    protected final String agentName;
    protected final String systemPrompt;

    protected AbstractEngineeringAgent(ChatModel chatModel,
                                        ChatMemory chatMemory,
                                        List<ToolCallback> tools,
                                        String agentName,
                                        String systemPrompt) {
        this.agentName = agentName;
        this.systemPrompt = systemPrompt;
        this.chatMemory = chatMemory;
        this.tools = tools;

        // 构建 ChatClient，注入所有横切关注点
        this.chatClient = ChatClient.builder(chatModel)
            .defaultSystem(systemPrompt)
            .defaultTools(tools.toArray(new ToolCallback[0]))
            .defaultAdvisors(
                MessageChatMemoryAdvisor.builder(chatMemory).build(),
                SafetyGuardAdvisor.builder().build(),
                ObservabilityAdvisor.builder().build(),
                TokenUsageTracker.builder().build()
            )
            .build();
    }

    /**
     * 同步执行 Agent 任务
     */
    public AgentResponse execute(AgentRequest request) {
        return chatClient.prompt()
            .user(request.getUserMessage())
            .advisors(a -> a.param("chat_memory_conversation_id", request.getConversationId()))
            .call()
            .entity(AgentResponse.class);
    }

    /**
     * 流式执行 Agent 任务（适合长耗时工程任务）
     */
    public Flux<AgentChunk> executeStream(AgentRequest request) {
        return chatClient.prompt()
            .user(request.getUserMessage())
            .advisors(a -> a.param("chat_memory_conversation_id", request.getConversationId()))
            .stream()
            .content()
            .map(AgentChunk::new);
    }

    /**
     * 带自动重规划的执行（ReAct 循环）
     */
    public AgentResponse executeWithRePlan(AgentRequest request, int maxIterations) {
        AgentResponse response = execute(request);
        int iteration = 0;

        while (!response.isTaskComplete() && iteration < maxIterations) {
            // 自我评估：任务是否完成？是否需要调整策略？
            RePlanResult rePlan = evaluateAndRePlan(request, response);
            if (!rePlan.needsRePlan()) break;

            // 追加补充指令继续执行
            response = execute(request.withAdditionalMessage(rePlan.getAdditionalPrompt()));
            iteration++;
        }

        return response;
    }

    protected abstract RePlanResult evaluateAndRePlan(AgentRequest request, AgentResponse response);
}
```

#### 3.2.2 CodeAgent — 代码工程 Agent

```java
@Component
public class CodeAgent extends AbstractEngineeringAgent {

    public CodeAgent(ChatModel chatModel,
                     ChatMemory chatMemory,
                     FileSystemTools fileSystemTools,
                     GitTools gitTools,
                     CodeAnalysisTools codeAnalysisTools,
                     SearchTools searchTools) {
        super(chatModel, chatMemory,
              List.of(fileSystemTools, gitTools, codeAnalysisTools, searchTools),
              "code-agent",
              CODE_AGENT_SYSTEM_PROMPT);
    }

    private static final String CODE_AGENT_SYSTEM_PROMPT = """
        你是 Harness CodeAgent，一个专业的代码工程助手。

        ## 核心能力
        - 代码生成：根据需求描述生成高质量代码
        - 代码审查：识别代码缺陷、安全漏洞、性能问题
        - 代码重构：提供重构建议并执行重构
        - 代码搜索：在代码库中搜索相关代码

        ## 工作流程
        1. 理解用户需求，明确任务目标
        2. 搜索相关代码上下文（使用 searchCode 工具）
        3. 分析现有代码结构（使用 analyzeCode 工具）
        4. 生成/修改代码（使用 writeFile 工具）
        5. 验证修改结果（使用 readFile + analyzeCode 工具）

        ## 约束
        - 必须先读取现有代码再修改，禁止盲目覆盖
        - 修改代码前必须理解上下文，使用 searchCode 搜索相关引用
        - 每次修改后验证语法正确性
        - 遵循项目现有的代码风格和约定
        """;

    @Override
    protected RePlanResult evaluateAndRePlan(AgentRequest request, AgentResponse response) {
        // 代码任务的自我评估逻辑
        // 检查：代码是否编译通过？测试是否通过？是否有遗漏的修改？
        return chatClient.prompt()
            .system("评估代码任务完成度，判断是否需要继续修改")
            .user("原始任务: %s\n当前结果: %s".formatted(request.getUserMessage(), response.getContent()))
            .call()
            .entity(RePlanResult.class);
    }
}
```

#### 3.2.3 CIAgent — CI/CD 流水线 Agent

```java
@Component
public class CIAgent extends AbstractEngineeringAgent {

    public CIAgent(ChatModel chatModel,
                   ChatMemory chatMemory,
                   HarnessAPITools harnessAPITools,
                   YamlTools yamlTools,
                   FileSystemTools fileSystemTools) {
        super(chatModel, chatMemory,
              List.of(harnessAPITools, yamlTools, fileSystemTools),
              "ci-agent",
              CI_AGENT_SYSTEM_PROMPT);
    }

    private static final String CI_AGENT_SYSTEM_PROMPT = """
        你是 Harness CIAgent，专注于 CI/CD 流水线的智能编排。

        ## 核心能力
        - 流水线生成：根据项目特征自动生成 Harness Pipeline YAML
        - 流水线优化：分析执行数据，优化构建速度和资源使用
        - 故障诊断：分析流水线失败日志，定位根因
        - 部署编排：生成蓝绿部署、金丝雀发布等策略配置

        ## Harness Pipeline YAML 规范
        生成 YAML 时必须遵循 Harness Platform 的 Pipeline YAML 规范：
        - pipeline/stage/step 三级结构
        - 使用 <+account.xxx> 表达式引用变量
        - 使用 when 条件控制步骤执行
        - 使用 strategy 配置并行/矩阵执行

        ## 工作流程
        1. 分析项目结构（语言、框架、依赖管理）
        2. 生成基础 Pipeline YAML
        3. 根据需求添加测试/安全扫描/部署阶段
        4. 使用 Harness API 验证 YAML 语法
        5. 提交到 Harness Platform
        """;
}
```

#### 3.2.4 DiagnoseAgent — 故障诊断 Agent

```java
@Component
public class DiagnoseAgent extends AbstractEngineeringAgent {

    private static final String DIAGNOSE_AGENT_SYSTEM_PROMPT = """
        你是 Harness DiagnoseAgent，专注于生产环境故障诊断与修复。

        ## 诊断方法论
        采用结构化诊断流程：
        1. **症状收集**: 收集错误日志、指标异常、告警信息
        2. **假设生成**: 基于症状生成可能的根因假设
        3. **假设验证**: 逐个验证假设，收集证据
        4. **根因确认**: 确认根因并给出置信度
        5. **修复建议**: 提供修复方案和预防措施

        ## 工具使用策略
        - 先用 searchLogs 搜索错误日志
        - 再用 getMetrics 获取相关指标
        - 用 executeQuery 查询相关数据
        - 用 searchKnowledgeBase 搜索历史相似故障

        ## 输出格式
        必须以结构化格式输出诊断报告：
        - severity: P0/P1/P2/P3
        - rootCause: 根因描述
        - confidence: 置信度 0-1
        - evidence: 支撑证据列表
        - remediation: 修复步骤
        - prevention: 预防措施
        """;
}
```

### 3.3 Multi-Agent 协作编排

#### 3.3.1 Supervisor 模式

```java
@Component
public class HarnessEngineeringSupervisor {

    private final ChatClient supervisorClient;
    private final Map<String, AbstractEngineeringAgent> agents;

    public HarnessEngineeringSupervisor(ChatModel chatModel,
                                         ChatMemory chatMemory,
                                         CodeAgent codeAgent,
                                         CIAgent ciAgent,
                                         TestAgent testAgent,
                                         DiagnoseAgent diagnoseAgent) {
        this.agents = Map.of(
            "code", codeAgent,
            "ci", ciAgent,
            "test", testAgent,
            "diagnose", diagnoseAgent
        );

        // Supervisor 自身也是一个 ChatClient，负责路由决策
        this.supervisorClient = ChatClient.builder(chatModel)
            .defaultSystem(SUPERVISOR_SYSTEM_PROMPT)
            .defaultTools(new AgentRoutingTool(agents))
            .defaultAdvisors(
                MessageChatMemoryAdvisor.builder(chatMemory).build()
            )
            .build();
    }

    private static final String SUPERVISOR_SYSTEM_PROMPT = """
        你是 Harness Engineering AI Agent 的总协调器。

        ## 可调度的子 Agent
        - code: 代码生成、审查、重构
        - ci: CI/CD 流水线编排、部署
        - test: 测试生成、执行、覆盖率分析
        - diagnose: 故障诊断、根因分析

        ## 路由策略
        1. 分析用户请求，判断需要哪些子 Agent 参与
        2. 对于简单任务，直接路由到单个 Agent
        3. 对于复杂任务，拆分为子任务并编排多个 Agent
        4. 监控子 Agent 执行状态，必要时重新规划

        ## 任务拆分原则
        - 每个子任务应有明确的输入和预期输出
        - 子任务之间尽量解耦，可并行执行
        - 有依赖关系的子任务按顺序执行
        - 每个子任务完成后评估结果质量
        """;

    /**
     * 处理用户请求：路由 → 拆分 → 编排 → 汇总
     */
    public SupervisorResponse handle(UserRequest request) {
        // Step 1: 路由决策
        RoutingDecision routing = decideRouting(request);

        if (routing.isSingleAgent()) {
            // 单 Agent 直接执行
            AbstractEngineeringAgent agent = agents.get(routing.getAgentName());
            return SupervisorResponse.from(agent.execute(request.toAgentRequest()));
        }

        // Step 2: 多 Agent 编排
        return orchestrateMultiAgent(routing, request);
    }

    private SupervisorResponse orchestrateMultiAgent(RoutingDecision routing, UserRequest request) {
        List<TaskStep> steps = routing.getSteps();
        Map<String, AgentResponse> results = new ConcurrentHashMap<>();

        for (TaskStep step : steps) {
            if (step.getDependencies().isEmpty()) {
                // 无依赖，可并行执行
                CompletableFuture<AgentResponse> future = CompletableFuture.supplyAsync(() -> {
                    AbstractEngineeringAgent agent = agents.get(step.getAgentName());
                    return agent.execute(step.toAgentRequest(results));
                });
                results.put(step.getId(), future.join());
            } else {
                // 有依赖，等待前置步骤完成
                step.getDependencies().forEach(dep -> results.get(dep));  // 确保依赖已完成
                AbstractEngineeringAgent agent = agents.get(step.getAgentName());
                results.put(step.getId(), agent.execute(step.toAgentRequest(results)));
            }
        }

        // Step 3: 汇总结果
        return aggregateResults(results, request);
    }
}
```

#### 3.3.2 Agent 间通信协议

```java
/**
 * Agent 间通信消息
 */
public record AgentMessage(
    String fromAgent,       // 发送方 Agent 名称
    String toAgent,         // 接收方 Agent 名称
    MessageType type,       // REQUEST / RESPONSE / ERROR / PROGRESS
    String correlationId,   // 关联ID，用于追踪请求链路
    Map<String, Object> payload,  // 消息体
    long timestamp
) {}

/**
 * Agent 通信总线（基于 Spring ApplicationEvent 机制）
 */
@Component
public class AgentCommunicationBus {

    private final ApplicationEventPublisher eventPublisher;
    private final Map<String, CompletableFuture<AgentMessage>> pendingRequests = new ConcurrentHashMap<>();

    public CompletableFuture<AgentMessage> sendAsync(AgentMessage message) {
        CompletableFuture<AgentMessage> future = new CompletableFuture<>();
        pendingRequests.put(message.correlationId(), future);
        eventPublisher.publishEvent(message);
        return future.orTimeout(5, TimeUnit.MINUTES);
    }

    @EventListener
    public void handleResponse(AgentMessage response) {
        CompletableFuture<AgentMessage> future = pendingRequests.remove(response.correlationId());
        if (future != null) {
            future.complete(response);
        }
    }
}
```

---

## 4. 工具系统设计

### 4.1 工具分类与实现

#### 4.1.1 文件系统工具

```java
@Component
public class FileSystemTools {

    @Tool(description = "读取文件内容，支持指定行范围读取")
    public ReadResult readFile(
        @ToolParam(description = "文件绝对路径") String path,
        @ToolParam(description = "起始行号，从1开始，可选") Integer startLine,
        @ToolParam(description = "结束行号，可选") Integer endLine
    ) {
        Path filePath = Path.of(path);
        validatePath(filePath);  // 沙箱校验

        if (startLine != null && endLine != null) {
            // 按行范围读取
            List<String> lines = Files.readAllLines(filePath).subList(startLine - 1, endLine);
            return new ReadResult(path, String.join("\n", lines), lines.size());
        }
        return new ReadResult(path, Files.readString(filePath), -1);
    }

    @Tool(description = "在文件中执行精确字符串替换，用于代码修改")
    public EditResult editFile(
        @ToolParam(description = "文件绝对路径") String path,
        @ToolParam(description = "要替换的原始字符串") String oldString,
        @ToolParam(description = "替换后的新字符串") String newString
    ) {
        String content = Files.readString(Path.of(path));
        if (!content.contains(oldString)) {
            throw new ToolExecutionException("未找到要替换的字符串，请先读取文件确认内容");
        }
        String newContent = content.replace(oldString, newString);
        Files.writeString(Path.of(path), newContent);
        return new EditResult(path, true, "替换成功");
    }

    @Tool(description = "搜索匹配 glob 模式的文件路径")
    public List<String> searchFiles(
        @ToolParam(description = "搜索根目录") String baseDir,
        @ToolParam(description = "glob 模式，如 **/*.java") String pattern
    ) {
        return Files.walk(Path.of(baseDir))
            .filter(p -> p.toFile().isFile())
            .filter(p -> PathMatcher.matches(p, pattern))
            .map(Path::toString)
            .limit(100)
            .toList();
    }

    private void validatePath(Path path) {
        // 沙箱安全校验：禁止访问工作目录之外的文件
        Path sandboxRoot = Path.of(sandboxConfig.getWorkspaceRoot());
        if (!path.normalize().startsWith(sandboxRoot.normalize())) {
            throw new SecurityException("路径超出沙箱范围: " + path);
        }
    }
}
```

#### 4.1.2 Git 版本控制工具

```java
@Component
public class GitTools {

    private final GitOperations gitOps;  // 封装 JGit 或 CLI git

    @Tool(description = "执行 Git 操作，支持 clone/commit/push/pull/branch/checkout 等")
    public GitResult git(
        @ToolParam(description = "Git 仓库路径") String repoPath,
        @ToolParam(description = "Git 操作，如 clone/commit/push/pull") String operation,
        @ToolParam(description = "操作参数，JSON 格式") Map<String, String> params
    ) {
        return switch (operation) {
            case "clone"   -> gitOps.cloneRepo(params.get("url"), repoPath, params.get("branch"));
            case "commit"  -> gitOps.commit(repoPath, params.get("message"), params.get("files"));
            case "push"    -> gitOps.push(repoPath, params.get("remote"), params.get("branch"));
            case "pull"    -> gitOps.pull(repoPath, params.get("remote"), params.get("branch"));
            case "diff"    -> gitOps.diff(repoPath, params.get("base"), params.get("head"));
            case "log"     -> gitOps.log(repoPath, Integer.parseInt(params.getOrDefault("n", "10")));
            default -> throw new ToolExecutionException("不支持的 Git 操作: " + operation);
        };
    }

    @Tool(description = "获取 Git 仓库的变更摘要，用于代码审查")
    public DiffSummary getDiffSummary(
        @ToolParam(description = "仓库路径") String repoPath,
        @ToolParam(description = "基准分支") String baseBranch
    ) {
        return gitOps.getDiffSummary(repoPath, baseBranch);
    }
}
```

#### 4.1.3 Harness 平台 API 工具

```java
@Component
public class HarnessAPITools {

    private final HarnessPlatformClient harnessClient;

    @Tool(description = "调用 Harness Platform API，管理 Pipeline/Service/Environment/Connector 等资源")
    public HarnessAPIResult callHarnessAPI(
        @ToolParam(description = "API 路径，如 /api/v1/pipelines") String apiPath,
        @ToolParam(description = "HTTP 方法: GET/POST/PUT/DELETE") String method,
        @ToolParam(description = "请求体，JSON 格式，GET 时可为空") String body
    ) {
        return harnessClient.call(apiPath, method, body);
    }

    @Tool(description = "获取 Harness Pipeline 执行日志")
    public PipelineExecutionLog getPipelineLog(
        @ToolParam(description = "Pipeline 执行 ID") String executionId,
        @ToolParam(description = "失败步骤名称，可选，用于过滤") String failedStep
    ) {
        return harnessClient.getExecutionLog(executionId, failedStep);
    }

    @Tool(description = "触发 Harness Pipeline 执行")
    public PipelineExecution triggerPipeline(
        @ToolParam(description = "Pipeline 标识") String pipelineIdentifier,
        @ToolParam(description = "运行时变量，YAML 格式") String runtimeInputs
    ) {
        return harnessClient.triggerPipeline(pipelineIdentifier, runtimeInputs);
    }
}
```

#### 4.1.4 代码分析工具

```java
@Component
public class CodeAnalysisTools {

    @Tool(description = "分析代码结构，提取类/方法/依赖等信息")
    public CodeStructure analyzeCode(
        @ToolParam(description = "项目根目录") String projectPath,
        @ToolParam(description = "目标文件或目录路径") String targetPath
    ) {
        // 基于 Tree-sitter 或 LSP 进行代码结构分析
        return codeAnalyzer.analyze(projectPath, targetPath);
    }

    @Tool(description = "在代码库中搜索匹配查询的代码片段")
    public List<CodeSnippet> searchCode(
        @ToolParam(description = "搜索查询，支持自然语言描述") String query,
        @ToolParam(description = "搜索范围目录") String scopeDir,
        @ToolParam(description = "文件类型过滤，如 java,go,py") List<String> fileTypes
    ) {
        // 语义搜索：先 embedding，再向量检索
        // 回退到关键词搜索：ripgrep
        return codeSearchService.search(query, scopeDir, fileTypes);
    }

    @Tool(description = "执行代码质量检查，返回 lint/安全/复杂度问题")
    public List<CodeIssue> lintCode(
        @ToolParam(description = "要检查的文件路径") String filePath
    ) {
        return codeLinter.lint(filePath);
    }
}
```

#### 4.1.5 搜索与知识检索工具

```java
@Component
public class SearchTools {

    private final VectorStore vectorStore;
    private final EmbeddingModel embeddingModel;

    @Tool(description = "在知识库中搜索相关信息，用于 RAG 检索增强")
    public List<KnowledgeChunk> searchKnowledge(
        @ToolParam(description = "搜索查询") String query,
        @ToolParam(description = "返回结果数量，默认5") int topK
    ) {
        float[] embedding = embeddingModel.embed(query);
        return vectorStore.similaritySearch(
            SearchRequest.builder()
                .query(embedding)
                .topK(topK)
                .similarityThreshold(0.7)
                .build()
        ).stream().map(this::toKnowledgeChunk).toList();
    }

    @Tool(description = "搜索网页获取最新信息")
    public List<WebResult> searchWeb(
        @ToolParam(description = "搜索关键词") String query
    ) {
        return webSearchService.search(query);
    }
}
```

### 4.2 工具注册与发现

```java
@Configuration
public class ToolRegistryConfig {

    @Bean
    public ToolRegistry toolRegistry(
        FileSystemTools fileSystemTools,
        GitTools gitTools,
        HarnessAPITools harnessAPITools,
        CodeAnalysisTools codeAnalysisTools,
        SearchTools searchTools,
        YamlTools yamlTools,
        ShellTools shellTools
    ) {
        return ToolRegistry.builder()
            .register("filesystem", fileSystemTools)
            .register("git", gitTools)
            .register("harness-api", harnessAPITools)
            .register("code-analysis", codeAnalysisTools)
            .register("search", searchTools)
            .register("yaml", yamlTools)
            .register("shell", shellTools)
            .build();
    }
}

/**
 * 工具注册表 — 按需为 Agent 组装工具集
 */
public class ToolRegistry {

    private final Map<String, Object> toolGroups = new LinkedHashMap<>();

    public List<ToolCallback> getToolsForAgent(String agentType) {
        return switch (agentType) {
            case "code"     -> getToolCallbacks("filesystem", "git", "code-analysis", "search");
            case "ci"       -> getToolCallbacks("filesystem", "harness-api", "yaml", "search");
            case "test"     -> getToolCallbacks("filesystem", "shell", "code-analysis", "search");
            case "diagnose" -> getToolCallbacks("filesystem", "harness-api", "search", "shell");
            default         -> getToolCallbacks("filesystem", "search");
        };
    }
}
```

---

## 5. 记忆系统设计

### 5.1 三层记忆架构

```
┌─────────────────────────────────────────────────────┐
│              L1: 工作记忆 (Working Memory)            │
│  当前对话上下文，ChatMemory 管理                       │
│  生命周期: 单次对话                                   │
│  存储: InMemory / Redis                              │
├─────────────────────────────────────────────────────┤
│              L2: 情景记忆 (Episodic Memory)           │
│  历史任务执行记录，包含推理链和工具调用轨迹              │
│  生命周期: 跨对话持久化                                │
│  存储: PostgreSQL + VectorStore                      │
├─────────────────────────────────────────────────────┤
│              L3: 语义记忆 (Semantic Memory)            │
│  领域知识库，代码库知识，最佳实践                       │
│  生命周期: 长期持久化，定期更新                         │
│  存储: VectorStore (Qdrant/PgVector)                 │
└─────────────────────────────────────────────────────┘
```

### 5.2 记忆实现

```java
/**
 * 工作记忆 — 基于 Spring AI ChatMemory
 */
@Component
public class WorkingMemory {

    private final ChatMemory chatMemory;

    public WorkingMemory(ChatMemory chatMemory) {
        this.chatMemory = chatMemory;
    }

    public void add(String conversationId, Message message) {
        chatMemory.add(conversationId, List.of(message));
    }

    public List<Message> get(String conversationId, int lastN) {
        return chatMemory.get(conversationId, lastN);
    }
}

/**
 * 情景记忆 — 记录任务执行历史
 */
@Component
public class EpisodicMemory {

    private final TaskExecutionRepository taskRepo;
    private final VectorStore vectorStore;
    private final EmbeddingModel embeddingModel;

    /**
     * 存储任务执行记录
     */
    public void storeExecution(TaskExecution execution) {
        // 结构化存储
        taskRepo.save(execution);

        // 向量化存储（用于相似任务检索）
        String summary = execution.getSummary();
        float[] embedding = embeddingModel.embed(summary);
        Document doc = new Document(
            execution.getId(),
            summary,
            Map.of(
                "taskType", execution.getTaskType(),
                "agentName", execution.getAgentName(),
                "outcome", execution.getOutcome(),
                "timestamp", execution.getTimestamp().toString()
            )
        );
        doc.setEmbedding(embedding);
        vectorStore.add(List.of(doc));
    }

    /**
     * 检索相似历史任务（经验复用）
     */
    public List<TaskExecution> findSimilarTasks(String taskDescription, int topK) {
        return vectorStore.similaritySearch(
            SearchRequest.builder()
                .query(taskDescription)
                .topK(topK)
                .similarityThreshold(0.75)
                .build()
        ).stream()
         .map(doc -> taskRepo.findById(doc.getId()).orElse(null))
         .filter(Objects::nonNull)
         .toList();
    }
}

/**
 * 语义记忆 — 领域知识库（RAG）
 */
@Component
public class SemanticMemory {

    private final VectorStore knowledgeStore;
    private final EmbeddingModel embeddingModel;

    /**
     * 索引代码库知识
     */
    public void indexCodebase(String projectPath) {
        List<Document> documents = codebaseLoader.load(projectPath);
        // 按代码块切分
        List<Document> chunks = new TokenTextSplitter(500, 100, 5, true)
            .apply(documents);
        // 生成 embedding 并存储
        knowledgeStore.add(chunks);
    }

    /**
     * 索引文档知识（Harness 文档、最佳实践等）
     */
    public void indexDocuments(List<Resource> documentResources) {
        List<Document> documents = documentResources.stream()
            .flatMap(res -> new TikaDocumentReader(res).get().stream())
            .toList();
        List<Document> chunks = new TokenTextSplitter(800, 200, 5, true)
            .apply(documents);
        knowledgeStore.add(chunks);
    }

    /**
     * 检索相关知识
     */
    public List<KnowledgeEntry> retrieve(String query, int topK) {
        return knowledgeStore.similaritySearch(
            SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(0.7)
                .build()
        ).stream().map(this::toKnowledgeEntry).toList();
    }
}
```

### 5.3 记忆注入到 Agent

```java
// 通过 Advisor 链将三层记忆注入 Agent
ChatClient agentClient = ChatClient.builder(chatModel)
    .defaultSystem(systemPrompt)
    .defaultTools(tools)
    .defaultAdvisors(
        // L1: 工作记忆
        MessageChatMemoryAdvisor.builder(workingMemory.getChatMemory())
            .maxMessages(30)
            .build(),
        // L3: 语义记忆（RAG）
        QuestionAnswerAdvisor.builder(semanticMemory.getKnowledgeStore())
            .topK(5)
            .userTextAdvice("""
                参考以下知识库内容来辅助回答:
                {question_answer_context}
                """)
            .build(),
        // 可观测性
        ObservabilityAdvisor.builder().build()
    )
    .build();
```

---

## 6. 安全与治理

### 6.1 安全护栏 (Guardrails)

```java
/**
 * 输入安全护栏 — 检测注入攻击、敏感信息泄露
 */
@Component
public class InputSafetyAdvisor implements CallAdvisor {

    private final List<InputGuard> guards;

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest request, CallAdvisorChain chain) {
        String userInput = extractUserMessage(request);

        // 1. Prompt Injection 检测
        GuardResult injectionCheck = guards.stream()
            .filter(g -> g instanceof PromptInjectionGuard)
            .findFirst()
            .map(g -> g.check(userInput))
            .orElse(GuardResult.pass());

        if (injectionCheck.isBlocked()) {
            throw new SafetyViolationException("检测到潜在的 Prompt 注入: " + injectionCheck.getReason());
        }

        // 2. 敏感信息检测（PII、密钥、Token）
        GuardResult piiCheck = guards.stream()
            .filter(g -> g instanceof PIIDetectionGuard)
            .findFirst()
            .map(g -> g.check(userInput))
            .orElse(GuardResult.pass());

        if (piiCheck.isBlocked()) {
            throw new SafetyViolationException("输入包含敏感信息，请脱敏后重试");
        }

        return chain.nextAroundCall(request);
    }
}

/**
 * 输出安全护栏 — 检测模型幻觉、敏感信息泄露
 */
@Component
public class OutputSafetyAdvisor implements CallAdvisor {

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest request, CallAdvisorChain chain) {
        AdvisedResponse response = chain.nextAroundCall(request);
        String output = extractContent(response);

        // 1. 敏感信息脱敏
        String sanitized = sanitizeOutput(output);

        // 2. 幻觉检测（可选：通过二次 LLM 调用验证关键事实）
        // hallucinationChecker.check(sanitized, context);

        return replaceContent(response, sanitized);
    }

    private String sanitizeOutput(String output) {
        // 正则匹配并脱敏：API Key、密码、Token、IP 等
        return output
            .replaceAll("(?i)(api[_-]?key|password|token|secret)\\s*[:=]\\s*\\S+", "$1=***REDACTED***")
            .replaceAll("\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b", "***.***.***.***");
    }
}
```

### 6.2 工具权限控制

```java
/**
 * 工具执行权限校验
 */
@Component
public class ToolPermissionGuard {

    private final PermissionService permissionService;

    /**
     * 工具白名单 — 按 Agent 角色配置
     */
    private final Map<String, Set<String>> agentToolWhitelist = Map.of(
        "code-agent",     Set.of("readFile", "writeFile", "editFile", "searchFiles", "git", "analyzeCode", "searchCode", "lintCode", "searchKnowledge"),
        "ci-agent",       Set.of("readFile", "writeFile", "callHarnessAPI", "getPipelineLog", "triggerPipeline", "parseYaml", "generateYaml", "searchKnowledge"),
        "test-agent",     Set.of("readFile", "writeFile", "executeShell", "analyzeCode", "searchCode", "searchKnowledge"),
        "diagnose-agent", Set.of("readFile", "callHarnessAPI", "getPipelineLog", "searchLogs", "getMetrics", "executeShell", "searchKnowledge", "searchWeb")
    );

    /**
     * 危险操作二次确认
     */
    private final Set<String> dangerousOperations = Set.of(
        "writeFile", "executeShell", "triggerPipeline", "git:push"
    );

    public void checkPermission(String agentName, String toolName, ToolExecutionRequest request) {
        // 白名单校验
        Set<String> allowed = agentToolWhitelist.getOrDefault(agentName, Set.of());
        if (!allowed.contains(toolName)) {
            throw new PermissionDeniedException(
                "Agent [%s] 无权使用工具 [%s]".formatted(agentName, toolName));
        }

        // 危险操作确认
        if (dangerousOperations.contains(toolName)) {
            requireHumanApproval(request);
        }
    }
}
```

### 6.3 成本治理

```java
/**
 * Token 用量追踪与预算控制
 */
@Component
public class TokenUsageTracker implements CallAdvisor {

    private final TokenUsageRepository usageRepo;
    private final BudgetConfig budgetConfig;

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest request, CallAdvisorChain chain) {
        String agentName = extractAgentName(request);
        String userId = extractUserId(request);

        // 预算检查
        TokenUsage currentUsage = usageRepo.getCurrentUsage(userId, agentName);
        if (currentUsage.getTotalCost().compareTo(budgetConfig.getBudget(userId)) > 0) {
            throw new BudgetExceededException(
                "用户 [%s] 的 Agent [%s] 已超出预算限制".formatted(userId, agentName));
        }

        // 执行请求
        AdvisedResponse response = chain.nextAroundCall(request);

        // 记录用量
        Usage usage = response.response().getMetadata().getUsage();
        usageRepo.record(userId, agentName, usage);

        return response;
    }
}
```

---

## 7. 可观测性

### 7.1 执行追踪

```java
/**
 * Agent 执行追踪 — 记录完整的推理链和工具调用轨迹
 */
@Component
public class AgentExecutionTracer implements CallAdvisor {

    private final TraceRepository traceRepo;

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest request, CallAdvisorChain chain) {
        String traceId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();

        // 记录请求
        TraceEntry entry = TraceEntry.builder()
            .traceId(traceId)
            .agentName(extractAgentName(request))
            .input(extractUserMessage(request))
            .timestamp(Instant.now())
            .build();

        try {
            AdvisedResponse response = chain.nextAroundCall(request);

            // 记录成功响应
            entry.complete(
                extractContent(response),
                System.currentTimeMillis() - startTime,
                extractToolCalls(response),
                extractTokenUsage(response)
            );
            traceRepo.save(entry);

            return response;
        } catch (Exception e) {
            entry.fail(e.getMessage(), System.currentTimeMillis() - startTime);
            traceRepo.save(entry);
            throw e;
        }
    }
}
```

### 7.2 Micrometer 指标

```java
@Configuration
public class AgentMetricsConfig {

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> agentMetrics() {
        return registry -> {
            // Agent 执行耗时
            Timer.builder("agent.execution.duration")
                .description("Agent 执行耗时")
                .tag("agent", "name")
                .register(registry);

            // 工具调用次数
            Counter.builder("agent.tool.invocations")
                .description("工具调用次数")
                .tag("tool", "name")
                .register(registry);

            // Token 消耗
            Counter.builder("agent.token.usage")
                .description("Token 消耗量")
                .tag("model", "name")
                .register(registry);

            // 任务成功率
            Gauge.builder("agent.task.success.rate", taskRepo, TaskRepo::getSuccessRate)
                .description("任务成功率")
                .register(registry);
        };
    }
}
```

---

## 8. MCP (Model Context Protocol) 集成

### 8.1 MCP Server 暴露 Agent 能力

Spring AI 原生支持 MCP 协议，可将 Agent 工具暴露为 MCP Server：

```yaml
# application.yml
spring:
  ai:
    mcp:
      server:
        enabled: true
        name: harness-engineering-agent
        version: 1.0.0
        tools:
          - name: generate_pipeline
            description: 生成 Harness Pipeline YAML
          - name: diagnose_failure
            description: 诊断流水线失败原因
          - name: code_review
            description: 执行代码审查
```

```java
/**
 * MCP Server 端点 — 将 Agent 能力暴露给外部 MCP Client
 */
@RestController
@RequestMapping("/mcp")
public class MCPServerController {

    private final HarnessEngineeringSupervisor supervisor;

    @PostMapping("/tools/call")
    public MCPToolResult callTool(@RequestBody MCPToolCallRequest request) {
        return switch (request.getToolName()) {
            case "generate_pipeline" -> {
                AgentResponse response = supervisor.handle(
                    UserRequest.of("生成 Pipeline: " + request.getArguments()));
                yield MCPToolResult.success(response.getContent());
            }
            case "diagnose_failure" -> {
                AgentResponse response = supervisor.handle(
                    UserRequest.of("诊断失败: " + request.getArguments()));
                yield MCPToolResult.success(response.getContent());
            }
            default -> MCPToolResult.error("未知工具: " + request.getToolName());
        };
    }
}
```

### 8.2 MCP Client 消费外部工具

```java
/**
 * 通过 MCP 协议消费外部工具服务器的能力
 */
@Configuration
public class MCPClientConfig {

    @Bean
    public List<ToolCallback> mcpTools(MCPClient mcpClient) {
        // 连接外部 MCP Server（如 GitHub MCP、数据库 MCP 等）
        return mcpClient.connect("http://github-mcp-server:3000")
            .listTools()
            .stream()
            .map(this::toToolCallback)
            .toList();
    }
}
```

---

## 9. 项目结构与模块划分

```
harness-engineering-agent/
├── pom.xml
├── agent-core/                          # 核心模块
│   ├── src/main/java/
│   │   └── io/harness/agent/core/
│   │       ├── agent/
│   │       │   ├── AbstractEngineeringAgent.java
│   │       │   ├── AgentRequest.java
│   │       │   ├── AgentResponse.java
│   │       │   └── RePlanResult.java
│   │       ├── memory/
│   │       │   ├── WorkingMemory.java
│   │       │   ├── EpisodicMemory.java
│   │       │   └── SemanticMemory.java
│   │       ├── advisor/
│   │       │   ├── SafetyGuardAdvisor.java
│   │       │   ├── InputSafetyAdvisor.java
│   │       │   ├── OutputSafetyAdvisor.java
│   │       │   ├── TokenUsageTracker.java
│   │       │   └── AgentExecutionTracer.java
│   │       └── orchestration/
│   │           ├── HarnessEngineeringSupervisor.java
│   │           ├── RoutingDecision.java
│   │           └── AgentCommunicationBus.java
│   └── src/main/resources/
│       └── prompts/                     # 系统提示词模板
│           ├── code-agent.st
│           ├── ci-agent.st
│           ├── test-agent.st
│           └── diagnose-agent.st
├── agent-tools/                         # 工具模块
│   ├── src/main/java/
│   │   └── io/harness/agent/tools/
│   │       ├── filesystem/
│   │       │   └── FileSystemTools.java
│   │       ├── git/
│   │       │   └── GitTools.java
│   │       ├── harness/
│   │       │   └── HarnessAPITools.java
│   │       ├── code/
│   │       │   └── CodeAnalysisTools.java
│   │       ├── search/
│   │       │   └── SearchTools.java
│   │       ├── shell/
│   │       │   └── ShellTools.java
│   │       └── yaml/
│   │           └── YamlTools.java
│   └── src/main/resources/
│       └── sandbox-policy.yml           # 沙箱安全策略
├── agent-agents/                        # Agent 实现模块
│   ├── src/main/java/
│   │   └── io/harness/agent/agents/
│   │       ├── CodeAgent.java
│   │       ├── CIAgent.java
│   │       ├── TestAgent.java
│   │       ├── DiagnoseAgent.java
│   │       └── IaaCAgent.java
├── agent-server/                        # 服务入口模块
│   ├── src/main/java/
│   │   └── io/harness/agent/server/
│   │       ├── AgentApplication.java
│   │       ├── controller/
│   │       │   ├── AgentRestController.java
│   │       │   ├── AgentWebSocketHandler.java
│   │       │   └── MCPServerController.java
│   │       └── config/
│   │           ├── SpringAIConfig.java
│   │           ├── ToolRegistryConfig.java
│   │           ├── MemoryConfig.java
│   │           └── SecurityConfig.java
│   └── src/main/resources/
│       └── application.yml
└── agent-knowledge/                     # 知识库模块
    ├── src/main/java/
    │   └── io/harness/agent/knowledge/
    │       ├── CodebaseIndexer.java
    │       ├── DocumentIndexer.java
    │       └── KnowledgeIngestionJob.java
    └── src/main/resources/
        └── knowledge/                   # 领域知识文档
            ├── harness-pipeline-spec.md
            ├── harness-delegate-guide.md
            └── best-practices/
```

---

## 10. 关键依赖与配置

### 10.1 Maven 依赖

```xml
<properties>
    <spring-ai.version>1.0.0</spring-ai.version>
    <spring-boot.version>3.4.0</spring-boot.version>
</properties>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-bom</artifactId>
            <version>${spring-ai.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <!-- Spring AI Core -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-core</artifactId>
    </dependency>

    <!-- LLM Provider（按需选择） -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
    </dependency>
    <!-- 或 -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-ollama-spring-boot-starter</artifactId>
    </dependency>

    <!-- Vector Store -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-qdrant-store-spring-boot-starter</artifactId>
    </dependency>
    <!-- 或 PgVector -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-pgvector-store-spring-boot-starter</artifactId>
    </dependency>

    <!-- Embedding Model -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-transformers-spring-boot-starter</artifactId>
    </dependency>

    <!-- MCP -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-mcp-spring-boot-starter</artifactId>
    </dependency>

    <!-- 文档解析 -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-tika-document-reader</artifactId>
    </dependency>

    <!-- Redis（生产环境 ChatMemory） -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>

    <!-- 可观测性 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-registry-prometheus</artifactId>
    </dependency>
</dependencies>
```

### 10.2 应用配置

```yaml
# application.yml
spring:
  application:
    name: harness-engineering-agent

  ai:
    # LLM 配置（以 OpenAI 兼容接口为例）
    openai:
      api-key: ${LLM_API_KEY}
      base-url: ${LLM_BASE_URL:https://api.openai.com}
      chat:
        options:
          model: ${LLM_MODEL:gpt-4o}
          temperature: 0.1
          max-tokens: 4096
      embedding:
        options:
          model: text-embedding-3-small

    # Vector Store 配置
    vectorstore:
      qdrant:
        host: ${QDRANT_HOST:localhost}
        port: 6333
        collection-name: harness-agent-knowledge

    # MCP 配置
    mcp:
      server:
        enabled: true
        name: harness-engineering-agent
        version: 1.0.0

  # Redis 配置（生产环境 ChatMemory）
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: 6379

# Agent 自定义配置
harness:
  agent:
    # 沙箱配置
    sandbox:
      workspace-root: ${WORKSPACE_ROOT:/tmp/harness-agent-workspace}
      allowed-extensions: .java,.go,.py,.js,.ts,.yaml,.yml,.xml,.json,.md,.properties,.toml
      max-file-size: 10MB

    # 安全配置
    security:
      input-guard-enabled: true
      output-guard-enabled: true
      pii-detection-enabled: true
      prompt-injection-detection: true

    # 预算配置
    budget:
      default-daily-limit: 10.00    # USD
      per-agent-limits:
        code: 5.00
        ci: 3.00
        test: 2.00
        diagnose: 3.00

    # Agent 配置
    agents:
      code:
        max-iterations: 10
        model-override: null        # 可为特定 Agent 指定不同模型
      ci:
        max-iterations: 5
      test:
        max-iterations: 8
      diagnose:
        max-iterations: 15          # 诊断任务可能需要更多迭代

    # Harness Platform 集成
    platform:
      api-url: ${HARNESS_API_URL}
      api-token: ${HARNESS_API_TOKEN}
      account-id: ${HARNESS_ACCOUNT_ID}
      org-id: ${HARNESS_ORG_ID}
      project-id: ${HARNESS_PROJECT_ID}
```

---

## 11. API 接口设计

### 11.1 REST API

```java
@RestController
@RequestMapping("/api/v1/agent")
public class AgentRestController {

    private final HarnessEngineeringSupervisor supervisor;

    /**
     * 同步执行 Agent 任务
     */
    @PostMapping("/execute")
    public ResponseEntity<AgentResponse> execute(@RequestBody AgentRequest request) {
        AgentResponse response = supervisor.handle(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 流式执行 Agent 任务（SSE）
     */
    @PostMapping(value = "/execute/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<AgentChunk>> executeStream(@RequestBody AgentRequest request) {
        return supervisor.handleStream(request)
            .map(chunk -> ServerSentEvent.<AgentChunk>builder()
                .id(chunk.getId())
                .event(chunk.getEventType())
                .data(chunk)
                .build());
    }

    /**
     * 查询任务执行状态
     */
    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<TaskStatus> getTaskStatus(@PathVariable String taskId) {
        return ResponseEntity.ok(taskService.getStatus(taskId));
    }

    /**
     * 获取 Agent 执行历史
     */
    @GetMapping("/history")
    public ResponseEntity<Page<TaskExecution>> getHistory(
        @RequestParam String agentName,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(taskService.getHistory(agentName, page, size));
    }
}
```

### 11.2 WebSocket 接口（交互式对话）

```java
@Component
public class AgentWebSocketHandler extends TextWebSocketHandler {

    private final HarnessEngineeringSupervisor supervisor;

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        AgentRequest request = objectMapper.readValue(message.getPayload(), AgentRequest.class);

        // 流式响应
        supervisor.handleStream(request)
            .subscribe(
                chunk -> session.sendMessage(new TextMessage(toJson(chunk))),
                error -> session.sendMessage(new TextMessage(toJson(AgentChunk.error(error.getMessage())))),
                () -> session.sendMessage(new TextMessage(toJson(AgentChunk.complete())))
            );
    }
}
```

---

## 12. 部署架构

### 12.1 容器化部署

```dockerfile
# Dockerfile
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY agent-server/target/agent-server.jar app.jar

ENV JAVA_OPTS="-XX:+UseZGC -XX:MaxRAMPercentage=75.0"
ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

### 12.2 Kubernetes 部署

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: harness-engineering-agent
spec:
  replicas: 2
  selector:
    matchLabels:
      app: harness-engineering-agent
  template:
    metadata:
      labels:
        app: harness-engineering-agent
    spec:
      containers:
      - name: agent
        image: harness/engineering-agent:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: LLM_API_KEY
          valueFrom:
            secretKeyRef:
              name: agent-secrets
              key: llm-api-key
        - name: HARNESS_API_TOKEN
          valueFrom:
            secretKeyRef:
              name: agent-secrets
              key: harness-api-token
        resources:
          requests:
            memory: "2Gi"
            cpu: "1000m"
          limits:
            memory: "4Gi"
            cpu: "2000m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 10
---
apiVersion: v1
kind: Service
metadata:
  name: harness-engineering-agent
spec:
  selector:
    app: harness-engineering-agent
  ports:
  - port: 80
    targetPort: 8080
  type: ClusterIP
```

---

## 13. 实施路线图

### Phase 1: 核心框架搭建
- 初始化 Spring Boot + Spring AI 项目骨架
- 实现 `AbstractEngineeringAgent` 基础抽象
- 实现 `ChatClient` 配置与 `Advisor` 链
- 实现基础工具集：FileSystemTools、GitTools
- 实现工作记忆（InMemoryChatMemory）
- 验证单 Agent 端到端调用链路

### Phase 2: Agent 能力实现
- 实现 CodeAgent（代码生成/审查/搜索）
- 实现 CIAgent（Pipeline YAML 生成/诊断）
- 实现 TestAgent（测试用例生成/执行）
- 实现 DiagnoseAgent（故障诊断/根因分析）
- 实现 HarnessAPITools（Harness 平台集成）
- 实现 CodeAnalysisTools（代码结构分析）

### Phase 3: 编排与记忆增强
- 实现 HarnessEngineeringSupervisor（多 Agent 编排）
- 实现 Agent 间通信机制
- 实现情景记忆（EpisodicMemory + VectorStore）
- 实现语义记忆（RAG + 知识库索引）
- 实现 RePlan 自动重规划机制

### Phase 4: 安全与可观测性
- 实现输入/输出安全护栏
- 实现工具权限控制
- 实现沙箱隔离
- 实现执行追踪与审计日志
- 实现 Micrometer 指标 + Prometheus + Grafana
- 实现 Token 用量追踪与预算控制

### Phase 5: 生产化
- 实现 Redis ChatMemory（替代 InMemory）
- 实现 MCP Server/Client 集成
- 实现 REST API + WebSocket + SSE 接口
- 容器化 + Kubernetes 部署
- 压测与性能调优
- Harness Platform Plugin 集成

---

## 14. 关键技术决策与权衡

| 决策点 | 选择 | 理由 |
|--------|------|------|
| Agent 抽象方式 | 继承 AbstractEngineeringAgent | 统一 Advisor 链、工具注册、记忆注入，减少重复代码 |
| 多 Agent 编排 | Supervisor 模式 | 工程任务需要中心化协调，Supervisor 可做路由、拆分、质量把控 |
| 工具定义方式 | @Tool 注解 | Spring AI 1.0+ 原生支持，自动生成 JSON Schema，LLM 理解更准确 |
| 记忆存储 | Redis + Qdrant | Redis 做对话记忆（低延迟），Qdrant 做向量检索（高精度） |
| 流式输出 | SSE + WebSocket | SSE 适合单向推送，WebSocket 适合交互式对话 |
| 安全护栏 | Advisor 拦截器 | 与 Spring AI 原生机制一致，不侵入业务逻辑 |
| 模型选择 | 可配置切换 | 不同 Agent 可用不同模型（代码用强模型，分类用快模型） |
| MCP 集成 | 双向（Server + Client） | Server 暴露能力给外部，Client 消费外部工具 |

---

## 15. 总结

本方案基于 **Spring AI 1.0.0+** 框架，构建了一个完整的 **Harness Engineering AI Agent** 系统，核心特点：

1. **Spring AI 原生集成**：充分利用 ChatClient、ToolCallback、Advisor、Memory、VectorStore 等核心机制，不重复造轮子
2. **Agent-First 架构**：以 Agent 为核心抽象，支持单 Agent 独立执行和 Multi-Agent 协作编排
3. **工程能力工具化**：所有工程能力（代码操作、Git、Harness API、代码分析）通过 @Tool 暴露，LLM 自主决策调用
4. **三层记忆系统**：工作记忆 + 情景记忆 + 语义记忆，支撑复杂多轮工程任务
5. **安全治理内建**：输入/输出护栏、工具权限、沙箱隔离、预算控制，确保生产安全
6. **全链路可观测**：执行追踪、Micrometer 指标、审计日志，每步推理可追溯
7. **MCP 协议兼容**：双向 MCP 集成，可暴露能力给外部，也可消费外部工具
