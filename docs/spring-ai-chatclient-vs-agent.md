# Spring-AI ChatClient 与 AI Agent 的区别及 Agent 实现指南

## 目录

- [1. 概述](#1-概述)
- [2. ChatClient 详解](#2-chatclient-详解)
  - [2.1 ChatClient 是什么](#21-chatclient-是什么)
  - [2.2 ChatClient 核心架构](#22-chatclient-核心架构)
  - [2.3 ChatClient 的 Advisor 机制](#23-chatclient-的-advisor-机制)
  - [2.4 ChatClient 使用示例](#24-chatclient-使用示例)
  - [2.5 ChatClient 的局限性](#25-chatclient-的局限性)
- [3. AI Agent 详解](#3-ai-agent-详解)
  - [3.1 AI Agent 是什么](#31-ai-agent-是什么)
  - [3.2 Agent 核心架构](#32-agent-核心架构)
  - [3.3 Agent 的关键能力](#33-agent-的关键能力)
  - [3.4 Agent 的推理模式](#34-agent-的推理模式)
- [4. ChatClient 与 Agent 的核心区别](#4-chatclient-与-agent-的核心区别)
  - [4.1 对比总览](#41-对比总览)
  - [4.2 架构层级差异](#42-架构层级差异)
  - [4.3 交互模式差异](#43-交互模式差异)
  - [4.4 能力边界差异](#44-能力边界差异)
- [5. 用 Spring-AI 实现 AI Agent](#5-用-spring-ai-实现-ai-agent)
  - [5.1 实现路径概览](#51-实现路径概览)
  - [5.2 路径一：基于 ChatClient + Tool Calling 实现 ReAct Agent](#52-路径一基于-chatclient--tool-calling-实现-react-agent)
  - [5.3 路径二：基于 Advisor 链实现 Agent 横切能力](#53-路径二基于-advisor-链实现-agent-横切能力)
  - [5.4 路径三：基于 AbstractAgent 抽象实现完整 Agent](#54-路径三基于-abstractagent-抽象实现完整-agent)
  - [5.5 路径四：Multi-Agent 编排](#55-路径四multi-agent-编排)
- [6. 完整实现示例](#6-完整实现示例)
  - [6.1 项目依赖配置](#61-项目依赖配置)
  - [6.2 工具定义](#62-工具定义)
  - [6.3 ReAct Agent 实现](#63-react-agent-实现)
  - [6.4 自定义 Advisor 实现](#64-自定义-advisor-实现)
  - [6.5 Agent 编排器](#65-agent-编排器)
  - [6.6 使用示例](#66-使用示例)
- [7. 最佳实践](#7-最佳实践)
- [8. 总结](#8-总结)

---

## 1. 概述

Spring AI 框架中，**ChatClient** 和 **AI Agent** 是两个不同层级的抽象：

- **ChatClient** 是 Spring AI 提供的**对话客户端 API**，封装了与 LLM 的交互逻辑，支持 Advisor 链、Tool Calling、Chat Memory 等能力。它是构建 AI 应用的**基础组件**。
- **AI Agent** 是一个**更高层级的架构概念**，指能够自主感知环境、做出决策、调用工具、维护状态并完成复杂任务的智能体。ChatClient 是 Agent 的**核心组件之一**，但 Agent 还需要推理循环、工具编排、记忆管理、安全护栏等额外能力。

简言之：**ChatClient 是"嘴和耳"，Agent 是"完整的脑"**。

---

## 2. ChatClient 详解

### 2.1 ChatClient 是什么

ChatClient 是 Spring AI 对 LLM 交互的**统一抽象层**，类似于 RestTemplate 之于 HTTP 请求。它提供：

- 统一的 API 接口，屏蔽不同 LLM 提供商（OpenAI、Ollama、DeepSeek 等）的差异
- 流式（stream）和同步（call）两种调用模式
- Advisor 拦截链机制，支持横切关注点
- Tool Calling 集成
- Chat Memory 集成

### 2.2 ChatClient 核心架构

```
┌─────────────────────────────────────────────────┐
│                   ChatClient                      │
├─────────────────────────────────────────────────┤
│  .prompt()        → 设置用户提示词 / Prompt 对象  │
│  .options()       → 设置模型参数 (温度、topK等)   │
│  .advisors()      → 添加 Advisor 拦截器          │
│  .toolCallbacks() → 注册工具回调                  │
│  .call()          → 同步调用，返回 ChatResponse   │
│  .stream()        → 流式调用，返回 Flux<String>   │
└─────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────┐
│              Advisor Chain (拦截链)               │
│  RequestAdvisor → ChatModel → ResponseAdvisor   │
└─────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────┐
│                 ChatModel (底层)                  │
│  OpenAiChatModel / OllamaChatModel / ...        │
└─────────────────────────────────────────────────┘
```

### 2.3 ChatClient 的 Advisor 机制

Advisor 是 ChatClient 的核心扩展机制，分为两类：

| Advisor 类型 | 作用 | 典型实现 |
|-------------|------|---------|
| **RequestAdvisor** | 在请求发送前拦截，修改 Prompt | `MessageChatMemoryAdvisor`（注入对话历史） |
| **ResponseAdvisor** | 在响应返回后拦截，修改 Response | 日志、观测、后处理 |

Spring AI 内置的关键 Advisor：

| Advisor | 功能 |
|---------|------|
| `MessageChatMemoryAdvisor` | 自动管理对话历史，将历史消息注入 Prompt |
| `RetrievalAugmentationAdvisor` | RAG 检索增强，自动检索相关文档并注入上下文 |
| `VectorStoreChatMemoryAdvisor` | 基于向量存储的长期记忆 |

### 2.4 ChatClient 使用示例

**基本对话：**

```java
ChatClient chatClient = ChatClient.builder(chatModel).build();

// 同步调用
String response = chatClient.prompt("你好，请介绍一下自己").call().content();

// 流式调用
Flux<String> stream = chatClient.prompt("写一首诗").stream().content();
```

**带记忆的对话：**

```java
MessageWindowChatMemory memory = MessageWindowChatMemory.builder()
    .chatMemoryRepository(jdbcChatMemoryRepository)
    .maxMessages(20)
    .build();

ChatClient chatClient = ChatClient.builder(chatModel)
    .defaultAdvisors(MessageChatMemoryAdvisor.builder(memory).build())
    .build();

// 自动携带对话历史
String response = chatClient.prompt("我刚才说了什么？").call().content();
```

**带 RAG 的对话：**

```java
RetrievalAugmentationAdvisor ragAdvisor = RetrievalAugmentationAdvisor.builder()
    .documentRetriever(VectorStoreDocumentRetriever.builder()
        .vectorStore(vectorStore)
        .build())
    .queryAugmenter(ContextualQueryAugmenter.builder()
        .allowEmptyContext(true)
        .build())
    .build();

String response = ChatClient.builder(chatModel).build()
    .prompt("什么是微服务架构？")
    .advisors(ragAdvisor)
    .call()
    .content();
```

**带 Tool Calling 的对话：**

```java
@AgentTools
public class WeatherTools {
    @Tool(name = "get_weather", description = "获取指定城市的天气信息")
    public String getWeather(String city) {
        // 调用天气 API
        return weatherApi.query(city);
    }
}

// 注册工具
MethodToolCallbackProvider toolProvider = MethodToolCallbackProvider.builder()
    .toolObjects(new WeatherTools())
    .build();

String response = chatClient.prompt("北京今天天气怎么样？")
    .toolCallbacks(toolProvider.getToolCallbacks())
    .call()
    .content();
```

### 2.5 ChatClient 的局限性

ChatClient 虽然功能强大，但作为**单轮对话抽象**，存在以下局限：

| 局限性 | 说明 |
|--------|------|
| **无自主推理循环** | ChatClient 是"一问一答"模式，不会自主决定是否需要继续调用工具或重新思考 |
| **无任务规划** | 无法将复杂任务拆解为子任务并按序执行 |
| **无状态管理** | ChatClient 本身不维护 Agent 状态（如当前执行步骤、中间结果等） |
| **无编排能力** | 无法协调多个 ChatClient 或多个工具的执行顺序 |
| **无安全护栏** | 不内置输入/输出安全检查、权限控制 |
| **无多 Agent 协作** | 不支持 Agent 间通信和任务分发 |

这些局限性正是 **AI Agent** 需要解决的问题。

---

## 3. AI Agent 详解

### 3.1 AI Agent 是什么

AI Agent 是一个**自主决策系统**，具备以下核心特征：

1. **感知（Perception）**：接收用户输入、环境状态、工具返回结果
2. **推理（Reasoning）**：基于当前状态决定下一步行动（思考、调用工具、返回结果）
3. **行动（Action）**：执行工具调用、更新记忆、与外部系统交互
4. **反思（Reflection）**：评估行动结果，决定是否需要修正或继续
5. **记忆（Memory）**：维护对话历史、任务状态、经验知识

### 3.2 Agent 核心架构

```
┌──────────────────────────────────────────────────────────────┐
│                        AI Agent                               │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌──────────┐    ┌──────────┐    ┌──────────┐               │
│  │  推理引擎  │ ←→ │  工具注册  │ ←→ │  记忆系统  │               │
│  │ (ReAct/   │    │ (Tool    │    │ (Working/ │               │
│  │  Plan&Exec)│    │ Registry)│    │  Semantic)│               │
│  └──────────┘    └──────────┘    └──────────┘               │
│        │                │                │                     │
│        ▼                ▼                ▼                     │
│  ┌──────────────────────────────────────────────┐           │
│  │              ChatClient (底层对话能力)          │           │
│  └──────────────────────────────────────────────┘           │
│        │                                                      │
│        ▼                                                      │
│  ┌──────────────────────────────────────────────┐           │
│  │              Advisor Chain (横切关注点)         │           │
│  │  Memory → RAG → Guardrail → Observability     │           │
│  └──────────────────────────────────────────────┘           │
│                                                               │
├──────────────────────────────────────────────────────────────┤
│  安全护栏 │ 可观测性 │ 成本治理 │ 权限控制                      │
└──────────────────────────────────────────────────────────────┘
```

### 3.3 Agent 的关键能力

| 能力 | 说明 | ChatClient 是否具备 |
|------|------|:---:|
| **自主推理循环** | Agent 自主决定何时继续思考、何时调用工具、何时返回结果 | ❌ |
| **多步工具调用** | 在一次任务中按需多次调用不同工具 | ⚠️ (单轮) |
| **任务规划与拆解** | 将复杂任务拆解为可执行的子任务序列 | ❌ |
| **状态管理** | 维护任务执行状态、中间结果、执行步骤 | ❌ |
| **错误恢复** | 工具调用失败时自主重试或换用替代方案 | ❌ |
| **多 Agent 协作** | Agent 间通信、任务分发、结果汇总 | ❌ |
| **安全护栏** | 输入/输出过滤、权限控制、敏感信息脱敏 | ❌ |
| **对话记忆** | 短期对话历史管理 | ✅ |
| **RAG 检索** | 基于向量存储的文档检索增强 | ✅ |
| **Tool Calling** | LLM 驱动的工具调用 | ✅ |

### 3.4 Agent 的推理模式

#### ReAct 模式（Reasoning + Acting）

最常用的 Agent 推理模式，循环执行"思考→行动→观察"：

```
用户: "帮我分析这个项目的代码质量"

Agent 思考: 我需要先了解项目结构，然后检查代码规范，最后生成报告
Agent 行动: 调用 list_files 工具
Agent 观察: 获得文件列表

Agent 思考: 现在我需要检查关键文件的代码质量
Agent 行动: 调用 analyze_code 工具
Agent 观察: 获得分析结果

Agent 思考: 我已经收集了足够信息，可以生成报告了
Agent 行动: 调用 generate_report 工具
Agent 观察: 报告已生成

Agent 返回: "代码质量分析报告：..."
```

#### Plan-and-Execute 模式

先规划完整任务序列，再逐步执行：

```
用户: "重构用户认证模块"

Agent 规划:
  Step 1: 分析现有认证代码
  Step 2: 设计新的认证架构
  Step 3: 实现新代码
  Step 4: 编写测试
  Step 5: 验证测试通过

Agent 执行: Step 1 → Step 2 → Step 3 → Step 4 → Step 5
```

---

## 4. ChatClient 与 Agent 的核心区别

### 4.1 对比总览

| 维度 | ChatClient | AI Agent |
|------|-----------|----------|
| **抽象层级** | LLM 交互层 | 自主决策层 |
| **核心职责** | 发送 Prompt、接收 Response | 感知→推理→行动→反思 |
| **执行模式** | 单轮请求-响应 | 多轮自主循环 |
| **决策权** | 人类决定何时调用 | Agent 自主决定下一步 |
| **工具调用** | LLM 返回工具调用意图，框架执行一次 | Agent 自主决定调用哪些工具、调用几次 |
| **错误处理** | 抛异常，由调用方处理 | 自主重试、换方案、降级 |
| **状态管理** | 无状态（记忆由 Advisor 管理） | 有状态（任务进度、中间结果） |
| **编排能力** | 无 | 支持多步骤、多 Agent 编排 |
| **类比** | HTTP Client | 微服务编排引擎 |
| **类比** | SQL 连接 | 存储过程 |

### 4.2 架构层级差异

```
┌─────────────────────────────────────────┐
│         Application Layer (应用层)        │  ← 用户交互
├─────────────────────────────────────────┤
│         Agent Layer (智能体层)            │  ← 推理、规划、编排
│  ┌─────────┐ ┌─────────┐ ┌─────────┐  │
│  │ Agent A │ │ Agent B │ │ Agent C │  │  ← 多 Agent 协作
│  └────┬────┘ └────┬────┘ └────┬────┘  │
│       └──────────┼──────────┘         │
│            ┌──────┴──────┐             │
│            │  Orchestration│            │  ← 编排器
│            └──────┬──────┘             │
├──────────────────┼────────────────────┤
│         ChatClient Layer (对话层)       │  ← LLM 交互
│            ┌──────┴──────┐             │
│            │  Advisor Chain│            │  ← 横切关注点
│            └──────┬──────┘             │
├──────────────────┼────────────────────┤
│         ChatModel Layer (模型层)        │  ← LLM API 调用
│  ┌─────┐ ┌─────┐ ┌─────┐ ┌─────────┐ │
│  │OpenAI│ │Ollama│ │DeepSeek│ │Qwen  │ │
│  └─────┘ └─────┘ └─────┘ └─────────┘ │
└─────────────────────────────────────────┘
```

### 4.3 交互模式差异

**ChatClient 交互模式（单轮）：**

```
用户 → ChatClient → LLM → 响应
                    ↓ (可选)
                  Tool → 结果 → LLM → 响应
```

ChatClient 的 Tool Calling 是**单次迭代**：LLM 返回工具调用意图 → 框架执行工具 → 将结果回传 LLM → LLM 生成最终响应。如果 LLM 再次返回工具调用，Spring AI 会继续执行，但这是**框架驱动的自动循环**，不是 Agent 的自主推理。

**Agent 交互模式（多轮自主）：**

```
用户 → Agent
        │
        ├→ 思考: 需要做什么？
        ├→ 行动: 调用工具A
        ├→ 观察: 工具A返回结果
        ├→ 思考: 结果是否满足？需要继续吗？
        ├→ 行动: 调用工具B
        ├→ 观察: 工具B返回结果
        ├→ 思考: 任务完成，整理结果
        └→ 返回: 最终结果
```

### 4.4 能力边界差异

```
ChatClient 能力边界:
┌─────────────────────────────────────┐
│  ✅ 单轮对话                         │
│  ✅ 流式响应                         │
│  ✅ 对话记忆 (via Advisor)           │
│  ✅ RAG 检索 (via Advisor)           │
│  ✅ Tool Calling (单次或自动循环)     │
│  ✅ 模型参数配置                      │
│  ✅ Advisor 拦截链                   │
└─────────────────────────────────────┘

Agent 能力边界 (包含 ChatClient 所有能力，加上):
┌─────────────────────────────────────┐
│  ✅ 自主推理循环 (ReAct/Plan-Exec)   │
│  ✅ 多步工具编排                      │
│  ✅ 任务规划与拆解                    │
│  ✅ 状态管理 (执行步骤、中间结果)     │
│  ✅ 错误恢复与重试                    │
│  ✅ 多 Agent 协作与通信              │
│  ✅ 安全护栏 (输入/输出过滤)          │
│  ✅ 可观测性 (全链路追踪)             │
│  ✅ 成本治理                          │
│  ✅ 条件分支 (if-else 决策)          │
│  ✅ 并行执行                          │
└─────────────────────────────────────┘
```

---

## 5. 用 Spring-AI 实现 AI Agent

### 5.1 实现路径概览

Spring AI 本身没有提供开箱即用的 `Agent` 抽象（截至 2.0.0-M4），但提供了构建 Agent 所需的**全部基础组件**。实现 Agent 有以下路径：

| 路径 | 复杂度 | 适用场景 | 核心思路 |
|------|--------|---------|---------|
| **路径一** | ⭐⭐ | 简单工具调用 Agent | ChatClient + Tool Calling，利用框架自动循环 |
| **路径二** | ⭐⭐⭐ | 需要横切能力的 Agent | 自定义 Advisor 链（安全护栏、观测、限流） |
| **路径三** | ⭐⭐⭐⭐ | 完整自主 Agent | 自定义 AbstractAgent 抽象 + ReAct 循环 |
| **路径四** | ⭐⭐⭐⭐⭐ | 多 Agent 协作 | Agent 编排器 + 通信总线 + Supervisor |

### 5.2 路径一：基于 ChatClient + Tool Calling 实现 ReAct Agent

这是最简单的 Agent 实现方式，利用 Spring AI 的**自动 Tool Calling 循环**。

**原理：** 当 `internalToolExecutionEnabled=true` 时，Spring AI 会自动执行 LLM 返回的工具调用，将结果回传 LLM，如果 LLM 再次返回工具调用则继续执行，直到 LLM 返回最终文本响应。这本质上就是一个**框架驱动的 ReAct 循环**。

```java
@Component
public class SimpleAgent {

    private final ChatClient chatClient;

    public SimpleAgent(ChatModel chatModel, List<ToolCallback> tools) {
        DefaultToolCallingChatOptions options = new DefaultToolCallingChatOptions();
        options.setInternalToolExecutionEnabled(true);  // 启用自动工具执行循环
        options.setToolCallbacks(tools);

        this.chatClient = ChatClient.builder(chatModel)
            .defaultSystem("你是一个智能助手，可以使用工具来完成任务。请根据用户需求选择合适的工具。")
            .defaultOptions(options)
            .build();
    }

    public String execute(String userMessage) {
        return chatClient.prompt(userMessage).call().content();
    }

    public Flux<String> stream(String userMessage) {
        return chatClient.prompt(userMessage).stream().content();
    }
}
```

**优点：** 实现简单，利用 Spring AI 内置的 Tool Calling 循环
**缺点：** 无法控制推理过程，无法在循环中插入自定义逻辑（如安全检查、步骤计数、条件分支）

### 5.3 路径二：基于 Advisor 链实现 Agent 横切能力

通过自定义 Advisor 为 Agent 添加安全护栏、可观测性、限流等横切能力。

```java
// 自定义安全护栏 Advisor
public class GuardrailAdvisor implements RequestResponseAdvisor {

    private final List<String> blockedPatterns;

    @Override
    public AdvisedRequest adviseRequest(AdvisedRequest request, Map<String, Object> context) {
        // 输入安全检查
        String userMessage = request.userText();
        for (String pattern : blockedPatterns) {
            if (userMessage.contains(pattern)) {
                throw new GuardrailException("输入包含禁止内容: " + pattern);
            }
        }
        return request;
    }

    @Override
    public ChatResponse adviseResponse(ChatResponse response, Map<String, Object> context) {
        // 输出安全检查
        String content = response.getResult().getOutput().getText();
        // 脱敏、过滤等处理
        return response;
    }
}

// 自定义可观测性 Advisor
public class ObservabilityAdvisor implements RequestResponseAdvisor {

    private final MeterRegistry meterRegistry;

    @Override
    public AdvisedRequest adviseRequest(AdvisedRequest request, Map<String, Object> context) {
        context.put("startTime", System.currentTimeMillis());
        context.put("tokenCount", 0);
        return request;
    }

    @Override
    public ChatResponse adviseResponse(ChatResponse response, Map<String, Object> context) {
        long duration = System.currentTimeMillis() - (long) context.get("startTime");
        meterRegistry.timer("agent.chat.duration").record(duration, TimeUnit.MILLISECONDS);
        return response;
    }
}

// 组装 Agent
ChatClient agentClient = ChatClient.builder(chatModel)
    .defaultAdvisors(
        guardrailAdvisor,           // 安全护栏
        observabilityAdvisor,       // 可观测性
        MessageChatMemoryAdvisor.builder(memory).build(),  // 对话记忆
        ragAdvisor                  // RAG 检索
    )
    .build();
```

### 5.4 路径三：基于 AbstractAgent 抽象实现完整 Agent

这是最灵活的方式，自定义 Agent 抽象，实现完整的 ReAct 推理循环。

```java
public abstract class AbstractAgent {

    protected final ChatClient chatClient;
    protected final ChatMemory chatMemory;
    protected final List<ToolCallback> tools;
    protected final String agentName;
    protected final String systemPrompt;
    protected final int maxIterations;

    protected AbstractAgent(ChatModel chatModel,
                            ChatMemory chatMemory,
                            List<ToolCallback> tools,
                            String agentName,
                            String systemPrompt,
                            int maxIterations) {
        this.chatMemory = chatMemory;
        this.tools = tools;
        this.agentName = agentName;
        this.systemPrompt = systemPrompt;
        this.maxIterations = maxIterations;

        // 构建 ChatClient，注入所有横切关注点
        this.chatClient = ChatClient.builder(chatModel)
            .defaultSystem(systemPrompt)
            .defaultAdvisors(
                MessageChatMemoryAdvisor.builder(chatMemory).build()
            )
            .build();
    }

    /**
     * ReAct 推理循环
     */
    public String run(String userMessage) {
        chatMemory.add(new UserMessage(userMessage));

        for (int i = 0; i < maxIterations; i++) {
            // 推理步骤：让 LLM 决定下一步行动
            ChatResponse response = chatClient.prompt()
                .options(options -> {
                    if (!tools.isEmpty()) {
                        DefaultToolCallingChatOptions toolOptions = new DefaultToolCallingChatOptions();
                        toolOptions.setInternalToolExecutionEnabled(true);
                        toolOptions.setToolCallbacks(tools);
                    }
                })
                .call()
                .chatResponse();

            String content = response.getResult().getOutput().getText();
            chatMemory.add(new AssistantMessage(content));

            // 检查是否需要继续推理
            if (shouldTerminate(content)) {
                return content;
            }
        }

        return "达到最大推理步数限制，任务未完成。";
    }

    /**
     * 流式 ReAct 推理循环
     */
    public Flux<AgentStep> stream(String userMessage) {
        return Flux.create(sink -> {
            chatMemory.add(new UserMessage(userMessage));

            for (int i = 0; i < maxIterations; i++) {
                // 每一步的流式输出
                String content = chatClient.prompt()
                    .options(...)
                    .stream()
                    .content()
                    .collectList()
                    .block()
                    .stream()
                    .collect(Collectors.joining());

                sink.next(new AgentStep(i, "reasoning", content));

                if (shouldTerminate(content)) {
                    sink.complete();
                    return;
                }
            }
            sink.complete();
        });
    }

    protected abstract boolean shouldTerminate(String response);

    public record AgentStep(int step, String type, String content) {}
}
```

**具体 Agent 实现：**

```java
@Component
public class CodeAnalysisAgent extends AbstractAgent {

    private final List<ToolCallback> codeTools;

    public CodeAnalysisAgent(ChatModel chatModel,
                             ChatMemory chatMemory,
                             @Qualifier("codeTools") List<ToolCallback> codeTools) {
        super(chatModel, chatMemory, codeTools,
            "CodeAnalysisAgent",
            "你是一个代码分析专家。你可以使用工具来分析代码质量、查找问题、生成报告。" +
            "请按以下步骤工作：\n" +
            "1. 首先了解项目结构\n" +
            "2. 分析关键代码文件\n" +
            "3. 识别潜在问题\n" +
            "4. 生成分析报告\n" +
            "当分析完成时，请输出 [ANALYSIS_COMPLETE] 标记。",
            10  // 最多 10 轮推理
        );
        this.codeTools = codeTools;
    }

    @Override
    protected boolean shouldTerminate(String response) {
        return response.contains("[ANALYSIS_COMPLETE]") ||
               response.contains("分析完成");
    }
}
```

### 5.5 路径四：Multi-Agent 编排

实现多个 Agent 的协作，支持 Supervisor 模式。

```java
// Agent 通信总线
@Component
public class AgentCommunicationBus {

    private final ApplicationEventPublisher eventPublisher;

    public void publish(AgentMessage message) {
        eventPublisher.publishEvent(message);
    }
}

public record AgentMessage(
    String fromAgent,
    String toAgent,
    String type,       // "task", "result", "error", "control"
    String content,
    Map<String, Object> metadata
) {}

// Supervisor Agent - 负责任务分发和结果汇总
@Component
public class SupervisorAgent {

    private final Map<String, AbstractAgent> agents;
    private final AgentCommunicationBus communicationBus;

    public String orchestrate(String task) {
        // 1. 分析任务，决定需要哪些 Agent 参与
        List<String> requiredAgents = planAgents(task);

        // 2. 拆分子任务
        Map<String, String> subTasks = decompose(task, requiredAgents);

        // 3. 分发任务给各 Agent
        Map<String, CompletableFuture<String>> futures = new HashMap<>();
        for (Map.Entry<String, String> entry : subTasks.entrySet()) {
            String agentName = entry.getKey();
            String subTask = entry.getValue();
            futures.put(agentName,
                CompletableFuture.supplyAsync(() -> {
                    communicationBus.publish(new AgentMessage(
                        "supervisor", agentName, "task", subTask, Map.of()
                    ));
                    return agents.get(agentName).run(subTask);
                })
            );
        }

        // 4. 等待所有 Agent 完成，汇总结果
        Map<String, String> results = futures.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue().join()
            ));

        // 5. 整合最终结果
        return synthesize(task, results);
    }
}
```

---

## 6. 完整实现示例

以下是一个基于 Spring AI 2.0.0-M4 的完整 ReAct Agent 实现。

### 6.1 项目依赖配置

```groovy
// build.gradle
ext {
    springAiVersion = '2.0.0-M4'
}

dependencies {
    // Spring AI 核心
    implementation 'org.springframework.ai:spring-ai-client-chat'
    implementation 'org.springframework.ai:spring-ai-model'

    // 模型提供商（按需选择）
    implementation 'org.springframework.ai:spring-ai-openai'
    implementation 'org.springframework.ai:spring-ai-ollama'

    // 对话记忆
    implementation 'org.springframework.ai:spring-ai-starter-model-chat-memory-repository-jdbc'

    // RAG
    implementation 'org.springframework.ai:spring-ai-rag'
    implementation 'org.springframework.ai:spring-ai-advisors-vector-store'

    // 向量存储（按需选择）
    implementation 'org.springframework.ai:spring-ai-qdrant-store'
}
```

### 6.2 工具定义

```java
// 文件系统工具
@AgentTools(defaultEnable = true)
public class FileSystemTools {

    @Tool(name = "list_files", description = "列出指定目录下的文件和子目录")
    public String listFiles(@ToolParam(description = "目录路径") String path) {
        return Arrays.toString(new File(path).list());
    }

    @Tool(name = "read_file", description = "读取文件内容")
    public String readFile(@ToolParam(description = "文件路径") String path) throws IOException {
        return Files.readString(Path.of(path));
    }

    @Tool(name = "write_file", description = "将内容写入文件")
    public String writeFile(@ToolParam(description = "文件路径") String path,
                           @ToolParam(description = "文件内容") String content) throws IOException {
        Files.writeString(Path.of(path), content);
        return "文件写入成功: " + path;
    }
}

// 代码分析工具
@AgentTools(defaultEnable = true)
public class CodeAnalysisTools {

    @Tool(name = "count_lines", description = "统计代码文件行数")
    public String countLines(@ToolParam(description = "文件路径") String path) throws IOException {
        long lines = Files.lines(Path.of(path)).count();
        return "文件 " + path + " 共 " + lines + " 行";
    }

    @Tool(name = "search_pattern", description = "在文件中搜索匹配正则表达式的内容")
    public String searchPattern(@ToolParam(description = "文件路径") String path,
                               @ToolParam(description = "正则表达式") String pattern) throws IOException {
        Pattern regex = Pattern.compile(pattern);
        List<String> matches = Files.lines(Path.of(path))
            .filter(line -> regex.matcher(line).find())
            .toList();
        return "找到 " + matches.size() + " 处匹配:\n" + String.join("\n", matches);
    }
}
```

### 6.3 ReAct Agent 实现

```java
/**
 * ReAct Agent - 基于 Spring AI ChatClient 实现的推理-行动循环 Agent
 */
public class ReActAgent {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final List<ToolCallback> tools;
    private final int maxIterations;
    private final String systemPrompt;

    private ReActAgent(Builder builder) {
        this.chatMemory = builder.chatMemory;
        this.tools = builder.tools;
        this.maxIterations = builder.maxIterations;
        this.systemPrompt = builder.systemPrompt;

        // 构建 ChatClient
        ChatClient.Builder clientBuilder = ChatClient.builder(builder.chatModel)
            .defaultSystem(systemPrompt);

        // 注入 Advisor 链
        List<Advisor> advisors = new ArrayList<>();
        if (chatMemory != null) {
            advisors.add(MessageChatMemoryAdvisor.builder(chatMemory).build());
        }
        advisors.addAll(builder.customAdvisors);
        if (!advisors.isEmpty()) {
            clientBuilder.defaultAdvisors(advisors.toArray(new Advisor[0]));
        }

        this.chatClient = clientBuilder.build();
    }

    /**
     * 执行 ReAct 推理循环
     */
    public AgentResult run(String userMessage) {
        List<AgentStep> steps = new ArrayList<>();
        chatMemory.add(new UserMessage(userMessage));

        for (int i = 0; i < maxIterations; i++) {
            long startTime = System.currentTimeMillis();

            // 构建请求选项
            DefaultToolCallingChatOptions options = new DefaultToolCallingChatOptions();
            options.setInternalToolExecutionEnabled(true);
            if (!tools.isEmpty()) {
                options.setToolCallbacks(tools);
            }

            // 调用 LLM
            ChatResponse response = chatClient.prompt()
                .options(options)
                .call()
                .chatResponse();

            String content = response.getResult().getOutput().getText();
            chatMemory.add(new AssistantMessage(content));

            long duration = System.currentTimeMillis() - startTime;
            steps.add(new AgentStep(i, content, duration));

            // 检查终止条件
            if (isTaskComplete(content)) {
                return AgentResult.success(content, steps);
            }
        }

        return AgentResult.incomplete("达到最大推理步数限制", steps);
    }

    /**
     * 流式执行 ReAct 推理循环
     */
    public Flux<AgentStep> stream(String userMessage) {
        chatMemory.add(new UserMessage(userMessage));

        return Flux.create(sink -> {
            for (int i = 0; i < maxIterations; i++) {
                DefaultToolCallingChatOptions options = new DefaultToolCallingChatOptions();
                options.setInternalToolExecutionEnabled(true);
                if (!tools.isEmpty()) {
                    options.setToolCallbacks(tools);
                }

                StringBuilder contentBuilder = new StringBuilder();
                chatClient.prompt()
                    .options(options)
                    .stream()
                    .content()
                    .doOnNext(chunk -> {
                        contentBuilder.append(chunk);
                        sink.next(new AgentStep(i, chunk, 0));
                    })
                    .blockLast();

                String fullContent = contentBuilder.toString();
                chatMemory.add(new AssistantMessage(fullContent));

                if (isTaskComplete(fullContent)) {
                    sink.complete();
                    return;
                }
            }
            sink.complete();
        });
    }

    private boolean isTaskComplete(String response) {
        // 检查是否包含完成标记或不再请求工具调用
        return response.contains("[DONE]") ||
               response.contains("任务完成") ||
               response.contains("分析完成");
    }

    // Builder 模式
    public static Builder builder(ChatModel chatModel) {
        return new Builder(chatModel);
    }

    public static class Builder {
        private final ChatModel chatModel;
        private ChatMemory chatMemory;
        private List<ToolCallback> tools = new ArrayList<>();
        private List<Advisor> customAdvisors = new ArrayList<>();
        private int maxIterations = 10;
        private String systemPrompt = "你是一个智能助手，可以使用工具完成任务。";

        public Builder(ChatModel chatModel) { this.chatModel = chatModel; }
        public Builder chatMemory(ChatMemory chatMemory) { this.chatMemory = chatMemory; return this; }
        public Builder tools(List<ToolCallback> tools) { this.tools = tools; return this; }
        public Builder advisors(Advisor... advisors) { this.customAdvisors = Arrays.asList(advisors); return this; }
        public Builder maxIterations(int max) { this.maxIterations = max; return this; }
        public Builder systemPrompt(String prompt) { this.systemPrompt = prompt; return this; }
        public ReActAgent build() { return new ReActAgent(this); }
    }

    // 结果记录
    public record AgentStep(int iteration, String content, long durationMs) {}
    public record AgentResult(boolean success, String content, List<AgentStep> steps) {
        public static AgentResult success(String content, List<AgentStep> steps) {
            return new AgentResult(true, content, steps);
        }
        public static AgentResult incomplete(String content, List<AgentStep> steps) {
            return new AgentResult(false, content, steps);
        }
    }
}
```

### 6.4 自定义 Advisor 实现

```java
/**
 * 安全护栏 Advisor - 检查输入/输出是否安全
 */
public class SafetyGuardrailAdvisor implements RequestResponseAdvisor {

    private final List<Pattern> blockedInputPatterns;
    private final List<Pattern> blockedOutputPatterns;

    public SafetyGuardrailAdvisor(List<String> blockedInputs, List<String> blockedOutputs) {
        this.blockedInputPatterns = blockedInputs.stream()
            .map(Pattern::compile).toList();
        this.blockedOutputPatterns = blockedOutputs.stream()
            .map(Pattern::compile).toList();
    }

    @Override
    public AdvisedRequest adviseRequest(AdvisedRequest request, Map<String, Object> context) {
        String userText = request.userText();
        for (Pattern pattern : blockedInputPatterns) {
            if (pattern.matcher(userText).find()) {
                throw new SafetyException("输入违反安全策略: " + pattern.pattern());
            }
        }
        return request;
    }

    @Override
    public ChatResponse adviseResponse(ChatResponse response, Map<String, Object> context) {
        String output = response.getResult().getOutput().getText();
        for (Pattern pattern : blockedOutputPatterns) {
            if (pattern.matcher(output).find()) {
                // 脱敏处理而非抛异常
                String sanitized = pattern.matcher(output).replaceAll("[REDACTED]");
                // 返回脱敏后的响应
                return ChatResponse.builder()
                    .from(response)
                    .withGenerations(List.of(new Generation(new AssistantMessage(sanitized))))
                    .build();
            }
        }
        return response;
    }
}

/**
 * 限流 Advisor - 控制 Agent 调用频率
 */
public class RateLimitAdvisor implements RequestResponseAdvisor {

    private final RateLimiter rateLimiter;

    public RateLimitAdvisor(double permitsPerSecond) {
        this.rateLimiter = RateLimiter.create(permitsPerSecond);
    }

    @Override
    public AdvisedRequest adviseRequest(AdvisedRequest request, Map<String, Object> context) {
        rateLimiter.acquire();  // 阻塞等待许可
        return request;
    }
}

/**
 * 步骤计数 Advisor - 记录推理步数，防止无限循环
 */
public class StepCountAdvisor implements RequestResponseAdvisor {

    private static final String STEP_COUNT_KEY = "agent.stepCount";
    private final int maxSteps;

    public StepCountAdvisor(int maxSteps) {
        this.maxSteps = maxSteps;
    }

    @Override
    public AdvisedRequest adviseRequest(AdvisedRequest request, Map<String, Object> context) {
        int currentStep = (int) context.getOrDefault(STEP_COUNT_KEY, 0) + 1;
        if (currentStep > maxSteps) {
            throw new StepLimitExceededException("Agent 推理步数超过限制: " + maxSteps);
        }
        context.put(STEP_COUNT_KEY, currentStep);
        return request;
    }
}
```

### 6.5 Agent 编排器

```java
/**
 * Multi-Agent 编排器 - Supervisor 模式
 */
@Component
public class AgentOrchestrator {

    private final Map<String, ReActAgent> agentRegistry;
    private final ChatClient supervisorClient;

    public AgentOrchestrator(ChatModel chatModel,
                            Map<String, ReActAgent> agents) {
        this.agentRegistry = agents;
        this.supervisorClient = ChatClient.builder(chatModel)
            .defaultSystem("""
                你是一个任务编排器。根据用户任务，决定需要哪些 Agent 参与，
                以及每个 Agent 应该执行什么子任务。

                可用 Agent:
                %s

                请以 JSON 格式输出编排计划。
                """.formatted(agents.keySet().stream()
                    .map(name -> "- " + name)
                    .collect(Collectors.joining("\n"))))
            .build();
    }

    /**
     * 编排执行：分析任务 → 分发子任务 → 汇总结果
     */
    public String orchestrate(String task) {
        // 1. 分析任务，生成编排计划
        String plan = supervisorClient.prompt()
            .user("请为以下任务生成编排计划: " + task)
            .call()
            .content();

        // 2. 解析编排计划
        List<SubTask> subTasks = parsePlan(plan);

        // 3. 按依赖顺序执行子任务
        Map<String, String> results = new LinkedHashMap<>();
        for (SubTask subTask : subTasks) {
            ReActAgent agent = agentRegistry.get(subTask.agentName());
            if (agent != null) {
                String context = buildContext(subTask, results);
                String result = agent.run(context);
                results.put(subTask.id(), result);
            }
        }

        // 4. 汇总最终结果
        return supervisorClient.prompt()
            .user("请汇总以下子任务结果，生成最终回答:\n" + formatResults(results))
            .call()
            .content();
    }

    private record SubTask(String id, String agentName, String task, List<String> dependencies) {}

    private List<SubTask> parsePlan(String plan) {
        // 解析 JSON 编排计划
        // 实现省略
        return List.of();
    }

    private String buildContext(SubTask subTask, Map<String, String> results) {
        StringBuilder context = new StringBuilder(subTask.task());
        for (String dep : subTask.dependencies()) {
            context.append("\n\n前置任务结果:\n").append(results.get(dep));
        }
        return context.toString();
    }

    private String formatResults(Map<String, String> results) {
        return results.entrySet().stream()
            .map(e -> "## " + e.getKey() + "\n" + e.getValue())
            .collect(Collectors.joining("\n\n"));
    }
}
```

### 6.6 使用示例

```java
@SpringBootApplication
public class AgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentApplication.class, args);
    }

    @Bean
    public ReActAgent codeAnalysisAgent(ChatModel chatModel,
                                        JdbcChatMemoryRepository memoryRepository) {
        // 对话记忆
        MessageWindowChatMemory memory = MessageWindowChatMemory.builder()
            .chatMemoryRepository(memoryRepository)
            .maxMessages(30)
            .build();

        // 工具集
        List<ToolCallback> tools = MethodToolCallbackProvider.builder()
            .toolObjects(new FileSystemTools(), new CodeAnalysisTools())
            .build()
            .getToolCallbacksAsList();

        // 安全护栏
        SafetyGuardrailAdvisor guardrail = new SafetyGuardrailAdvisor(
            List.of("rm -rf", "format", "del /s"),  // 禁止危险命令
            List.of("password", "secret", "token")   // 输出脱敏
        );

        return ReActAgent.builder(chatModel)
            .chatMemory(memory)
            .tools(tools)
            .advisors(guardrail, new StepCountAdvisor(15))
            .maxIterations(15)
            .systemPrompt("""
                你是一个代码分析专家 Agent。请按以下步骤工作：

                1. 使用 list_files 了解项目结构
                2. 使用 read_file 阅读关键代码文件
                3. 使用 count_lines 和 search_pattern 分析代码特征
                4. 整理分析结果，给出改进建议

                当分析完成时，在回复末尾添加 [DONE] 标记。
                """)
            .build();
    }
}

// Controller
@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final ReActAgent codeAnalysisAgent;

    @PostMapping("/analyze")
    public AgentResult analyze(@RequestBody String task) {
        return codeAnalysisAgent.run(task);
    }

    @PostMapping(value = "/analyze/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<AgentStep> analyzeStream(@RequestBody String task) {
        return codeAnalysisAgent.stream(task);
    }
}
```

---

## 7. 最佳实践

### 7.1 Agent 设计原则

| 原则 | 说明 |
|------|------|
| **单一职责** | 每个 Agent 只负责一类任务，工具集与职责匹配 |
| **有限推理** | 设置 maxIterations 防止无限循环，建议 5-15 轮 |
| **明确终止** | System Prompt 中明确告知 Agent 何时应停止推理并返回结果 |
| **工具最小化** | 只注册必要的工具，过多工具会降低 LLM 选择准确率 |
| **记忆分层** | 短期记忆（对话历史）+ 长期记忆（向量存储）+ 工作记忆（任务状态） |

### 7.2 Tool Calling 最佳实践

| 实践 | 说明 |
|------|------|
| **描述精确** | `@Tool` 的 description 要清晰说明功能、输入格式、输出格式 |
| **参数校验** | 工具方法内部校验参数合法性，返回有意义的错误信息 |
| **幂等设计** | 查询类工具应幂等，修改类工具应有确认机制 |
| **超时控制** | 工具调用设置超时，避免长时间阻塞 |
| **结果格式化** | 工具返回结构化文本（JSON 或 Markdown），便于 LLM 理解 |

### 7.3 Advisor 链顺序

Advisor 按注册顺序执行，建议顺序：

```
请求方向:  安全护栏 → 限流 → 步骤计数 → 记忆注入 → RAG 检索
响应方向:  RAG 后处理 → 记忆保存 → 步骤记录 → 限流记录 → 安全脱敏
```

### 7.4 Multi-Agent 编排最佳实践

| 实践 | 说明 |
|------|------|
| **Supervisor 模式** | 工程类任务适合中心化协调，避免 Agent 间无序通信 |
| **上下文传递** | 前置 Agent 的结果作为后续 Agent 的输入上下文 |
| **并行执行** | 无依赖的子任务可并行，有依赖的必须串行 |
| **结果验证** | Supervisor 对子 Agent 结果进行质量检查 |
| **降级策略** | 子 Agent 失败时，Supervisor 决定重试或换方案 |

### 7.5 性能优化

| 优化点 | 方法 |
|--------|------|
| **模型选择** | 简单任务用小模型（快+省），复杂推理用大模型 |
| **流式输出** | 长任务使用 stream() 减少用户等待感 |
| **记忆窗口** | 设置 maxMessages 限制上下文长度，避免 Token 浪费 |
| **工具缓存** | 相同参数的工具调用结果可缓存 |
| **并行工具** | 无依赖的工具调用可并行执行 |

---

## 8. 总结

### 核心关系

```
ChatClient ⊂ Agent

ChatClient 是 Agent 的基础组件，Agent 是 ChatClient 的高阶抽象。
```

### 选择指南

| 场景 | 推荐方案 |
|------|---------|
| 简单问答、对话 | ChatClient + ChatMemory |
| 知识库问答 | ChatClient + RAG Advisor |
| 需要调用工具的对话 | ChatClient + Tool Calling |
| 需要多步推理的任务 | ReAct Agent (ChatClient + Tool Calling + 推理循环) |
| 需要安全控制的 Agent | ChatClient + 自定义 Advisor 链 |
| 复杂工程任务 | Multi-Agent 编排 (Supervisor + 多个专业 Agent) |

### Spring AI Agent 实现路线图

```
Phase 1: ChatClient + Tool Calling
         ↓ (满足不了自主推理需求)
Phase 2: 自定义 ReActAgent (推理循环 + 状态管理)
         ↓ (需要横切能力)
Phase 3: + Advisor 链 (安全护栏、观测、限流)
         ↓ (需要多 Agent 协作)
Phase 4: + AgentOrchestrator (Supervisor 模式编排)
         ↓ (需要标准化)
Phase 5: AbstractAgent 抽象 + AgentRuntime 接口标准化
```

Spring AI 提供了构建 AI Agent 的全部基础组件（ChatClient、Advisor、ToolCallback、ChatMemory、RAG），但 Agent 的推理循环、编排逻辑、状态管理需要开发者自行实现。这种设计给了开发者最大的灵活性，可以根据业务需求选择合适的实现路径。
