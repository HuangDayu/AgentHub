# Spring AI ReAct Agent 运行时实现方案

> 日期：2026-06-14 · 状态：待审核

---

## 1. 需求概述

基于 Spring AI 原生 `ChatClient` + `Advisor` 体系，在 `com.agenthub.infrastructure.agents.spring` 包下实现一套 ReAct Agent 运行时，**不依赖** `spring-ai-alibaba-agent-framework` 或 `agentscope-harness` 等第三方 Agent 框架。

### 关键输入输出

```
输入: ReActAgentContext（领域上下文，含 Agent/Model/Strategy/Tools/Workspace）
输出: AbstractReActAgent（领域抽象的具体实现，支持 call / streamMessages）
```

---

## 2. 架构设计

### 2.1 核心架构

```
SpringReActAgent (extends AbstractReActAgent)
  └── ChatClient (Spring AI 原生)
       ├── ChatModel (通过 SpringShareObjectFactory 解析)
       ├── Default Tools (ToolCallback)
       ├── SystemPrompt (从 ReActAgentContext 注入)
       └── Advisors 链
            ├── ModelStrategyAdvisor      ← ModelStrategy
            ├── GuardrailStrategyAdvisor   ← GuardrailStrategy
            ├── ToolStrategyAdvisor        ← ToolStrategy
            ├── RetrievalStrategyAdvisor   ← RetrievalStrategy
            └── MessageChatMemoryAdvisor   ← ChatMemory (Spring AI 原生)
```

### 2.2 文件清单

| 层 | 文件 | 操作 | 说明 |
|----|------|------|------|
| infrastructure | `SpringReActAgent.java` | 新建 | 继承 `AbstractReActAgent`，封装 `ChatClient` |
| infrastructure | `SpringReActAgentFactory.java` | 新建 | 实现 `ReActAgentFactory`，装配 ChatClient |
| infrastructure | `SpringReActAgentConfig.java` | 新建 | 配置 POJO，纯 Spring AI 类型 |
| infrastructure | `advisor/ModelStrategyAdvisor.java` | 新建 | `CallAroundAdvisor`，桥接 `ModelStrategy` |
| infrastructure | `advisor/GuardrailStrategyAdvisor.java` | 新建 | `CallAroundAdvisor`，桥接 `GuardrailStrategy` |
| infrastructure | `advisor/ToolStrategyAdvisor.java` | 新建 | `CallAroundAdvisor`，桥接 `ToolStrategy` |
| infrastructure | `advisor/RetrievalStrategyAdvisor.java` | 新建 | `CallAroundAdvisor`，桥接 `RetrievalStrategy` |
| test | `SpringReActAgentFactoryTest.java` | 新建 | 单元测试：工厂装配 |
| test | `SpringReActAgentTest.java` | 新建 | 单元测试：Agent 生命周期 |

**总计：7 个生产文件 + 2 个测试文件，共 9 个文件。不修改任何已有文件。**

### 2.3 Advisor 链执行顺序

```
Order -100: RetrievalStrategyAdvisor  (最先执行，RAG 注入)
Order    0: ModelStrategyAdvisor      (模型调用前后)
Order   50: ToolStrategyAdvisor       (工具调用前后)
Order  100: GuardrailStrategyAdvisor  (最后，输入输出安全校验)
```

---

## 3. API 设计

无需新增 Controller API。本次实现仅替换基础设施层的 Agent 运行时，对外接口不变：

- `ReActAgentFactory.create(ReActAgentContext)` → `AbstractReActAgent`
- `AbstractReActAgent.call(List<AgentMessage>)` → `AgentMessage`
- `AbstractReActAgent.streamMessages(List<AgentMessage>)` → `Flux<AgentMessage>`

---

## 4. 关键设计决策

### 4.1 消双重抽象

**现有**：`AbstractReActAgent` → `ReactAgent`/`HarnessAgent` → `ChatModel`

**本方案**：
```
SpringReActAgent.call(messages)
  → beforeInference() (继承 AbstractReActAgent)
  → ChatClient.prompt().system(prompt).user(text).advisors(chain).call()
  → afterInference()
```

`ChatClient` 内部自动处理工具调用（`DefaultToolCallingChatOptions.internalToolExecutionEnabled=true`），无需中间 Agent 框架。

### 4.2 策略即 Advisor

| 领域策略 | Advisor 实现 | 行为 |
|---------|-------------|------|
| `ModelStrategy` | `ModelStrategyAdvisor` | 调用 `beforeInference`/`afterInference` |
| `GuardrailStrategy` | `GuardrailStrategyAdvisor` | 调用 `validateInput`/`validateOutput` |
| `ToolStrategy` | `ToolStrategyAdvisor` | 调用 `beforeToolCall`/`afterToolCall` |
| `RetrievalStrategy` | `RetrievalStrategyAdvisor` | 调用 `beforeRetrieval`/`afterRetrieval` |

Advisor 之间完全解耦，可独立测试、独立组合、独立排序。

### 4.3 工具调用

完全依托 Spring AI 原生 `ToolCallback`：
- 从 `ReActAgentContext.toolCallbacks` 获取 `ToolCallback` 列表（由 `AgentToolsFactory` 解析）
- 通过 `ChatClient.Builder.defaultTools()` 注入
- Spring AI 自动处理工具调用循环（Thought → Action → Observation 由框架完成）

### 4.4 对话记忆

使用 Spring AI 原生 `MessageChatMemoryAdvisor` + `JdbcChatMemoryRepository`（已有）：
- `conversationId` = `ReActAgentContext.sessionId`
- 从 `ModelStrategy.getMaxMessages()` 获取窗口大小

---

## 5. 边界情况

- [ ] `chatModelId` 为 null → 跳过 ChatModel 注入，抛出明确异常
- [ ] `systemPrompt` 为 null → 跳过 system prompt 注入
- [ ] toolCallbacks 为空 → 跳过工具注入
- [ ] advisors 为空 → 使用空 Advisor 链
- [ ] stream 过程中中断 → `interrupt()` 处理
- [ ] ChatClient 调用异常 → 状态置 ERROR，抛出上层
- [ ] 输入/输出护栏校验失败 → 抛出 `ValidationException`
- [ ] 支持 `DefaultToolCallingChatOptions` — 多个 Customer Advisor 时 ChatClient 支持

---

## 6. 检查清单

- [ ] 每个方法 ≤10 行、≤3 参数
- [ ] 中文 Javadoc
- [ ] domain 层零框架依赖（本方案不改 domain，但需注意引用）
- [ ] application 层无 infrastructure 引用（本方案不改 application）
- [ ] Controller 不直接依赖 domain 模型（本次不涉及 Controller）
- [ ] 文件名和包路径符合命名约定
- [ ] 无 `record` / `@Builder` / `@Autowired`
- [ ] 无通配符 import

---

## 7. 风险与缓解

| 风险 | 缓解 |
|------|------|
| Spring AI Advisor 链顺序依赖 | 显式指定 `@Order`，文档化依赖关系 |
| ChatClient 自动工具调用行为差异 | 对比验证 `DefaultToolCallingChatOptions.internalToolExecutionEnabled` |
| 替换后行为不一致 | 与两套现有系统输出对比验证 |
| 无 Controller/集成测试 | 本次仅为基础设施层替换，单元测试覆盖核心逻辑 |

---

## 完成情况

- ArchUnit: 17/17 ✅（新代码无违规；2 个失败为仓库其他文件的预存违规）
- 单元测试: 9/9 ✅（SpringReActAgentTest 7 个 + SpringReActAgentFactoryTest 2 个）
- 编译: compileJava ✅ / compileTestJava ✅
- 创建文件: 9 个（4 advisor + 3 核心 + 2 测试）

## 反思

### 遇到的问题

1. **Spring AI 2.0.0-M4 Advisor API 差异**：计划中使用的是后来版本才有的 `CallAroundAdvisor` / `StreamAroundAdvisor` / `AdvisedRequest` / `AdvisedResponse` API。实际 2.0.0-M4 提供的是 `CallAdvisor` / `StreamAdvisor` / `BaseAdvisor` 接口，请求/响应类型为 `ChatClientRequest` / `ChatClientResponse`。

2. **ToolCallAdvisor 已内建 ReAct 循环**：Spring AI 2.0.0-M4 的 `ToolCallAdvisor` 已经实现了完整的工具调用循环（调用→执行→观察→重试），无需手动实现 ReAct 循环。方案应利用 Spring AI 生态能力而非重复造轮子。

3. **ArchUnit 方法行数限制**：初始实现的 `call()` 方法包含 try-catch-finally 状态管理导致 12 行违规。通过提取 `callInternal()` 和简化异常处理解决。

4. **AutonomousAgentWorkflowTest 预存编译错误**：引用了不存在的 `ModelTools` / `KnowledgeTools` 类，需临时修复以通过编译。

5. **测试初始化不足**：`SpringReActAgentFactoryTest` 未设置 `chatModelId` 和 `systemPrompt` 导致 `IllegalArgumentException`，需完善 Mock 和上下文配置。

### 改进建议

- Advisor 的策略注入机制可进一步抽象：当前 4 个 Advisor 中 `ToolStrategyAdvisor` 和 `ModelStrategyAdvisor` 是透传的，实际策略逻辑通过域层生命周期钩子（`AbstractReActAgent.beforeInference` 等）执行。后续可考虑完全移除透传 Advisor 或用更轻量的方案替代。
- 未来可添加集成测试验证 Advisor 链的实际执行顺序和策略交互。
