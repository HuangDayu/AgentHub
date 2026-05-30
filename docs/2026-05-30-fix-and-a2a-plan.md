# 自主 Agent 修复 + A2A 通信协议技术方案

> 日期：2026-05-30

---

## Part 1: 修复 5 个关键问题

### 1.1 模型切换写 DB

**问题**：`ModelSwitchTools.switchModel()` 只驱逐缓存，没有写入新的 modelConfigId。

**方案**：在 `agent_config` 表中更新 CHAT_MODEL 类型的 configId。

**修改文件**：
- `ModelSwitchTools.java` — 调用 `AgentConfigUseCase` 更新配置

### 1.2 工具策略接入

**问题**：`ToolStrategyApplier` 已实现但未被调用。

**方案**：在 `AgentContextUseCase.resolveToolCallbacks()` 中调用 `toolStrategyApplier.apply()`。

**修改文件**：
- `AgentContextUseCase.java` — 注入并调用 ToolStrategyApplier

### 1.3 HTTP_TOOL 工厂

**问题**：`AgentToolType.HTTP_TOOL` 没有对应的 `AbstractToolsFactory` 实现。

**方案**：创建 `HttpToolsFactoryAdapter` 将 `HttpToolsFactory` 适配为 `AbstractToolsFactory`。

**新增文件**：
- `infrastructure/tools/http_tools/HttpToolsFactoryAdapter.java`

### 1.4 SKILL_TOOL 工厂

**问题**：`AgentToolType.SKILL_TOOL` 没有对应的 `AbstractToolsFactory` 实现。

**方案**：创建 `SkillToolsFactoryAdapter`。

**新增文件**：
- `infrastructure/tools/skills_tools/SkillToolsFactoryAdapter.java`

### 1.5 SkillRunner 工具名解析

**问题**：`extractToolName()` 硬编码 4 个关键词。

**方案**：改为通过 `toolCallbackResolver.resolveByName()` 查找工具，支持任意工具名。

**修改文件**：
- `SkillRunner.java` — 替换硬编码映射

---

## Part 2: A2A 通信协议

### 2.1 概念

A2A (Agent-to-Agent) 通信协议允许独立 Agent 之间发现、委派、协作。

与现有子 Agent 的区别：
- 子 Agent：父子关系，父创建子，单向委派
- A2A：对等关系，Agent 之间互相发现、互相委派

### 2.2 架构

```
AgentHub 平台
├── AgentRegistry — Agent 注册与发现
├── A2AMessageBroker — 消息路由
├── A2ATaskManager — 任务委派与追踪
└── Agent 之间的通信通过平台中转
```

### 2.3 领域模型

**AgentCard** — Agent 的能力描述卡（类似 A2A 协议的 Agent Card）
```
domain/model/a2a/AgentCard.java
字段：agentId, name, description, capabilities, endpoint, status
```

**A2AMessage** — Agent 间消息
```
domain/model/a2a/A2AMessage.java
字段：id, fromAgentId, toAgentId, type(TASK/RESULT/HEARTBEAT), content, status
```

**A2ATask** — 跨 Agent 任务
```
domain/model/a2a/A2ATask.java
字段：id, fromAgentId, toAgentId, task, status, result, createdAt
```

### 2.4 端口接口

```
application/port/out/a2a/
├── AgentRegistryPort.java — Agent 注册与发现
├── A2AMessagePort.java — 消息发送与接收
└── A2ATaskPort.java — 任务委派与状态查询
```

### 2.5 用例

```
application/usecase/a2a/
├── AgentDiscoveryUseCase.java — Agent 发现
├── A2ATaskUseCase.java — 任务委派与结果获取
└── A2AMessageUseCase.java — 消息收发
```

### 2.6 SystemTools

```
infrastructure/tools/system_tools/core_tools/
└── A2ATools.java — Agent 间通信工具
    ├── listAvailableAgents() — 发现可用 Agent
    ├── sendTask(agentId, task) — 委派任务
    ├── getTaskResult(taskId) — 获取结果
    └── awaitTaskResult(taskId, timeout) — 等待结果
```

### 2.7 Infrastructure

```
infrastructure/a2a/
├── InMemoryAgentRegistry.java — 内存 Agent 注册表
├── InMemoryA2AMessageBroker.java — 内存消息路由
└── InMemoryA2ATaskManager.java — 内存任务管理
```

### 2.8 文件清单

**新增**：
```
domain/model/a2a/AgentCard.java
domain/model/a2a/A2AMessage.java
domain/model/a2a/A2ATask.java
application/port/out/a2a/AgentRegistryPort.java
application/port/out/a2a/A2AMessagePort.java
application/port/out/a2a/A2ATaskPort.java
application/usecase/a2a/AgentDiscoveryUseCase.java
application/usecase/a2a/A2ATaskUseCase.java
application/usecase/a2a/A2AMessageUseCase.java
infrastructure/a2a/InMemoryAgentRegistry.java
infrastructure/a2a/InMemoryA2AMessageBroker.java
infrastructure/a2a/InMemoryA2ATaskManager.java
infrastructure/tools/system_tools/core_tools/A2ATools.java
infrastructure/tools/system_tools/core_tools/dto/A2ATaskResult.java
infrastructure/tools/system_tools/core_tools/dto/AgentCardDTO.java
```

**修改**：
```
ModelSwitchTools.java — 写DB
AgentContextUseCase.java — 接入 ToolStrategyApplier
AgentPoolUseCase.java — Agent 注册到 A2A 注册表
```

---

## 实施顺序

```
1. 修复 ModelSwitchTools
2. 修复 ToolStrategyApplier 接入
3. 创建 HttpToolsFactoryAdapter
4. 创建 SkillToolsFactoryAdapter
5. 修复 SkillRunner
6. 创建 A2A 领域模型
7. 创建 A2A 端口接口
8. 创建 A2A 用例
9. 创建 A2A Infrastructure
10. 创建 A2ATools
11. 编译验证 + ArchUnit
```
