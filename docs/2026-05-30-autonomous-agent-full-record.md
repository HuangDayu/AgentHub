# 自主 Agent 能力实现完整记录

> 日期：2026-05-30
> 目标：将 AgentHub 从"配置驱动的执行器"演进为"自主决策的智能体"

---

## 一、项目目标

Agent 在运行时自主决定：
- 使用什么模型
- 使用什么工具
- 查询什么知识库
- 使用什么技能
- 遵循什么工作流程（动态，非静态 DAG）
- 创建多少个子 Agent、什么样的子 Agent

核心原则：**一切都是数据和工具**。Agent 通过工具查询可用资源，通过工具创建子 Agent，通过工具规划执行流程。

---

## 二、架构约束

### 多框架兼容
项目支持两套 Agent 运行时（Spring AI Alibaba + AgentScope Harness），所有改动在 **SystemTools 层** 实现，不侵入框架。

### Agent 构建时序
Agent 对象在创建时注入所有 ToolCallback，之后不可变。动态能力通过工具组合实现，而非运行时重配置。

### 整洁架构
- `domain` — 纯模型，无框架依赖
- `application` — 用例 + 端口接口
- `infrastructure` — 实现端口 + SystemTools

---

## 三、动态工作流 vs 计划引擎

| | 计划引擎 | 动态工作流 |
|--|---------|-----------|
| 本质 | 静态蓝图：先定义所有步骤，再按序执行 | 动态决策：每一步基于当前状态决定下一步 |
| 步骤来源 | Agent 创建时预定义 | Agent 运行时根据中间结果动态生成 |
| 适用场景 | 任务明确、步骤可预见 | 任务模糊、需要探索、结果驱动 |
| 执行方式 | PlanExecutor 自动调用工具 | Agent 的 ReAct 循环本身就是动态工作流 |

**结论**：计划引擎是确定性任务的辅助工具，动态工作流是 Agent 的 ReAct 决策循环。两者互补。

### 服务内 Agent 通信

服务内 Agent 间通信通过 **SubagentManageTools** 实现：
- `createSubagent()` → 创建子 Agent
- `awaitSubagent()` → 等待完成
- `getSubagentResult()` → 获取结果

不需要额外协议。A2A 协议仅用于跨服务 Agent 通信。

---

## 四、已实现的能力

### 4.1 核心 SystemTools（16个）

| 工具 | 功能 |
|------|------|
| PlanTools | 执行计划 CRUD + 自动执行（PlanExecutor） |
| ModelTools | 模型查询 + 能力摘要 + 推荐 |
| ModelSwitchTools | 运行时模型切换（写DB + 驱逐缓存） |
| KnowledgeTools | 知识库查询 + 语义推荐（关键词0.5+文档数0.3+时效0.2） |
| SubagentManageTools | 子 Agent 创建/查询/等待/批量 |
| SkillTools | 技能查询 + 详情 + 执行（SkillRunner） |
| MemoryTools | 记忆搜索/保存/删除 |
| AutomationTools | 定时任务 CRUD + 启停 + Agent绑定 |
| FileTools | 文件读写/编辑/追加/删除/列表/存在检查 |
| RuntimeTools | 命令执行/代码执行/进程管理（安全加固） |
| WebFetchTools | 网页抓取/搜索 |
| RestfulTools | HTTP 接口调用 + RetryTemplate |
| McpDiscoveryTools | MCP 工具平台发现（Smithery + Glama） |

### 4.2 自主决策能力

| 能力 | 实现 | 状态 |
|------|------|------|
| 动态系统提示词注入 | SystemPromptBuilderUseCase | ✅ |
| 计划自动执行 | PlanExecutor + resolveByName | ✅ |
| 工具策略过滤 | ToolFilterUseCase + AgentContextUseCase | ✅ |
| 模型运行时切换 | ModelSwitchTools + AgentPoolUseCase.evict | ✅ |
| 技能执行 | SkillRunner + SKILL.md 解析 | ✅ |
| 知识库语义推荐 | 评分公式 | ✅ |
| 定时任务-Agent绑定 | agentId 字段 + 精确匹配 | ✅ |

### 4.3 代码审查修复

| 严重度 | 修复内容 |
|--------|---------|
| CRITICAL ×4 | PlanExecutor 无限循环、PlanTools.addStep 重复计划、RuntimeTools 流死锁、命令注入 |
| HIGH ×8 | 资源泄漏、线程阻塞、竞态条件、静态状态、步骤状态错误、模型切换无反馈 |
| MEDIUM ×10 | 名称匹配、空指针、步骤顺序、进程清理、null 返回、N+1 查询 |

---

## 五、文件清单

### 新增文件（30+个）

```
domain/model/plan/
├── ExecutionPlan.java
├── PlanStep.java
├── PlanStatus.java
└── PlanStepStatus.java

application/usecase/
├── ExecutionPlanUseCase.java
├── SystemPromptBuilderUseCase.java
├── ToolFilterUseCase.java

application/command/
├── CreatePlanCommand.java
└── PlanStepInput.java

application/dto/
├── ExecutionPlanOutput.java
└── PlanStepOutput.java

application/port/out/
├── repositories/ExecutionPlanRepository.java
└── tools/ToolCallbackResolverPort.java

infrastructure/store/db/
├── entity/ExecutionPlanEntity.java
├── entity/PlanStepEntity.java
├── mapper/ExecutionPlanMybatisMapper.java
├── mapper/PlanStepMybatisMapper.java
└── repository/MybatisExecutionPlanRepository.java

infrastructure/tools/system_tools/core_tools/
├── PlanExecutor.java
├── SkillRunner.java
├── dto/ModelCapabilitySummary.java
├── dto/ModelRecommendation.java
├── dto/KnowledgeBaseSummary.java
├── dto/KnowledgeBaseRecommendation.java
├── dto/ScheduledTaskResult.java
├── dto/SkillDetailDTO.java
├── dto/SkillExecutionResult.java
├── dto/ModelSwitchResult.java
├── dto/HttpToolResult.java
└── dto/RestfulToolDTO.java

infrastructure/tools/mcp_tools/platform/
├── McpPlatform.java
├── McpToolInfo.java
├── McpPlatformClient.java
├── McpPlatformRegistry.java
├── McpPlatformConfig.java
├── SmitheryPlatformClient.java
├── GlamaPlatformClient.java
└── McpDiscoveryTools.java

sql/V2__add_execution_plan_tables.sql
```

### 修改文件（15+个）

```
AgentContextUseCase.java — 动态提示词 + 工具策略
AgentPoolUseCase.java — evict 方法
ScheduledTaskRepository.java — findAllEnabled
ToolCallbackResolverPort.java — resolveByName
AgentToolsFactory.java — 实现 resolveByName
ModelTools.java — 推荐逻辑
KnowledgeTools.java — 语义推荐
MemoryTools.java — 搜索 + 删除
RuntimeTools.java — 安全性
FileTools.java — 完善
SkillTools.java — 执行
AutomationTools.java — 调度器集成
SubagentManageTools.java — await/batch
RestfulTools.java — RetryTemplate
ScheduledTaskEntity.java — 新增字段
MybatisScheduledTaskRepository.java — 映射
ScheduledTaskScheduler.java — agentId 绑定
ScheduledTask.java — agentId/lastRunResult/runCount
schema.sql — scheduled_task 新增列 + 新表
```

---

## 六、系统提示词 v3.0

```
你是 {agentName}。

# 你是什么
你是一个能自主完成复杂任务的 Agent。

# 你怎么工作
1. 理解任务 → 2. 评估需要什么资源 → 3. 做计划 → 4. 逐步执行 → 5. 完成汇报

# 你能做什么
- 获取信息（知识库、外部数据、数据库、文件）
- 选择模型（getModelCapabilities / recommendModel）
- 执行计划（createPlan → startExecution → 循环 → completePlan）
- 并行处理（createSubagent / awaitSubagent）
- 定时执行（createScheduledTask）
- 调用 HTTP 接口（invokeHttpTool / callHttp）
- 发现 MCP 工具（searchTools）
- 记忆管理（memorySave / memorySearch）

# 你必须遵守的规则
- 不要猜测，先确认再行动
- 每个步骤执行后立即更新计划状态
- 工具调用失败时分析原因，能重试则重试
- 不要手动做能工具化的事
```

---

## 七、测试结果

| 测试类型 | 结果 |
|---------|------|
| `gradle clean compileJava` | ✅ 通过 |
| `gradle test --tests "*AgentHubCleanArchitectureTest*"` | ✅ 15/15 通过 |

---

## 八、技术方案文档

| 文档 | 内容 |
|------|------|
| `2026-05-30-autonomous-agent-technical-design.md` | 初始技术方案 |
| `2026-05-30-autonomous-agent-capabilities-plan.md` | 能力完善方案 |
| `2026-05-30-fix-and-a2a-plan.md` | 修复方案 |
| `2026-05-30-autonomous-agent-implementation-summary.md` | 实现总结 |
| `2026-05-30-code-review-fix-plan.md` | 代码审查修复方案 |
| `2026-05-30-autonomous-agent-system-prompt.md` | 系统提示词 v3.0 |
| `2026-05-30-autonomous-agent-capabilities-plan.md` | 能力完善方案 |

---

## 九、后续可增强方向

| 方向 | 说明 | 优先级 |
|------|------|--------|
| 执行可观测性 | Agent 决策链记录到 trace | P1 |
| 安全护栏 | 危险操作前确认 | P1 |
| 记忆持久化 | MemoryTools 内存存储重启丢失 | P1 |
| 子Agent结果聚合 | 批量创建后自动收集 | P2 |
| Agent能力自描述 | 自动写入 AgentCard | P2 |
| 执行回放 | 决策链回放分析 | P3 |
| 跨服务 A2A 协议 | 官方 A2A 规范对齐 | P3 |
