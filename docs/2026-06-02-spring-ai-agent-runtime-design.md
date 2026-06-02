# ADR: Agent 运行时架构分析与 Spring AI 原生方案设计

## 1. 现状分析

### 1.1 当前架构

```
AbstractReActAgent (领域抽象)
├── AlibabaReActAgent (封装 ReactAgent)
│   └── 依赖: spring-ai-alibaba-graph-core
└── AgentScopeHarnessAgent (封装 HarnessAgent)
    └── 依赖: agentscope-harness + agentscope-core
```

### 1.2 存在的问题

#### 问题一：抽象泄漏（Abstraction Leak）

两个框架各有自己的抽象体系，项目被迫维护两套适配层：

| 概念 | Spring AI | Alibaba | AgentScope |
|------|-----------|---------|------------|
| 模型调用 | `ChatModel` | `ReactAgent` | `Model` |
| 工具 | `ToolCallback` | `ToolCallback` | `Tool` |
| 记忆 | `ChatMemory` | `MessageWindowChatMemory` | `Memory` |
| 检索 | `ContentRetriever` | 无原生支持 | `Knowledge` |
| 拦截 | `Advisor` | `Hook` + `Interceptor` | `Hook` |

**后果**：`AgentScopeSpringModelAdapter` 等适配类的存在，说明框架抽象与 Spring AI 不兼容。

#### 问题二：双重抽象（Double Abstraction）

```
用户请求 → AbstractReActAgent → ReactAgent/HarnessAgent → ChatModel/Model → 实际 LLM
```

四层调用链中，中间两层（AbstractReActAgent + 框架 Agent）功能重叠。`AbstractReActAgent` 本质上只是一个转发层，没有增加业务价值。

#### 问题三：Hook 模型不一致

- **Alibaba**：`Hook`（生命周期回调）+ `Interceptor`（调用链包装）两套机制
- **AgentScope**：统一的 `Hook.onEvent(HookEvent)` 事件驱动模型

**后果**：为每个框架编写专用 Hook（如 `ToolStrategyHook` vs `ToolStrategyInterceptor`），造成代码重复。

#### 问题四：策略集成碎片化

当前策略通过三种方式集成：
1. **构造时**：`ModelStrategy` → `ChatOptions`（工厂层）
2. **生命周期钩子**：`beforeInference()` / `afterInference()`（AbstractReActAgent）
3. **框架 Hook**：`ModelStrategyHook` / `ToolStrategyInterceptor`（基础设施层）

**后果**：同一个策略的逻辑分散在三层，难以追踪和测试。

#### 问题五：Spring AI 能力未充分利用

Spring AI 已提供：
- `ChatClient`：统一的模型调用入口
- `Advisor`：可组合的拦截链（Already have `MessageChatMemoryAdvisor`）
- `ToolCallback`：标准化的工具接口
- `ContentRetriever`：标准化的检索接口
- `ChatMemory`：标准化的记忆接口

**但项目没有用这些抽象来构建 Agent**，而是重新发明了轮子。

---

## 2. 基于 Spring AI 的 Agent 运行时设计

### 2.1 设计目标

1. **单一抽象**：只用 Spring AI 原生接口，消除框架特定代码
2. **策略即 Advisor**：所有策略统一为 `Advisor`，可组合、可测试
3. **零适配层**：不需要 `AgentScopeSpringModelAdapter` 等适配类
4. **渐进式迁移**：支持从现有架构平滑过渡

### 2.2 核心架构

```
SpringAiAgent (统一接口)
├── ChatClient (Spring AI 原生)
│   ├── ChatModel (模型调用)
│   ├── Advisors (策略链)
│   │   ├── ModelStrategyAdvisor      ← ModelStrategy
│   │   ├── GuardrailStrategyAdvisor  ← GuardrailStrategy
│   │   ├── ToolStrategyAdvisor       ← ToolStrategy
│   │   ├── RetrievalStrategyAdvisor  ← RetrievalStrategy
│   │   └── MessageChatMemoryAdvisor  ← ChatMemory
│   └── Tools (ToolCallback)
└── ChatMemory (Spring AI 原生)
```

### 2.3 核心接口设计

#### 2.3.1 统一 Agent 接口

```java
/**
 * 基于 Spring AI 的统一 Agent 接口。
 * 仅依赖 Spring AI 抽象，无框架特定代码。
 */
public interface SpringAiAgent {

    /** 同步调用。 */
    AgentMessage call(AgentMessage userMessage);

    /** 流式调用。 */
    Flux<AgentMessage> stream(AgentMessage userMessage);

    /** 获取 Agent 标识。 */
    String getName();

    /** 获取当前状态。 */
    AgentLifecycleState getState();
}
```

#### 2.3.2 Agent 配置

```java
@Data
@Builder
public class SpringAiAgentConfig {
    private String name;
    private String systemPrompt;
    private ChatModel chatModel;
    private ChatMemory chatMemory;
    private String conversationId;
    private List<ToolCallback> tools;
    private List<Advisor> advisors;
}
```

### 2.4 策略即 Advisor

#### 2.4.1 ModelStrategyAdvisor

```java
@Component
public class ModelStrategyAdvisor implements CallAroundAdvisor {

    private final ModelStrategy strategy;

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest request, CallAroundAdvisorChain chain) {
        // 推理前钩子
        strategy.beforeInference(extractContext(request), extractMessages(request));
        
        // 继续调用链
        AdvisedResponse response = chain.nextAroundCall(request);
        
        // 推理后钩子
        strategy.afterInference(extractContext(request), extractMessages(request), 
                               extractResponse(response));
        
        return response;
    }

    @Override
    public String getName() {
        return "ModelStrategyAdvisor";
    }

    @Override
    public int getOrder() {
        return 0; // 最先执行
    }
}
```

#### 2.4.2 GuardrailStrategyAdvisor

```java
@Component
public class GuardrailStrategyAdvisor implements CallAroundAdvisor {

    private final GuardrailStrategy strategy;

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest request, CallAroundAdvisorChain chain) {
        // 输入验证
        ValidationResult result = strategy.validateInput(extractUserText(request));
        if (!result.isValid()) {
            throw new ValidationException(String.join("; ", result.getViolations()));
        }
        
        // 继续调用链
        AdvisedResponse response = chain.nextAroundCall(request);
        
        // 输出验证
        ValidationResult outputResult = strategy.validateOutput(extractResponseText(response));
        if (!outputResult.isValid()) {
            throw new ValidationException(String.join("; ", outputResult.getViolations()));
        }
        
        return response;
    }

    @Override
    public String getName() {
        return "GuardrailStrategyAdvisor";
    }

    @Override
    public int getOrder() {
        return 100; // 在 ModelStrategyAdvisor 之后
    }
}
```

#### 2.4.3 ToolStrategyAdvisor

```java
@Component
public class ToolStrategyAdvisor implements CallAroundAdvisor {

    private final ToolStrategy strategy;

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest request, CallAroundAdvisorChain chain) {
        // 工具调用前钩子（如果请求中包含工具调用）
        if (hasToolCalls(request)) {
            strategy.beforeToolCall(extractContext(request), 
                                   extractToolName(request), 
                                   extractToolArgs(request));
        }
        
        // 继续调用链
        AdvisedResponse response = chain.nextAroundCall(request);
        
        // 工具调用后钩子
        if (hasToolResults(response)) {
            strategy.afterToolCall(extractContext(request),
                                  extractToolName(response),
                                  extractToolResult(response));
        }
        
        return response;
    }

    @Override
    public String getName() {
        return "ToolStrategyAdvisor";
    }

    @Override
    public int getOrder() {
        return 50; // 在 ModelStrategyAdvisor 之后，GuardrailStrategyAdvisor 之前
    }
}
```

#### 2.4.4 RetrievalStrategyAdvisor

```java
@Component
public class RetrievalStrategyAdvisor implements CallAroundAdvisor {

    private final RetrievalStrategy strategy;
    private final ContentRetriever contentRetriever;

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest request, CallAroundAdvisorChain chain) {
        // 检索前：查询改写
        String query = extractQuery(request);
        String rewrittenQuery = strategy.beforeRetrieval(extractContext(request), query);
        
        // 执行检索
        List<Document> documents = contentRetriever.retrieve(rewrittenQuery);
        
        // 检索后：结果过滤/重排
        List<?> filtered = strategy.afterRetrieval(extractContext(request), 
                                                   rewrittenQuery, documents);
        
        // 将检索结果注入请求
        AdvisedRequest enrichedRequest = injectDocuments(request, filtered);
        
        return chain.nextAroundCall(enrichedRequest);
    }

    @Override
    public String getName() {
        return "RetrievalStrategyAdvisor";
    }

    @Override
    public int getOrder() {
        return -10; // 在所有 Advisor 之前执行检索
    }
}
```

### 2.5 Agent 实现

```java
@Component
@RequiredArgsConstructor
public class DefaultSpringAiAgent implements SpringAiAgent {

    private final SpringAiAgentConfig config;
    private AgentLifecycleState state = AgentLifecycleState.CREATED;

    @Override
    public AgentMessage call(AgentMessage userMessage) {
        state = AgentLifecycleState.RUNNING;
        try {
            ChatClient chatClient = buildChatClient();
            String response = chatClient.prompt()
                    .system(config.getSystemPrompt())
                    .user(userMessage.getText())
                    .advisors advisors -> advisors
                            .addAll(config.getAdvisors())
                            .build()
                    .call()
                    .content();
            
            return AgentMessage.builder()
                    .messageType(AgentMessage.MessageType.ASSISTANT)
                    .text(response)
                    .build();
        } catch (Exception e) {
            state = AgentLifecycleState.ERROR;
            throw e;
        } finally {
            state = AgentLifecycleState.CREATED;
        }
    }

    @Override
    public Flux<AgentMessage> stream(AgentMessage userMessage) {
        state = AgentLifecycleState.RUNNING;
        ChatClient chatClient = buildChatClient();
        return chatClient.prompt()
                .system(config.getSystemPrompt())
                .user(userMessage.getText())
                .advisors advisors -> advisors
                        .addAll(config.getAdvisors())
                        .build()
                .stream()
                .content()
                .map(text -> AgentMessage.builder()
                        .messageType(AgentMessage.MessageType.ASSISTANT)
                        .text(text)
                        .build())
                .doFinally(signal -> state = AgentLifecycleState.CREATED);
    }

    private ChatClient buildChatClient() {
        return ChatClient.builder(config.getChatModel())
                .defaultSystem(config.getSystemPrompt())
                .defaultTools(config.getTools().toArray(new ToolCallback[0]))
                .defaultAdvisors(config.getAdvisors().toArray(new Advisor[0]))
                .build();
    }
}
```

### 2.6 与现有架构的对比

| 方面 | 现有架构 | Spring AI 原生架构 |
|------|----------|-------------------|
| **抽象层数** | 4 层（User → AbstractAgent → FrameworkAgent → ChatModel） | 2 层（User → SpringAiAgent → ChatModel） |
| **策略集成** | 3 种方式（构造时、生命周期钩子、框架 Hook） | 1 种方式（Advisor） |
| **框架依赖** | Alibaba + AgentScope + Spring AI | 仅 Spring AI |
| **适配层** | 需要（AgentScopeSpringModelAdapter） | 不需要 |
| **可测试性** | 需要 Mock 框架 Agent | 直接 Mock ChatModel |
| **可组合性** | 有限（框架特定） | 完全（Advisor 链） |

---

## 3. 迁移策略

### 3.1 渐进式迁移路径

```
Phase 1: 并行运行
├── 保留现有 AlibabaReActAgent 和 AgentScopeHarnessAgent
├── 新增 DefaultSpringAiAgent
└── 通过配置切换

Phase 2: 策略迁移
├── 将现有 Strategy 适配为 Advisor
├── 验证 Advisor 行为与现有 Hook 一致
└── 逐步移除框架特定 Hook

Phase 3: 框架统一
├── 移除 AlibabaReActAgent 和 AgentScopeHarnessAgent
├── 统一使用 DefaultSpringAiAgent
└── 移除所有适配层代码
```

### 3.2 兼容性保证

- `ReActAgentContext` 保持不变，作为策略配置的载体
- `AgentContextUseCase` 保持不变，负责构建上下文
- 策略模型（`ModelStrategy` 等）保持不变，作为充血模型

---

## 4. 风险与缓解

| 风险 | 缓解措施 |
|------|----------|
| Spring AI Advisor 性能 | 基准测试对比，必要时优化 Advisor 链 |
| 功能缺失（如 Alibaba 的 Graph 执行） | 保留可选的框架特定实现 |
| 团队学习成本 | 提供迁移指南和示例代码 |

---

## 5. 结论

当前双框架架构存在抽象泄漏、双重抽象、Hook 模型不一致等问题。基于 Spring AI 的 `ChatClient` + `Advisor` 模式可以提供更简洁、更统一的 Agent 运行时。

**建议**：采用渐进式迁移策略，先在并行模式下验证新架构，再逐步移除旧代码。
