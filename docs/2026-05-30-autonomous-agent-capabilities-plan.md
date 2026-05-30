# 自主 Agent 能力完善技术方案

> 日期：2026-05-30
> 目标：实现 Agent 自主决策所需的全部能力

---

## 总览

| Phase | 能力 | 优先级 | 涉及文件 |
|-------|------|--------|---------|
| 1 | 动态系统提示词注入 | P0 | AgentContextUseCase, SystemPromptBuilder |
| 1 | 计划自动执行引擎 | P0 | PlanExecutor, PlanTools |
| 1 | 运行时模型切换 | P1 | ModelSwitchTools, AgentPoolUseCase |
| 2 | 技能执行 | P1 | SkillRunner, SkillTools |
| 2 | 工具动态绑定 | P1 | ToolStrategyApplier |
| 2 | 知识库语义推荐 | P2 | KnowledgeTools |
| 3 | 任务-Agent绑定 + 日志 | P2 | ScheduledTask, ExecutionLog |

---

## Phase 1: 动态系统提示词注入

### 目标
Agent 创建时，系统提示词自动注入可用资源清单，Agent 无需手动发现。

### 方案

**新增类**：`SystemPromptBuilder`

```
application/usecase/SystemPromptBuilder.java
```

职责：
1. 收集 Agent 的 AgentConfig 中所有 TOOL 类型配置 → 生成工具列表
2. 收集所有 MODEL 类型配置 → 生成模型列表
3. 收集所有 KNOWLEDGE 类型配置 → 生成知识库列表
4. 收集 TOOL 类型中的 SYSTEM_TOOL → 生成系统工具列表
5. 拼接到静态系统提示词末尾

**修改**：`AgentContextUseCase.resolveSystemPrompt()`

```
修改前：直接返回 PromptTemplateInfo.content
修改后：调用 SystemPromptBuilder.build(agentConfigs) 追加资源摘要
```

**注入格式**：
```
## 你当前可用的资源

### 工具（N个）
- webSearch: 互联网搜索
- read: 读取文件
- ...

### 模型（N个）
- gpt-4o (OPENAI) - 通用，HIGH成本
- ...

### 知识库（N个）
- 产品文档: 描述...
- ...
```

---

## Phase 1: 计划自动执行引擎

### 目标
PlanTools 创建的计划能自动执行，不需要 Agent 手动逐步调用。

### 方案

**新增类**：`PlanExecutor`

```
infrastructure/tools/system_tools/core_tools/PlanExecutor.java
```

职责：
1. 接收 planId
2. 循环调用 getNextSteps
3. 对每个步骤，根据 toolName 自动查找并调用对应的 ToolCallback
4. 更新步骤状态
5. 所有步骤完成后自动 completePlan

**修改**：`PlanTools` 新增方法

```
executePlan(planId) — 启动自动执行循环
```

**执行逻辑**：
```
while (hasNextSteps) {
    steps = getNextSteps(planId)
    for (step : steps) {
        updateStep(planId, stepId, "RUNNING")
        result = toolCallbackResolver.resolveByName(step.toolName).call(step.toolInput)
        updateStep(planId, stepId, "COMPLETED", result)
    }
}
completePlan(planId, summary)
```

**工具解析**：
- `ToolCallbackResolverPort` 新增 `resolveByName(String name)` 方法
- 按名称从已注册的 ToolCallback 中查找

---

## Phase 1: 运行时模型切换

### 目标
Agent 可以在对话中动态切换模型（用于子 Agent 或下一轮对话）。

### 方案

**新增类**：`ModelSwitchTools`

```
infrastructure/tools/system_tools/core_tools/ModelSwitchTools.java
```

方法：
```
switchModel(modelConfigId) — 切换当前会话的模型
getCurrentModel() — 获取当前使用的模型信息
```

**原理**：
- 切换模型不需要改变当前 Agent 对象
- 通过创建新的子 Agent 实现模型切换（子 Agent 用新模型）
- 或者通过 AgentPoolUseCase 刷新缓存，下次创建时用新模型

**实际实现**：
- `switchModel` 将新的 modelConfigId 写入当前会话的 ReActAgentContext
- AgentPoolUseCase 移除当前会话的缓存
- 下一轮对话时自动用新模型重建 Agent

---

## Phase 2: 技能执行

### 目标
Agent 能执行技能（SKILL.md 定义的流程），而不仅仅是读取。

### 方案

**新增类**：`SkillRunner`

```
infrastructure/tools/system_tools/core_tools/SkillRunner.java
```

职责：
1. 读取 SKILL.md 解析执行步骤
2. 解析 steps YAML/JSON 定义
3. 按步骤调用对应的工具
4. 返回执行结果

**修改**：`SkillTools` 新增方法

```
executeSkill(skillId, parameters) — 执行技能
```

**SKILL.md 扩展格式**：
```yaml
name: search-web
description: 搜索互联网
steps:
  - tool: webSearch
    input: "${query}"
  - tool: read
    input: "${result.url}"
    condition: "${result.hasContent}"
```

---

## Phase 2: 工具动态绑定

### 目标
ToolStrategy 中的 toolBindings 在运行时生效，动态过滤可用工具。

### 方案

**新增类**：`ToolStrategyApplier`

```
infrastructure/tools/system_tools/core_tools/ToolStrategyApplier.java
```

职责：
1. 读取 ToolStrategy.toolBindings
2. 按 priority 和 enabled 过滤工具列表
3. 返回过滤后的 ToolCallback 集合

**修改**：`AgentContextUseCase.resolveToolCallbacks()`

```
修改前：直接返回所有工具
修改后：调用 ToolStrategyApplier.apply(toolCallbacks, toolStrategy) 过滤
```

---

## Phase 2: 知识库语义推荐

### 目标
recommendKnowledgeBase 使用语义相似度而非字符串匹配。

### 方案

**修改**：`KnowledgeTools.toRecommendation()`

实现方式：
1. 计算 topic 与每个 KB 的 name + description 的词重叠率
2. 考虑文档数量（文档多的 KB 可能覆盖更广）
3. 考虑最近更新时间（更新频繁的 KB 可能更活跃）

评分公式：
```
score = keywordMatch * 0.5 + docCountScore * 0.3 + freshnessScore * 0.2
```

---

## Phase 3: 任务-Agent 绑定 + 日志

### 目标
定时任务指定执行哪个 Agent，执行结果记录日志。

### 方案

**修改**：`ScheduledTask` 领域模型

```
新增字段：agentId — 指定执行的 Agent ID
新增字段：lastRunResult — 最近一次执行结果
新增字段：runCount — 累计执行次数
```

**修改**：`ScheduledTaskScheduler.executeTask()`

```
修改前：findAll().stream().filter(workspace).findFirst()
修改后：findById(agentId) 精确匹配
```

**新增**：`ScheduledTaskRunLog` 领域模型

```
domain/model/ScheduledTaskRunLog.java
字段：id, taskId, agentId, sessionId, startTime, endTime, status, result
```

---

## 文件变更清单

### 新增文件

```
application/usecase/SystemPromptBuilder.java
infrastructure/tools/system_tools/core_tools/PlanExecutor.java
infrastructure/tools/system_tools/core_tools/ModelSwitchTools.java
infrastructure/tools/system_tools/core_tools/SkillRunner.java
infrastructure/tools/system_tools/core_tools/ToolStrategyApplier.java
domain/model/ScheduledTaskRunLog.java
application/port/out/repositories/ScheduledTaskRunLogRepository.java
infrastructure/store/db/entity/ScheduledTaskRunLogEntity.java
infrastructure/store/db/mapper/ScheduledTaskRunLogMybatisMapper.java
infrastructure/store/db/repository/MybatisScheduledTaskRunLogRepository.java
```

### 修改文件

```
application/usecase/AgentContextUseCase.java — 动态提示词注入
infrastructure/tools/system_tools/core_tools/PlanTools.java — executePlan
infrastructure/tools/system_tools/core_tools/SkillTools.java — executeSkill
infrastructure/tools/system_tools/core_tools/KnowledgeTools.java — 语义推荐
application/port/out/tools/ToolCallbackResolverPort.java — resolveByName
infrastructure/tools/AgentToolsFactory.java — 实现 resolveByName
domain/model/ScheduledTask.java — agentId 字段
infrastructure/scheduler/ScheduledTaskScheduler.java — agentId 绑定
sql/schema.sql — 新增表
```

---

## 实施顺序

```
1. SystemPromptBuilder + AgentContextUseCase 修改
2. PlanExecutor + PlanTools 修改 + ToolCallbackResolverPort 修改
3. ModelSwitchTools
4. SkillRunner + SkillTools 修改
5. ToolStrategyApplier + AgentContextUseCase 修改
6. KnowledgeTools 语义推荐修改
7. ScheduledTask agentId + Scheduler 修改 + RunLog 表
8. 编译验证 + ArchUnit 测试
```
