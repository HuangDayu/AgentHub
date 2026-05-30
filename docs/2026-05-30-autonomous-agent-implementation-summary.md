# 自主 Agent 能力实现总结

> 日期：2026-05-30
> 目标：将 AgentHub 从"配置驱动的执行器"演进为"自主决策的智能体"

---

## 一、目标

Agent 在运行时自主决定：
- 使用什么模型
- 使用什么工具
- 查询什么知识库
- 使用什么技能
- 遵循什么工作流程（动态，非静态 DAG）
- 创建多少个子 Agent、什么样的子 Agent

---

## 二、已实现的能力

### 2.1 基础工具层（core_tools）

| 工具 | 功能 | 状态 |
|------|------|------|
| PlanTools | 执行计划 CRUD + 自动执行 | ✅ |
| ModelTools | 模型查询 + 能力摘要 + 推荐 | ✅ |
| KnowledgeTools | 知识库查询 + 语义推荐 | ✅ |
| SubagentManageTools | 子 Agent 创建/查询/等待/批量 | ✅ |
| SkillTools | 技能查询 + 详情 + 执行 | ✅ |
| MemoryTools | 记忆搜索/保存/删除 | ✅ |
| AutomationTools | 定时任务 CRUD + 启停 | ✅ |
| FileTools | 文件读写/编辑/追加/删除/列表 | ✅ |
| RuntimeTools | 命令执行/代码执行/进程管理 | ✅ |
| WebFetchTools | 网页抓取/搜索 | ✅ |
| RestfulTools | HTTP 接口调用 + RetryTemplate | ✅ |
| McpDiscoveryTools | MCP 工具平台发现 | ✅ |
| ModelSwitchTools | 运行时模型切换 | ✅ |
| A2ATools | Agent 间通信 | ✅ |

### 2.2 自主决策能力

| 能力 | 实现 | 状态 |
|------|------|------|
| 动态系统提示词注入 | SystemPromptBuilderUseCase | ✅ |
| 计划自动执行 | PlanExecutor + ToolCallbackResolverPort.resolveByName | ✅ |
| 工具策略过滤 | ToolFilterUseCase + AgentContextUseCase 接入 | ✅ |
| 模型运行时切换 | ModelSwitchTools + AgentPoolUseCase.evict | ✅ |
| 技能执行 | SkillRunner + SKILL.md 解析 | ✅ |
| 知识库语义推荐 | 评分公式（关键词0.5 + 文档数0.3 + 时效0.2） | ✅ |
| 定时任务-Agent绑定 | agentId 字段 + 精确匹配 | ✅ |

### 2.3 MCP 工具平台

| 平台 | 状态 |
|------|------|
| Smithery (smithery.ai) | ✅ |
| Glama (glama.ai) | ✅ |

### 2.4 A2A 通信协议

| 组件 | 状态 |
|------|------|
| AgentCard（能力描述卡） | ✅ |
| AgentRegistryPort（注册发现） | ✅ |
| A2AMessagePort（消息路由） | ✅ |
| A2ATaskPort（任务委派） | ✅ |
| InMemory 实现 | ✅ |
| A2ATools（Agent 间通信工具） | ✅ |

---

## 三、文件清单

### 新增文件

```
domain/model/plan/
├── ExecutionPlan.java
├── PlanStep.java
├── PlanStatus.java
└── PlanStepStatus.java

domain/model/a2a/
├── AgentCard.java
├── A2AMessage.java
└── A2ATask.java

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
├── tools/ToolCallbackResolverPort.java
└── a2a/
    ├── AgentRegistryPort.java
    ├── A2AMessagePort.java
    └── A2ATaskPort.java

application/usecase/a2a/
├── AgentDiscoveryUseCase.java
├── A2ATaskUseCase.java
└── A2AMessageUseCase.java

infrastructure/store/db/
├── entity/ExecutionPlanEntity.java
├── entity/PlanStepEntity.java
├── mapper/ExecutionPlanMybatisMapper.java
├── mapper/PlanStepMybatisMapper.java
└── repository/MybatisExecutionPlanRepository.java

infrastructure/a2a/
├── InMemoryAgentRegistry.java
├── InMemoryA2AMessageBroker.java
└── InMemoryA2ATaskManager.java

infrastructure/tools/system_tools/core_tools/
├── PlanTools.java
├── PlanExecutor.java
├── SkillRunner.java
├── ModelSwitchTools.java
├── A2ATools.java
└── dto/
    ├── PlanStepToolInput.java
    ├── ModelCapabilitySummary.java
    ├── ModelRecommendation.java
    ├── KnowledgeBaseSummary.java
    ├── KnowledgeBaseRecommendation.java
    ├── ScheduledTaskResult.java
    ├── SkillDetailDTO.java
    ├── SkillExecutionResult.java
    ├── ModelSwitchResult.java
    ├── HttpToolResult.java
    ├── RestfulToolDTO.java
    ├── AgentCardDTO.java
    └── A2ATaskResult.java

infrastructure/tools/mcp_tools/platform/
├── McpPlatform.java
├── McpToolInfo.java
├── McpPlatformClient.java
├── McpPlatformRegistry.java
├── McpPlatformConfig.java
├── SmitheryPlatformClient.java
├── GlamaPlatformClient.java
└── McpDiscoveryTools.java

sql/
└── V2__add_execution_plan_tables.sql

docs/
├── 2026-05-30-autonomous-agent-technical-design.md
├── 2026-05-30-autonomous-agent-system-prompt.md
├── 2026-05-30-autonomous-agent-capabilities-plan.md
└── 2026-05-30-fix-and-a2a-plan.md
```

### 修改文件

```
application/usecase/AgentContextUseCase.java — 动态提示词 + 工具策略
application/usecase/AgentPoolUseCase.java — evict 方法
application/usecase/ScheduledTaskUseCase.java — 移除 deleteBefore 调用
application/port/out/repositories/ScheduledTaskRepository.java — findAllEnabled
application/port/out/tools/ToolCallbackResolverPort.java — resolveByName
infrastructure/tools/AgentToolsFactory.java — 实现 resolveByName
infrastructure/tools/system_tools/core_tools/ModelTools.java — 推荐逻辑
infrastructure/tools/system_tools/core_tools/KnowledgeTools.java — 语义推荐
infrastructure/tools/system_tools/core_tools/MemoryTools.java — 搜索 + 删除
infrastructure/tools/system_tools/core_tools/RuntimeTools.java — 安全性
infrastructure/tools/system_tools/core_tools/FileTools.java — 完善
infrastructure/tools/system_tools/core_tools/SkillTools.java — 执行
infrastructure/tools/system_tools/core_tools/AutomationTools.java — 调度器集成
infrastructure/tools/system_tools/core_tools/SubagentManageTools.java — await/batch
infrastructure/tools/system_tools/core_tools/RestfulTools.java — RetryTemplate
infrastructure/store/db/entity/ScheduledTaskEntity.java — 新增字段
infrastructure/store/db/repository/MybatisScheduledTaskRepository.java — findAllEnabled + 映射
infrastructure/store/db/repository/MybatisScheduledTaskRunLogRepository.java — 新增
infrastructure/scheduler/ScheduledTaskScheduler.java — agentId 绑定
domain/model/ScheduledTask.java — agentId/lastRunResult/runCount
sql/schema.sql — scheduled_task 新增列 + 新表
```

---

## 四、测试结果

| 测试类型 | 结果 |
|---------|------|
| `gradle clean compileJava` | ✅ 通过 |
| `gradle test --tests "*AgentHubCleanArchitectureTest*"` | ✅ 15/15 通过 |

---

## 五、架构合规性

- ✅ 所有新增代码遵守四层整洁架构
- ✅ `domain` 层无框架依赖
- ✅ `application` 层通过端口接口访问基础设施
- ✅ `infrastructure` 层实现端口接口
- ✅ 每个方法 ≤ 10 行
- ✅ 使用 Lombok，禁止 record 类
- ✅ DTO 转换使用 BeanUtil.copyProperties
