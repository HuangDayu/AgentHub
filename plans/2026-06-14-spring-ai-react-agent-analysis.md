# 现有 Agent 运行时框架分析报告

> 日期：2026-06-14 · 分析目标：alibaba / aliyun 两套 Agent 运行时

---

## 1. 概览对比

| 维度 | alibaba (Spring AI Alibaba) | aliyun (AgentScope Harness) |
|------|----------------------------|----------------------------|
| 文件数 | 15 | 24 |
| 子包数 | 6（根/saver/hook/interceptor/tools/store） | 8（根/subagent/session/knowledge/hook/tools/filesystem/workspace/memory/model） |
| 底层引擎 | `com.alibaba.cloud.ai.graph.ReactAgent` | `io.agentscope.harness.agent.HarnessAgent` |
| 端口实现 | `ReActAgentFactory` + `TeamAgentFactory` | `ReActAgentFactory` + `TeamAgentFactory` + `ModelPoolManagerPort` |
| 测试覆盖 | 0 | 0 |
| 代码行数 | ~1350 | ~1400 |

---

## 2. alibaba 实现分析

### 2.1 优点

1. **Hook/Interceptor 机制完备**：Agent 生命周期钩子（`LoggingHook`, `ModelStrategyHook`）和工具拦截器（`ToolMonitoringInterceptor`, `ToolStrategyInterceptor`）分工清晰
2. **Graph 工具集成**：内置 `GrepSearchTool`、`GlobSearchTool`、`WriteTodosTool`，代码搜索/文件搜索能力强
3. **PostgreSQL 检查点持久化**：`AgentHubPostgresSaver` 支持线程/检查点持久化，实现对话中断恢复
4. **架构合规性好**：方法长度、参数数量、无 `@Autowired`、无通配符导入等均合规
5. **团队编排完整**：5 种团队模式（SUB_AGENT/SUPERVISOR/ROUTING/PARALLEL/SEQUENTIAL）全部实现

### 2.2 缺点

1. **双抽象层**：`AbstractReActAgent` → `ReactAgent` → `ChatModel`，中间层 `ReactAgent` 仅做转发，无业务增值
2. **Hook + Interceptor 两套机制**：同一件事（生命周期拦截）需学习两套 API，增加复杂度
3. **Config 对象污染**：`AlibabaReActAgentConfig` 将领域模型（`Agent`）与框架类型（`ChatModel`, `ToolCallback`, `Hook`, `RunnableConfig`）混装，违反接口隔离
4. **框架锁死**：依赖 `spring-ai-alibaba-agent-framework`，无法轻易切换 LLM 调用方式
5. **AgentHubPostgresSaver 577 行**：JDBC 样板代码过大，与项目"方法≤10行"风格冲突
6. **`ObjectProvider<TeamAgentFactory>`**：循环依赖是设计缺陷的外在表现

---

## 3. aliyun (AgentScope) 实现分析

### 3.1 优点

1. **Model 工厂体系完善**：`AgentScopeModelFactoryRegistry` + 4 个 Provider 工厂（OpenAI/DeepSeek/OpenRouter/Ollama），支持多供应商模型热切换
2. **两层模型解析**：优先使用 AgentScope 原生 Model，失败时回退到 Spring AI `ChatModel` → `AgentScopeSpringModelAdapter` 桥接，容错性好
3. **Hook 模型统一**：AgentScope 使用单一 `Hook.onEvent(HookEvent)` 事件模型，比 Alibaba 的 Hook+Interceptor 更简洁
4. **RAG/知识库集成**：`AgentScopeKnowledge` 通过应用层端口（`RagVectorSearchPort`, `EtlDocumentChunkStorePort`）实现检索增强
5. **架构分离度高**：24 个文件分布在 8 个子包，职责单一，符合 SRP

### 3.2 缺点

1. **抽象泄露最明显**：`AgentScopeSpringModelAdapter` 的存在证明框架抽象与 Spring AI 不兼容
2. **`AgentScopeTeamAgent.streamMessages()` 返回 `Flux.empty()`**：团队流式能力是空实现，半成品
3. **碎片化严重**：24 个文件、8 个子包，大量工厂类（SessionFactory/MemoryConfigFactory/FilesystemFactory/WorkspaceManagerFactory）功能极简，过度工程
4. **`AgentScopeModelFactoryRegistry.testModel()` 29 行**：违反方法 ≤10 行规则
5. **通配符导入**：`import io.agentscope.core.message.*`
6. **无任何测试**：0 个测试文件，违反 TDD 原则
7. **依赖 `agentscope-harness`**：额外第三方框架依赖，增加构建体积和漏洞面

---

## 4. 共性问题

```
                    ┌──────────────────────────────────────────────┐
                    │              核心问题：双重抽象                │
                    │                                              │
                    │  User Request                                 │
                    │    ↓                                         │
                    │  AbstractReActAgent  (domain)                │
                    │    ↓  (业务价值：生命周期钩子, 护栏检查)       │
                    │  ReactAgent / HarnessAgent  (框架)           │
                    │    ↓  (转发/执行工具调用)                     │
                    │  ChatModel / Model  (框架)                   │
                    │    ↓                                         │
                    │  LLM                                         │
                    └──────────────────────────────────────────────┘
```

| 问题 | 描述 | 涉及 |
|------|------|------|
| **双重抽象** | 框架 Agent 层（ReactAgent/HarnessAgent）与领域 AbstractReActAgent 功能重叠 | 两者 |
| **抽象泄露** | 领域层引用 `Flux`（`AbstractReActAgent`, `AbstractTeamAgent`） | domain 违规 |
| **`@Builder` 污染** | `ReActAgentContext`, `AgentToolInfo`, `ReActAgentWorkspace` 使用 `@Builder` | 违反项目规范 |
| **领域层框架依赖** | `AgentMessage` 导入 `org.springframework.ai.*` | 违反整洁架构 |
| **Spring AI 未充分利用** | `ChatClient` + `Advisor` 已支持组合式拦截，但项目未用 | 两者 |
| **零测试覆盖** | 两套运行时均无测试 | 两者 |

---

## 5. 跟 Spring AI 原生方案对比

| 能力 | alibaba | aliyun | Spring AI 原生（目标） |
|------|---------|--------|---------------------|
| 模型调用 | `ReactAgent` | `HarnessAgent` → `Model`/`ChatModel` | `ChatClient` |
| 生命周期拦截 | `Hook` + `Interceptor` | `Hook.onEvent()` | `Advisor`（统一） |
| 工具执行 | `ToolCallback`（同 Spring AI） | `SpringToolAdapter` → `AgentTool` ↔ `ToolCallback` | `ToolCallback`（原生） |
| 对话记忆 | `MessageChatMemoryAdvisor` | `Memory`（AgentScope） | `ChatMemory` + `Advisor` |
| 状态持久化 | `AgentHubPostgresSaver` | 无 | 可选的 `ChatMemory` 持久化 |
| 团队协作 | 5 种编排模式 | stub（未完成） | 统一在 UseCase 层编排 |

**结论**：Spring AI 原生 `ChatClient` + `Advisor` 可直接替代两套框架的 Agent 运行层，消除重复抽象和适配代码。

---

## 6. 建议方向

1. **去双重抽象**：直接用 `ChatClient` 替代 `ReactAgent`/`HarnessAgent`，消除框架中间层
2. **策略即 Advisor**：`ModelStrategy`/`ToolStrategy`/`GuardrailStrategy`/`RetrievalStrategy` 全部统一为 Spring AI `Advisor`
3. **去适配层**：不需 `AgentScopeSpringModelAdapter`、`SpringToolAdapter` 等桥接类
4. **复用 Spring AI 原生能力**：`ChatMemory`、`ToolCallback` 直接使用，不需二次包装
5. **单包结构**：`com.agenthub.infrastructure.agents.spring` 控制在 6-8 个文件，消灭碎片化
