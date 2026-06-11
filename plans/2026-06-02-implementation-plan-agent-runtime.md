# Agent Runtime 全能力实现计划

## 1. 当前架构分析

### 已有能力
| 能力 | 状态 | 位置 |
|------|------|------|
| 核心执行循环 | ✅ 已有 | `AbstractReActAgent` (domain) |
| 双框架支持 | ✅ 已有 | Alibaba + AgentScope (infrastructure) |
| 策略模型 | ✅ 已有 | `domain/model/strategy/` |
| 工具管理 | ⚠️ 分散 | Factory 中管理，无统一注册中心 |
| 知识检索 | ⚠️ 基础 | AgentScopeKnowledge，无管道 |
| 多 Agent | ✅ 已有 | `AbstractTeamAgent` + TeamAgentFactory |
| 流式响应 | ✅ 已有 | `Flux<AgentMessage>` |
| 护栏策略 | ✅ 已有 | `GuardrailStrategy` |
| 会话持久化 | ⚠️ 基础 | ChatMessage + Session，无检查点 |
| 子 Agent | ✅ 已有 | SubagentEngine |

### 缺失能力
| 能力 | 优先级 | 复杂度 |
|------|--------|--------|
| 统一 Agent 接口 | P0 | 低 |
| ToolRegistry + ToolExecutor | P0 | 中 |
| 分层记忆系统 | P1 | 高 |
| RetrievalPipeline | P1 | 中 |
| SessionManager + Checkpoint | P1 | 中 |
| AgentTracer + AgentMetrics | P2 | 中 |
| ErrorHandlingStrategy | P2 | 低 |
| HumanInTheLoopManager | P3 | 高 |

---

## 2. 实施路线图

### Phase 1: 核心基础 (2周)

#### 1.1 统一 Agent 接口
**目标**: 在 domain 层定义统一的 Agent 能力接口，不依赖具体实现

**新增文件**:
```
domain/model/agent/
├── Agent.java                    (接口，替代抽象类的部分职责)
├── AgentCapabilities.java        (能力描述)
├── AgentRequest.java             (请求对象)
├── AgentResponse.java            (响应对象)
├── AgentEvent.java               (流式事件)
├── AgentState.java               (状态枚举，扩展 AgentLifecycleState)
```

**修改文件**:
- `AbstractReActAgent.java`: 实现 `Agent` 接口
- `AbstractTeamAgent.java`: 实现 `Agent` 接口

#### 1.2 ToolRegistry + ToolExecutor
**目标**: 统一管理工具注册、执行、重试、超时

**新增文件**:
```
domain/model/tool/
├── ToolDefinition.java           (工具定义，包装 Spring AI ToolDefinition)
├── ToolCallback.java             (工具回调接口)
├── ToolCallResult.java           (调用结果)
├── ToolRegistry.java             (注册中心接口)
├── ToolExecutor.java             (执行器接口)

infrastructure/tools/
├── DefaultToolRegistry.java      (实现)
├── DefaultToolExecutor.java      (实现，带重试/超时)
```

#### 1.3 错误处理
**目标**: 统一错误处理策略

**新增文件**:
```
domain/model/agent/
├── ErrorHandlingStrategy.java    (策略接口)
├── RetryConfig.java              (重试配置)

infrastructure/agents/
├── DefaultErrorHandlingStrategy.java
```

---

### Phase 2: 记忆与检索 (2周)

#### 2.1 分层记忆系统
**目标**: 支持短期、工作、长期记忆

**新增文件**:
```
domain/model/memory/
├── MemoryManager.java            (记忆管理器接口)
├── MemoryConfig.java             (记忆配置)
├── MemorySnapshot.java           (记忆快照，用于导出/导入)
├── WorkingMemory.java            (工作记忆接口)

infrastructure/memory/
├── HierarchicalMemoryManager.java (分层记忆实现)
├── InMemoryWorkingMemory.java     (工作内存实现)
```

**修改文件**:
- `ReActAgentContext.java`: 添加 `MemoryManager` 字段

#### 2.2 RetrievalPipeline
**目标**: 可组合的检索处理链

**新增文件**:
```
domain/model/rag/
├── RetrievalPipeline.java        (检索管道接口)
├── RetrievalConfig.java          (检索配置)
├── QueryRewriter.java            (查询改写接口)
├── DocumentReranker.java         (重排序接口)
├── DocumentFilter.java           (过滤接口)

infrastructure/rag/
├── DefaultRetrievalPipeline.java (实现)
├── DefaultQueryRewriter.java     (实现)
```

**修改文件**:
- `ReActAgentContext.java`: 添加 `RetrievalPipeline` 字段

---

### Phase 3: 会话与可观测 (2周)

#### 3.1 SessionManager + Checkpoint
**目标**: 会话管理与检查点恢复

**新增文件**:
```
domain/model/session/
├── SessionManager.java           (会话管理器接口)
├── Session.java                  (会话对象，扩展现有)
├── Checkpoint.java               (检查点)
├── CheckpointSaver.java          (检查点保存接口)

infrastructure/session/
├── PostgresSessionManager.java   (实现)
├── PostgresCheckpointSaver.java  (实现)
```

#### 3.2 AgentTracer + AgentMetrics
**目标**: 执行追踪与指标收集

**新增文件**:
```
domain/model/observability/
├── AgentTracer.java              (追踪器接口)
├── TraceContext.java             (追踪上下文)
├── TraceResult.java             (追踪结果)
├── AgentMetrics.java            (指标接口)

infrastructure/telemetry/
├── MicrometerAgentMetrics.java   (Micrometer 实现)
├── LoggingAgentTracer.java       (日志追踪实现)
```

---

### Phase 4: 人机协作 (1周)

#### 4.1 HumanInTheLoopManager
**目标**: 支持人工审批工作流

**新增文件**:
```
domain/model/collaboration/
├── HumanInTheLoopManager.java    (管理器接口)
├── ApprovalRequest.java          (审批请求)
├── ApprovalResult.java           (审批结果)
├── PendingApproval.java          (待审批项)

infrastructure/collaboration/
├── DefaultHumanInTheLoopManager.java
```

---

### Phase 5: 策略增强 (1周)

#### 5.1 策略注册中心
**目标**: 统一管理所有策略

**新增文件**:
```
domain/model/strategy/
├── AgentStrategy.java            (策略基础接口)
├── StrategyRegistry.java         (注册中心接口)
├── ExecutionStrategy.java        (执行策略)
├── MemoryStrategy.java           (记忆策略)
├── TeamStrategy.java             (团队策略)

infrastructure/strategy/
├── DefaultStrategyRegistry.java
```

#### 5.2 策略与 Agent 集成
**修改文件**:
- `AbstractReActAgent.java`: 注入策略，调用策略钩子
- `ReActAgentContext.java`: 添加策略列表

---

## 3. 关键设计决策

### 3.1 Agent 接口 vs 抽象类
**决策**: 保留 `AbstractReActAgent` 作为实现基类，新增 `Agent` 接口作为契约

**理由**:
- 符合 Clean Architecture（接口在 domain，实现在 infrastructure）
- 保持向后兼容（现有代码无需大改）
- 支持多态（可以有不同实现）

### 3.2 工具管理位置
**决策**: ToolRegistry 在 infrastructure 层，Agent 通过接口使用

**理由**:
- 工具注册涉及 Spring AI，属于基础设施
- Agent 只需知道如何调用工具，不需知道注册细节
- 支持动态工具注册/注销

### 3.3 记忆系统
**决策**: 三层记忆（短期/工作/长期），通过 MemoryManager 统一管理

**理由**:
- 短期记忆：对话历史（Spring AI ChatMemory）
- 工作记忆：当前任务上下文（内存）
- 长期记忆：持久化知识（向量存储）

### 3.4 检索管道
**决策**: 可组合的管道模式，支持查询改写、检索、重排序、过滤

**理由**:
- 灵活性：可以根据场景组合不同组件
- 可测试性：每个组件独立测试
- 可扩展性：容易添加新组件

---

## 4. 依赖关系

```
Phase 1 (核心基础)
    │
    ├──> Phase 2 (记忆与检索)
    │        │
    │        └──> Phase 3 (会话与可观测)
    │                 │
    │                 └──> Phase 4 (人机协作)
    │
    └──> Phase 5 (策略增强)
```

---

## 5. 风险与缓解

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 破坏现有 API | 高 | 保留现有接口，新增接口作为补充 |
| 性能下降 | 中 | 记忆系统支持懒加载，检索支持缓存 |
| 复杂度增加 | 中 | 清晰的模块划分，完整的单元测试 |
| Spring AI 版本兼容 | 低 | 使用 BOM 管理版本，定期更新 |

---

## 6. 验收标准

### Phase 1
- [ ] Agent 接口定义完成
- [ ] ToolRegistry 支持动态注册
- [ ] ToolExecutor 支持重试/超时
- [ ] 单元测试覆盖率 > 80%

### Phase 2
- [ ] 三层记忆系统工作正常
- [ ] RetrievalPipeline 可组合
- [ ] 记忆压缩/摘要功能
- [ ] 集成测试通过

### Phase 3
- [ ] 会话持久化到数据库
- [ ] 检查点创建/恢复正常
- [ ] 追踪日志格式统一
- [ ] 指标可查询

### Phase 4
- [ ] 审批工作流完整
- [ ] 超时自动拒绝
- [ ] 与 Agent 执行集成

### Phase 5
- [ ] 策略注册/查找正常
- [ ] 策略钩子按顺序执行
- [ ] 策略可动态启用/禁用

---

## 7. 文件清单

### 新增文件 (约 40 个)

**Domain Layer** (~20 个):
- `domain/model/agent/Agent.java`
- `domain/model/agent/AgentCapabilities.java`
- `domain/model/agent/AgentRequest.java`
- `domain/model/agent/AgentResponse.java`
- `domain/model/agent/AgentEvent.java`
- `domain/model/agent/ErrorHandlingStrategy.java`
- `domain/model/agent/RetryConfig.java`
- `domain/model/tool/ToolDefinition.java`
- `domain/model/tool/ToolCallback.java`
- `domain/model/tool/ToolCallResult.java`
- `domain/model/tool/ToolRegistry.java`
- `domain/model/tool/ToolExecutor.java`
- `domain/model/memory/MemoryManager.java`
- `domain/model/memory/MemoryConfig.java`
- `domain/model/memory/MemorySnapshot.java`
- `domain/model/memory/WorkingMemory.java`
- `domain/model/rag/RetrievalPipeline.java`
- `domain/model/rag/RetrievalConfig.java`
- `domain/model/rag/QueryRewriter.java`
- `domain/model/rag/DocumentReranker.java`
- `domain/model/rag/DocumentFilter.java`
- `domain/model/session/SessionManager.java`
- `domain/model/session/Checkpoint.java`
- `domain/model/session/CheckpointSaver.java`
- `domain/model/observability/AgentTracer.java`
- `domain/model/observability/TraceContext.java`
- `domain/model/observability/TraceResult.java`
- `domain/model/observability/AgentMetrics.java`
- `domain/model/collaboration/HumanInTheLoopManager.java`
- `domain/model/collaboration/ApprovalRequest.java`
- `domain/model/collaboration/ApprovalResult.java`
- `domain/model/collaboration/PendingApproval.java`
- `domain/model/strategy/AgentStrategy.java`
- `domain/model/strategy/StrategyRegistry.java`
- `domain/model/strategy/ExecutionStrategy.java`
- `domain/model/strategy/MemoryStrategy.java`
- `domain/model/strategy/TeamStrategy.java`

**Infrastructure Layer** (~15 个):
- `infrastructure/tools/DefaultToolRegistry.java`
- `infrastructure/tools/DefaultToolExecutor.java`
- `infrastructure/memory/HierarchicalMemoryManager.java`
- `infrastructure/memory/InMemoryWorkingMemory.java`
- `infrastructure/rag/DefaultRetrievalPipeline.java`
- `infrastructure/rag/DefaultQueryRewriter.java`
- `infrastructure/session/PostgresSessionManager.java`
- `infrastructure/session/PostgresCheckpointSaver.java`
- `infrastructure/telemetry/MicrometerAgentMetrics.java`
- `infrastructure/telemetry/LoggingAgentTracer.java`
- `infrastructure/collaboration/DefaultHumanInTheLoopManager.java`
- `infrastructure/strategy/DefaultStrategyRegistry.java`
- `infrastructure/agents/DefaultErrorHandlingStrategy.java`

### 修改文件 (~10 个):
- `domain/model/agent/AbstractReActAgent.java`
- `domain/model/agent/AbstractTeamAgent.java`
- `domain/model/agent/ReActAgentContext.java`
- `infrastructure/agents/alibaba/AlibabaReActAgentFactory.java`
- `infrastructure/agents/aliyun/AgentScopeHarnessAgentFactory.java`
- `infrastructure/agents/alibaba/hook/AgentHookFactory.java`
- `infrastructure/agents/alibaba/interceptor/InterceptorFactory.java`
- `application/usecase/StrategyUseCase.java`
- `application/usecase/RagChatUseCase.java`
