# ADR: 企业级 Agent 运行时架构设计

## 1. 设计目标

构建一个基于 Spring AI 的**全能力 Agent 运行时**，覆盖：

- 核心执行循环（ReAct）
- 工具管理
- 记忆系统
- 知识检索（RAG）
- 多 Agent 协作
- 流式响应
- 安全护栏
- 可观测性
- 会话管理
- 错误处理与恢复
- 人机协作

---

## 2. 整体架构

```
┌─────────────────────────────────────────────────────────────────┐
│                        Agent Runtime                            │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐            │
│  │   Agent      │  │   Agent      │  │   Agent      │  ...     │
│  │  (Instance)  │  │  (Instance)  │  │  (Instance)  │          │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘            │
│         │                │                │                     │
│  ┌──────▼────────────────▼────────────────▼──────┐             │
│  │              AgentOrchestrator                 │             │
│  │  (生命周期管理 / 会话路由 / 负载均衡)           │             │
│  └──────────────────────┬────────────────────────┘             │
│                         │                                       │
│  ┌──────────────────────▼────────────────────────┐             │
│  │              AgentContext                      │             │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐      │             │
│  │  │ Strategy │ │ Strategy │ │ Strategy │ ...  │             │
│  │  └──────────┘ └──────────┘ └──────────┘      │             │
│  └───────────────────────────────────────────────┘             │
└─────────────────────────────────────────────────────────────────┘
         │                │                │
         ▼                ▼                ▼
┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│  ChatClient  │  │    Tools    │  │   Memory    │
│  (Spring AI) │  │ (ToolCallback│  │ (ChatMemory)│
└─────────────┘  └─────────────┘  └─────────────┘
         │                │                │
         ▼                ▼                ▼
┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│  ChatModel   │  │  ToolRegistry│  │ VectorStore │
│  (LLM)      │  │             │  │ (RAG)       │
└─────────────┘  └─────────────┘  └─────────────┘
```

---

## 3. 核心组件设计

### 3.1 Agent 接口

```java
/**
 * Agent 核心接口 — 定义 Agent 的完整能力。
 */
public interface Agent {

    /** Agent 标识。 */
    String getId();

    /** Agent 名称。 */
    String getName();

    /** Agent 描述。 */
    String getDescription();

    /** 同步执行。 */
    AgentResponse execute(AgentRequest request);

    /** 流式执行。 */
    Flux<AgentEvent> stream(AgentRequest request);

    /** 异步执行（返回 Future）。 */
    CompletableFuture<AgentResponse> submit(AgentRequest request);

    /** 中断执行。 */
    void interrupt();

    /** 获取当前状态。 */
    AgentState getState();

    /** 获取能力描述。 */
    AgentCapabilities getCapabilities();
}
```

### 3.2 AgentRequest / AgentResponse

```java
@Data
@Builder
public class AgentRequest {
    private String sessionId;
    private String userId;
    private String message;
    private Map<String, Object> metadata;
    private List<Attachment> attachments;
    private AgentRequestOptions options;
}

@Data
@Builder
public class AgentResponse {
    private String sessionId;
    private String messageId;
    private String content;
    private AgentMessageType messageType;
    private List<ToolCallResult> toolCalls;
    private TokenUsage tokenUsage;
    private Map<String, Object> metadata;
    private AgentResponseOptions options;
}

@Data
@Builder
public class AgentEvent {
    private String eventId;
    private AgentEventType type;      // REASONING, TOOL_CALL, TOOL_RESULT, RESPONSE, ERROR
    private String content;
    private Map<String, Object> data;
    private Instant timestamp;
}
```

### 3.3 AgentState

```java
public enum AgentState {
    CREATED,        // 已创建
    IDLE,           // 空闲
    THINKING,       // 推理中
    TOOL_CALLING,   // 工具调用中
    RETRIEVING,     // 检索中
    STREAMING,      // 流式输出中
    WAITING,        // 等待人工审批
    INTERRUPTED,    // 已中断
    ERROR           // 错误
}
```

---

## 4. 策略系统（Strategy System）

### 4.1 策略接口

```java
/**
 * 策略接口 — 所有策略的统一抽象。
 */
public interface AgentStrategy {

    /** 策略名称。 */
    String getName();

    /** 策略优先级（数字越小越先执行）。 */
    int getOrder();

    /** 是否启用。 */
    boolean isEnabled();
}
```

### 4.2 策略类型

```java
/**
 * 执行策略 — 控制 Agent 执行行为。
 */
public interface ExecutionStrategy extends AgentStrategy {

    /** 执行前钩子。 */
    default void beforeExecution(AgentContext context) {}

    /** 执行后钩子。 */
    default void afterExecution(AgentContext context, AgentResponse response) {}

    /** 执行异常钩子。 */
    default void onError(AgentContext context, Throwable error) {}
}

/**
 * 模型策略 — 控制 LLM 推理参数。
 */
public interface ModelStrategy extends AgentStrategy {

    /** 获取模型配置。 */
    ChatOptions getChatOptions();

    /** 推理前钩子。 */
    default void beforeInference(AgentContext context, List<AgentMessage> messages) {}

    /** 推理后钩子。 */
    default AgentMessage afterInference(AgentContext context,
                                        List<AgentMessage> messages,
                                        AgentMessage response) {
        return response;
    }
}

/**
 * 工具策略 — 控制工具调用行为。
 */
public interface ToolStrategy extends AgentStrategy {

    /** 工具调用前钩子。 */
    default void beforeToolCall(AgentContext context,
                                String toolName,
                                String arguments) {}

    /** 工具调用后钩子。 */
    default String afterToolCall(AgentContext context,
                                 String toolName,
                                 String result) {
        return result;
    }

    /** 工具调用失败钩子。 */
    default String onError(AgentContext context,
                           String toolName,
                           Throwable error) {
        throw new ToolExecutionException(toolName, error);
    }

    /** 是否允许调用该工具。 */
    default boolean isToolAllowed(AgentContext context, String toolName) {
        return true;
    }

    /** 获取重试策略。 */
    default RetryPolicy getRetryPolicy(String toolName) {
        return RetryPolicy.ofMaxAttempts(3);
    }

    /** 获取超时配置。 */
    default Duration getTimeout(String toolName) {
        return Duration.ofSeconds(30);
    }
}

/**
 * 检索策略 — 控制 RAG 检索行为。
 */
public interface RetrievalStrategy extends AgentStrategy {

    /** 检索前钩子（可用于查询改写）。 */
    default String beforeRetrieval(AgentContext context, String query) {
        return query;
    }

    /** 检索后钩子（可用于重排序/过滤）。 */
    default List<Document> afterRetrieval(AgentContext context,
                                          String query,
                                          List<Document> documents) {
        return documents;
    }

    /** 获取检索配置。 */
    RetrievalConfig getRetrievalConfig();
}

/**
 * 护栏策略 — 控制输入输出安全。
 */
public interface GuardrailStrategy extends AgentStrategy {

    /** 验证输入。 */
    ValidationResult validateInput(String input);

    /** 验证输出。 */
    ValidationResult validateOutput(String output);

    /** 输入过滤（可修改输入）。 */
    default String filterInput(String input) {
        return input;
    }

    /** 输出过滤（可修改输出）。 */
    default String filterOutput(String output) {
        return output;
    }
}

/**
 * 记忆策略 — 控制对话记忆行为。
 */
public interface MemoryStrategy extends AgentStrategy {

    /** 记忆窗口大小。 */
    int getWindowSize();

    /** 是否启用摘要。 */
    boolean isSummarizationEnabled();

    /** 消息压缩阈值。 */
    int getCompressionThreshold();

    /** 是否启用长期记忆。 */
    boolean isLongTermMemoryEnabled();
}

/**
 * 多 Agent 策略 — 控制团队协作行为。
 */
public interface TeamStrategy extends AgentStrategy {

    /** 团队协作模式。 */
    TeamMode getTeamMode();

    /** 最大并行 Agent 数。 */
    int getMaxParallelAgents();

    /** 任务分配策略。 */
    TaskAssignmentStrategy getTaskAssignmentStrategy();
}
```

### 4.3 策略注册与组合

```java
/**
 * 策略注册中心 — 管理所有策略的注册与查找。
 */
@Component
public class StrategyRegistry {

    private final Map<Class<? extends AgentStrategy>, List<AgentStrategy>> strategies;

    public <T extends AgentStrategy> List<T> getStrategies(Class<T> type) {
        return (List<T>) strategies.getOrDefault(type, List.of());
    }

    public <T extends AgentStrategy> T getStrategy(Class<T> type, String name) {
        return getStrategies(type).stream()
                .filter(s -> s.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new StrategyNotFoundException(type, name));
    }
}
```

---

## 5. 工具系统（Tool System）

### 5.1 工具注册中心

```java
/**
 * 工具注册中心 — 统一管理所有工具。
 */
@Component
public class ToolRegistry {

    private final Map<String, ToolDefinition> definitions = new ConcurrentHashMap<>();
    private final Map<String, ToolCallback> callbacks = new ConcurrentHashMap<>();
    private final Map<String, ToolStrategy> strategies = new ConcurrentHashMap<>();

    /** 注册工具。 */
    public void register(ToolDefinition definition, ToolCallback callback) {
        definitions.put(definition.name(), definition);
        callbacks.put(definition.name(), callback);
    }

    /** 注册工具（带策略）。 */
    public void register(ToolDefinition definition,
                         ToolCallback callback,
                         ToolStrategy strategy) {
        register(definition, callback);
        strategies.put(definition.name(), strategy);
    }

    /** 获取工具回调。 */
    public ToolCallback getCallback(String toolName) {
        return callbacks.get(toolName);
    }

    /** 获取工具策略。 */
    public ToolStrategy getStrategy(String toolName) {
        return strategies.getOrDefault(toolName, DefaultToolStrategy.INSTANCE);
    }

    /** 获取所有工具定义。 */
    public List<ToolDefinition> getAllDefinitions() {
        return List.copyOf(definitions.values());
    }
}
```

### 5.2 工具执行器

```java
/**
 * 工具执行器 — 带策略、重试、超时的工具调用。
 */
@Component
@RequiredArgsConstructor
public class ToolExecutor {

    private final ToolRegistry toolRegistry;
    private final MeterRegistry meterRegistry;

    /** 执行工具调用。 */
    public ToolCallResult execute(AgentContext context,
                                  String toolName,
                                  String arguments) {
        ToolStrategy strategy = toolRegistry.getStrategy(toolName);
        return executeWithRetry(context, toolName, arguments, strategy);
    }

    /** 带重试的执行。 */
    private ToolCallResult executeWithRetry(AgentContext context,
                                            String toolName,
                                            String arguments,
                                            ToolStrategy strategy) {
        RetryPolicy retryPolicy = strategy.getRetryPolicy(toolName);
        return RetryPolicy.execute(retryPolicy, () ->
                executeWithTimeout(context, toolName, arguments, strategy));
    }

    /** 带超时的执行。 */
    private ToolCallResult executeWithTimeout(AgentContext context,
                                              String toolName,
                                              String arguments,
                                              ToolStrategy strategy) {
        Duration timeout = strategy.getTimeout(toolName);
        return CompletableFuture.supplyAsync(() ->
                doExecute(context, toolName, arguments, strategy))
                .get(timeout.toMillis(), TimeUnit.MILLISECONDS);
    }

    /** 实际执行。 */
    private ToolCallResult doExecute(AgentContext context,
                                     String toolName,
                                     String arguments,
                                     ToolStrategy strategy) {
        strategy.beforeToolCall(context, toolName, arguments);
        try {
            ToolCallback callback = toolRegistry.getCallback(toolName);
            String result = callback.call(arguments);
            String processed = strategy.afterToolCall(context, toolName, result);
            meterRegistry.counter("tool.calls.success", "tool", toolName).increment();
            return ToolCallResult.success(toolName, processed);
        } catch (Exception e) {
            String fallback = strategy.onError(context, toolName, e);
            meterRegistry.counter("tool.calls.error", "tool", toolName).increment();
            return ToolCallResult.fallback(toolName, fallback);
        }
    }
}
```

---

## 6. 记忆系统（Memory System）

### 6.1 记忆接口

```java
/**
 * 记忆管理器 — 管理对话记忆。
 */
public interface MemoryManager {

    /** 添加消息。 */
    void addMessage(String conversationId, AgentMessage message);

    /** 获取消息历史。 */
    List<AgentMessage> getMessages(String conversationId);

    /** 获取消息窗口。 */
    List<AgentMessage> getWindow(String conversationId, int windowSize);

    /** 压缩消息历史。 */
    List<AgentMessage> compress(String conversationId);

    /** 清空记忆。 */
    void clear(String conversationId);

    /** 导出记忆。 */
    MemorySnapshot exportSnapshot(String conversationId);

    /** 导入记忆。 */
    void importSnapshot(String conversationId, MemorySnapshot snapshot);
}
```

### 6.2 记忆实现

```java
/**
 * 分层记忆管理器 — 支持短期、工作、长期记忆。
 */
@Component
@RequiredArgsConstructor
public class HierarchicalMemoryManager implements MemoryManager {

    private final ChatMemory chatMemory;           // Spring AI 短期记忆
    private final WorkingMemory workingMemory;      // 工作记忆（当前任务）
    private final LongTermMemory longTermMemory;    // 长期记忆（向量存储）

    @Override
    public void addMessage(String conversationId, AgentMessage message) {
        chatMemory.add(conversationId, toSpringMessage(message));
        workingMemory.add(conversationId, message);
    }

    @Override
    public List<AgentMessage> getMessages(String conversationId) {
        List<AgentMessage> messages = new ArrayList<>();
        messages.addAll(longTermMemory.retrieve(conversationId, "相关上下文"));
        messages.addAll(toAgentMessages(chatMemory.get(conversationId)));
        messages.addAll(workingMemory.get(conversationId));
        return messages;
    }
}
```

### 6.3 记忆策略配置

```java
@Data
@Builder
public class MemoryConfig {

    /** 短期记忆窗口大小。 */
    @Builder.Default
    private int shortTermWindowSize = 20;

    /** 工作记忆容量。 */
    @Builder.Default
    private int workingMemoryCapacity = 50;

    /** 长期记忆检索数量。 */
    @Builder.Default
    private int longTermRetrievalCount = 10;

    /** 压缩阈值（消息数超过此值触发压缩）。 */
    @Builder.Default
    private int compressionThreshold = 100;

    /** 是否启用摘要。 */
    @Builder.Default
    private boolean summarizationEnabled = true;

    /** 是否启用长期记忆。 */
    @Builder.Default
    private boolean longTermMemoryEnabled = true;
}
```

---

## 7. 知识检索系统（RAG System）

### 7.1 检索接口

```java
/**
 * 知识检索器 — 统一的检索接口。
 */
public interface KnowledgeRetriever {

    /** 检索相关文档。 */
    List<Document> retrieve(String query, RetrievalConfig config);

    /** 流式检索。 */
    Flux<Document> retrieveStream(String query, RetrievalConfig config);

    /** 带过滤条件的检索。 */
    List<Document> retrieve(String query,
                            RetrievalConfig config,
                            FilterExpression filter);
}

/**
 * 检索配置。
 */
@Data
@Builder
public class RetrievalConfig {

    /** 检索类型。 */
    @Builder.Default
    private RetrievalType type = RetrievalType.HYBRID;

    /** 返回数量。 */
    @Builder.Default
    private int topK = 10;

    /** 分数阈值。 */
    @Builder.Default
    private double scoreThreshold = 0.75;

    /** 向量权重。 */
    @Builder.Default
    private double vectorWeight = 0.7;

    /** 关键词权重。 */
    @Builder.Default
    private double keywordWeight = 0.3;

    /** 是否启用重排序。 */
    @Builder.Default
    private boolean rerankEnabled = false;

    /** 重排序模型。 */
    private String rerankModel;

    /** 是否启用查询改写。 */
    @Builder.Default
    private boolean queryRewriteEnabled = false;

    public enum RetrievalType {
        VECTOR, KEYWORD, HYBRID
    }
}
```

### 7.2 检索管道

```java
/**
 * 检索管道 — 可组合的检索处理链。
 */
@Component
@RequiredArgsConstructor
public class RetrievalPipeline {

    private final KnowledgeRetriever retriever;
    private final QueryRewriter queryRewriter;
    private final DocumentReranker reranker;
    private final DocumentFilter filter;

    /** 执行检索管道。 */
    public List<Document> execute(String query, RetrievalConfig config) {
        // 1. 查询改写
        String rewrittenQuery = config.isQueryRewriteEnabled()
                ? queryRewriter.rewrite(query)
                : query;

        // 2. 检索
        List<Document> documents = retriever.retrieve(rewrittenQuery, config);

        // 3. 过滤
        documents = filter.filter(documents, config.getScoreThreshold());

        // 4. 重排序
        if (config.isRerankEnabled()) {
            documents = reranker.rerank(rewrittenQuery, documents, config.getRerankModel());
        }

        return documents;
    }
}
```

---

## 8. 多 Agent 协作系统（Multi-Agent System）

### 8.1 团队模式

```java
public enum TeamMode {
    SEQUENTIAL,     // 顺序执行
    PARALLEL,       // 并行执行
    ROUTING,        // 路由分发
    SUPERVISOR,     // 监督者模式
    HIERARCHICAL    // 层级模式
}
```

### 8.2 团队接口

```java
/**
 * Agent 团队 — 管理多个 Agent 的协作。
 */
public interface AgentTeam {

    /** 团队标识。 */
    String getTeamId();

    /** 团队名称。 */
    String getTeamName();

    /** 协作模式。 */
    TeamMode getMode();

    /** 添加成员。 */
    void addMember(Agent agent);

    /** 移除成员。 */
    void removeMember(String agentId);

    /** 执行任务。 */
    TeamResponse execute(TeamRequest request);

    /** 流式执行。 */
    Flux<TeamEvent> stream(TeamRequest request);

    /** 获取所有成员。 */
    List<Agent> getMembers();

    /** 获取领导者。 */
    Agent getLeader();
}
```

### 8.3 任务分配器

```java
/**
 * 任务分配器 — 根据策略分配任务给 Agent。
 */
@Component
public class TaskAssigner {

    /** 分配任务。 */
    public Agent assignTask(TeamRequest request,
                            List<Agent> members,
                            TeamStrategy strategy) {
        return switch (strategy.getTeamMode()) {
            case ROUTING -> routeByCapability(request, members);
            case SUPERVISOR -> delegateToSupervisor(request, members);
            case HIERARCHICAL -> delegateHierarchically(request, members);
            default -> selectFirst(members);
        };
    }

    /** 根据能力路由。 */
    private Agent routeByCapability(TeamRequest request, List<Agent> members) {
        return members.stream()
                .filter(agent -> matchesCapability(request, agent))
                .findFirst()
                .orElseThrow(() -> new NoSuitableAgentException(request));
    }
}
```

---

## 9. 可观测性系统（Observability）

### 9.1 追踪接口

```java
/**
 * Agent 追踪器 — 记录 Agent 执行轨迹。
 */
public interface AgentTracer {

    /** 开始追踪。 */
    TraceContext startTrace(AgentRequest request);

    /** 记录推理步骤。 */
    void traceReasoning(TraceContext context, String reasoning);

    /** 记录工具调用。 */
    void traceToolCall(TraceContext context, String toolName, String args, String result);

    /** 记录检索。 */
    void traceRetrieval(TraceContext context, String query, int resultCount);

    /** 结束追踪。 */
    TraceResult endTrace(TraceContext context, AgentResponse response);

    /** 记录错误。 */
    void traceError(TraceContext context, Throwable error);
}
```

### 9.2 指标收集

```java
/**
 * Agent 指标收集器。
 */
@Component
@RequiredArgsConstructor
public class AgentMetrics {

    private final MeterRegistry meterRegistry;

    /** 记录执行指标。 */
    public void recordExecution(String agentId, Duration duration, boolean success) {
        Timer.builder("agent.execution.time")
                .tag("agent", agentId)
                .tag("success", String.valueOf(success))
                .register(meterRegistry)
                .record(duration);
    }

    /** 记录 Token 使用量。 */
    public void recordTokenUsage(String agentId, int inputTokens, int outputTokens) {
        meterRegistry.counter("agent.tokens.input", "agent", agentId)
                .increment(inputTokens);
        meterRegistry.counter("agent.tokens.output", "agent", agentId)
                .increment(outputTokens);
    }

    /** 记录工具调用。 */
    public void recordToolCall(String toolName, Duration duration, boolean success) {
        Timer.builder("agent.tool.call.time")
                .tag("tool", toolName)
                .tag("success", String.valueOf(success))
                .register(meterRegistry)
                .record(duration);
    }
}
```

---

## 10. 会话管理（Session Management）

### 10.1 会话接口

```java
/**
 * 会话管理器 — 管理 Agent 会话。
 */
public interface SessionManager {

    /** 创建会话。 */
    Session createSession(String agentId, String userId);

    /** 获取会话。 */
    Session getSession(String sessionId);

    /** 保存会话。 */
    void saveSession(Session session);

    /** 关闭会话。 */
    void closeSession(String sessionId);

    /** 获取会话历史。 */
    List<AgentMessage> getSessionHistory(String sessionId);

    /** 创建检查点。 */
    Checkpoint createCheckpoint(String sessionId);

    /** 恢复检查点。 */
    void restoreCheckpoint(String sessionId, Checkpoint checkpoint);
}

/**
 * 会话对象。
 */
@Data
@Builder
public class Session {
    private String sessionId;
    private String agentId;
    private String userId;
    private AgentState state;
    private Instant createdAt;
    private Instant lastActiveAt;
    private Map<String, Object> attributes;
    private List<Checkpoint> checkpoints;
}

/**
 * 检查点 — 用于会话恢复。
 */
@Data
@Builder
public class Checkpoint {
    private String checkpointId;
    private String sessionId;
    private AgentState state;
    private List<AgentMessage> messages;
    private Map<String, Object> context;
    private Instant createdAt;
}
```

---

## 11. 错误处理与恢复（Error Handling）

### 11.1 错误处理策略

```java
/**
 * 错误处理策略。
 */
public interface ErrorHandlingStrategy {

    /** 处理错误。 */
    AgentResponse handleError(AgentRequest request, Throwable error);

    /** 是否可恢复。 */
    boolean isRecoverable(Throwable error);

    /** 获取回退响应。 */
    AgentResponse getFallbackResponse(AgentRequest request, Throwable error);
}

/**
 * 默认错误处理策略。
 */
@Component
public class DefaultErrorHandlingStrategy implements ErrorHandlingStrategy {

    @Override
    public AgentResponse handleError(AgentRequest request, Throwable error) {
        if (isRecoverable(error)) {
            return getFallbackResponse(request, error);
        }
        throw new AgentExecutionException("Agent 执行失败", error);
    }

    @Override
    public boolean isRecoverable(Throwable error) {
        return error instanceof TimeoutException
            || error instanceof RateLimitException;
    }

    @Override
    public AgentResponse getFallbackResponse(AgentRequest request, Throwable error) {
        return AgentResponse.builder()
                .content("抱歉，当前无法处理您的请求，请稍后重试。")
                .messageType(AgentMessageType.ERROR)
                .metadata(Map.of("error", error.getMessage()))
                .build();
    }
}
```

### 11.2 重试机制

```java
/**
 * 重试配置。
 */
@Data
@Builder
public class RetryConfig {

    /** 最大重试次数。 */
    @Builder.Default
    private int maxAttempts = 3;

    /** 重试间隔。 */
    @Builder.Default
    private Duration backoff = Duration.ofMillis(500);

    /** 退避策略。 */
    @Builder.Default
    private BackoffStrategy backoffStrategy = BackoffStrategy.EXPONENTIAL;

    /** 可重试的异常类型。 */
    @Builder.Default
    private List<Class<? extends Throwable>> retryableExceptions = List.of(
            TimeoutException.class,
            RateLimitException.class,
            NetworkException.class
    );

    public enum BackoffStrategy {
        FIXED, LINEAR, EXPONENTIAL
    }
}
```

---

## 12. 人机协作（Human-in-the-Loop）

### 12.1 审批接口

```java
/**
 * 人机协作管理器 — 处理需要人工审批的场景。
 */
public interface HumanInTheLoopManager {

    /** 请求审批。 */
    ApprovalResult requestApproval(AgentContext context,
                                   String action,
                                   String description);

    /** 批准。 */
    void approve(String requestId, String userId, String comment);

    /** 拒绝。 */
    void reject(String requestId, String userId, String reason);

    /** 获取待审批列表。 */
    List<PendingApproval> getPendingApprovals(String userId);
}

/**
 * 审批请求。
 */
@Data
@Builder
public class PendingApproval {
    private String requestId;
    private String agentId;
    private String action;
    private String description;
    private Instant requestedAt;
    private ApprovalStatus status;

    public enum ApprovalStatus {
        PENDING, APPROVED, REJECTED, EXPIRED
    }
}
```

---

## 13. 完整的 Agent 实现

```java
/**
 * 全能力 Agent 实现。
 */
@Component
@RequiredArgsConstructor
public class FullCapabilityAgent implements Agent {

    private final ChatClient.Builder chatClientBuilder;
    private final ToolRegistry toolRegistry;
    private final ToolExecutor toolExecutor;
    private final MemoryManager memoryManager;
    private final KnowledgeRetriever knowledgeRetriever;
    private final RetrievalPipeline retrievalPipeline;
    private final StrategyRegistry strategyRegistry;
    private final AgentTracer tracer;
    private final AgentMetrics metrics;
    private final SessionManager sessionManager;
    private final ErrorHandlingStrategy errorHandling;
    private final HumanInTheLoopManager humanInTheLoop;

    private AgentState state = AgentState.CREATED;

    @Override
    public AgentResponse execute(AgentRequest request) {
        String sessionId = request.getSessionId();
        TraceContext trace = tracer.startTrace(request);
        Instant start = Instant.now();

        try {
            // 1. 恢复会话
            state = AgentState.THINKING;
            Session session = sessionManager.getSession(sessionId);

            // 2. 获取记忆
            List<AgentMessage> history = memoryManager.getMessages(sessionId);

            // 3. 检索知识
            state = AgentState.RETRIEVING;
            List<Document> documents = retrievalPipeline.execute(
                    request.getMessage(),
                    getRetrievalConfig());

            // 4. 执行推理（带策略）
            state = AgentState.THINKING;
            AgentResponse response = executeWithStrategies(request, history, documents);

            // 5. 保存记忆
            memoryManager.addMessage(sessionId,
                    AgentMessage.user(request.getMessage()));
            memoryManager.addMessage(sessionId,
                    AgentMessage.assistant(response.getContent()));

            // 6. 记录指标
            metrics.recordExecution(getId(),
                    Duration.between(start, Instant.now()), true);
            tracer.endTrace(trace, response);

            return response;
        } catch (Exception e) {
            metrics.recordExecution(getId(),
                    Duration.between(start, Instant.now()), false);
            tracer.traceError(trace, e);
            return errorHandling.handleError(request, e);
        } finally {
            state = AgentState.IDLE;
        }
    }

    /** 带策略执行推理。 */
    private AgentResponse executeWithStrategies(AgentRequest request,
                                                List<AgentMessage> history,
                                                List<Document> documents) {
        // 构建 ChatClient
        ChatClient chatClient = buildChatClient();

        // 执行推理
        String response = chatClient.prompt()
                .system(getSystemPrompt())
                .user(request.getMessage())
                .advisors(buildAdvisors())
                .call()
                .content();

        return AgentResponse.builder()
                .sessionId(request.getSessionId())
                .content(response)
                .messageType(AgentMessageType.ASSISTANT)
                .build();
    }

    /** 构建 ChatClient。 */
    private ChatClient buildChatClient() {
        return chatClientBuilder
                .defaultTools(toolRegistry.getAllDefinitions().toArray(new ToolDefinition[0]))
                .build();
    }

    /** 构建 Advisor 链。 */
    private List<Advisor> buildAdvisors() {
        return strategyRegistry.getStrategies(ExecutionStrategy.class).stream()
                .map(strategy -> (Advisor) new ExecutionStrategyAdvisor(strategy))
                .toList();
    }
}
```

---

## 14. 与现有架构的对比

| 能力 | 现有架构 | 新架构 |
|------|----------|--------|
| **核心执行** | 双框架抽象 | 统一 Agent 接口 |
| **工具管理** | 分散在 Factory | ToolRegistry + ToolExecutor |
| **记忆系统** | ChatMemory 单一 | 分层记忆（短期/工作/长期） |
| **知识检索** | AgentScopeKnowledge | RetrievalPipeline |
| **多 Agent** | AbstractTeamAgent | AgentTeam + TaskAssigner |
| **流式响应** | 框架特定 | Flux\<AgentEvent\> |
| **安全护栏** | GuardrailStrategy | GuardrailStrategy + 过滤 |
| **可观测性** | 分散的日志 | AgentTracer + AgentMetrics |
| **会话管理** | 有限 | SessionManager + Checkpoint |
| **错误处理** | 基础 | ErrorHandlingStrategy + Retry |
| **人机协作** | 无 | HumanInTheLoopManager |

---

## 15. 实施路线图

### Phase 1: 核心能力（2周）
- [ ] Agent 接口与实现
- [ ] AgentRequest/AgentResponse
- [ ] ToolRegistry + ToolExecutor
- [ ] 基础 MemoryManager

### Phase 2: 策略系统（2周）
- [ ] AgentStrategy 接口体系
- [ ] StrategyRegistry
- [ ] ModelStrategy 实现
- [ ] ToolStrategy 实现
- [ ] GuardrailStrategy 实现

### Phase 3: 高级能力（3周）
- [ ] RetrievalPipeline
- [ ] 分层记忆系统
- [ ] SessionManager + Checkpoint
- [ ] AgentTracer + AgentMetrics

### Phase 4: 多 Agent（2周）
- [ ] AgentTeam 接口
- [ ] TaskAssigner
- [ ] 各种 TeamMode 实现

### Phase 5: 人机协作（1周）
- [ ] HumanInTheLoopManager
- [ ] 审批工作流

### Phase 6: 迁移与优化（2周）
- [ ] 从现有架构迁移
- [ ] 性能优化
- [ ] 文档与示例
