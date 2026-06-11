# ADR: 将四种策略融入 AbstractReActAgent 生命周期

## 背景

当前项目中 `ModelStrategy`、`ToolStrategy`、`RetrievalStrategy`、`GuardrailStrategy` 四种策略仅在 Agent 构建阶段被消费，未完全融入运行时生命周期：

| 策略 | 构建时 | 运行时 | 问题 |
|------|--------|--------|------|
| ModelStrategy | ChatOptions 参数 | 无 | 推理前后无钩子 |
| ToolStrategy | 工具过滤 | 无 | 无并发/超时/重试执行时保障 |
| RetrievalStrategy | 知识库关联 | 无（仅 AgentScopeKnowledge 非正式使用） | 检索前无查询改写、检索后无重排序 |
| GuardrailStrategy | 无 | StrategyUseCase 外部调用 | 未嵌入 Agent 自身生命周期 |

## 决策

采用 **模板方法 + 策略执行器接口** 模式，将四种策略融入 `AbstractReActAgent` 生命周期。

### 架构设计

```
domain/model/strategy/
├── ModelStrategyExecutor.java        # 接口
├── ToolStrategyExecutor.java         # 接口
├── RetrievalStrategyExecutor.java    # 接口
├── GuardrailStrategyExecutor.java    # 接口（新）
└── ValidationResult.java             # 领域值对象（新增）

application/executor/
├── ModelStrategyExecutorImpl.java    # 实现
├── ToolStrategyExecutorImpl.java     # 实现
├── RetrievalStrategyExecutorImpl.java# 实现
└── GuardrailStrategyExecutorImpl.java# 重命名现有类

domain/model/agent/
└── AbstractReActAgent.java           # 添加生命周期钩子

infrastructure/agents/
├── alibaba/AlibabaReActAgent.java    # 调用钩子
├── alibaba/AlibabaReActAgentFactory.java  # 注入执行器
├── aliyun/AgentScopeHarnessAgent.java     # 调用钩子
└── aliyun/AgentScopeHarnessAgentFactory.java # 注入执行器
```

### 生命周期钩子时序

```
call(messages) / streamMessages(messages):
  ┌─────────────────────────────────────────────┐
  │ 1. GuardrailStrategy.validateInput(input)   │
  │ 2. ModelStrategy.beforeInference(messages)  │
  │ 3. RetrievalStrategy.beforeRetrieval(query) │
  ├─────────────────────────────────────────────┤
  │ 4. nativeAgent.call(messages)               │
  │    └─ ToolStrategy.beforeToolCall()         │
  │    └─ ToolStrategy.afterToolCall()          │
  ├─────────────────────────────────────────────┤
  │ 5. RetrievalStrategy.afterRetrieval(results)│
  │ 6. ModelStrategy.afterInference(response)   │
  │ 7. GuardrailStrategy.validateOutput(output) │
  └─────────────────────────────────────────────┘
```

## 实施步骤

### Step 1: Domain Layer — 策略执行器接口与值对象

1. 新增 `domain/model/strategy/ValidationResult.java`
2. 新增 `domain/model/strategy/ModelStrategyExecutor.java`
3. 新增 `domain/model/strategy/ToolStrategyExecutor.java`
4. 新增 `domain/model/strategy/RetrievalStrategyExecutor.java`
5. 新增 `domain/model/strategy/GuardrailStrategyExecutor.java`（领域接口）

### Step 2: AbstractReActAgent — 生命周期钩子

修改 `AbstractReActAgent`：
- 添加四个受保护的执行器字段
- 添加模板方法钩子：`beforeInference()`、`afterInference()`、`beforeToolCall()`、`afterToolCall()`、`beforeRetrieval()`、`afterRetrieval()`

### Step 3: Application Layer — 执行器实现

1. 新增 `ModelStrategyExecutorImpl`
2. 新增 `ToolStrategyExecutorImpl`
3. 新增 `RetrievalStrategyExecutorImpl`
4. 将现有 `GuardrailStrategyExecutor` 适配为领域接口实现

### Step 4: Concrete Implementations — 调用钩子

修改 `AlibabaReActAgent` 和 `AgentScopeHarnessAgent`：
- `call()` 方法中调用模板方法
- `streamMessages()` 方法中调用模板方法

### Step 5: Factory — 注入执行器

修改 `AlibabaReActAgentFactory` 和 `AgentScopeHarnessAgentFactory`：
- 注入四个执行器实现
- 传递给 Agent 构造函数

### Step 6: StrategyUseCase — 适配新接口

修改 `StrategyUseCase` 适配新的 `GuardrailStrategyExecutor` 领域接口。

## 验证

1. `gradle compileJava` 编译通过
2. `gradle test` 所有测试通过
3. ArchUnit 架构测试通过（domain 层无外部依赖）
