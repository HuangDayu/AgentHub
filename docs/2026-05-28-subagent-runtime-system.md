# Subagent 运行时系统实现方案

## 1. 概述

实现 Agent 运行时自动创建 Subagent 的能力，Subsession 作为 Session 的子资源管理，
前端以树结构展示 Subsession，并提供完整的运行时生命周期管理。

## 2. 概念模型

```
Agent (智能体)
  ├── Session (会话) ← 用户发起对话的基本单位
  │     ├── ChatMessage (消息记录)
  │     ├── Subsession (子会话) ← Agent运行时为Subagent创建
  │     │     ├── Subagent (子智能体) ← Agent运行时动态创建
  │     │     └── SubagentRun (执行记录)
  │     └── Subsession...
  └── Session...
```

### 关系说明

| 关系 | 说明 |
|------|------|
| Agent → Session | 1:N，一个Agent可以有多个会话 |
| Session → Subsession | 1:N，一个Session可以有多个子会话 |
| Subsession → Subagent | 1:1，每个子会话对应一个运行时子智能体 |
| Subsession → SubagentRun | 1:N，一个子会话可以有多次运行 |
| Agent → Subagent | 1:N，一个Agent可以创建多个子智能体（运行时） |

### 生命周期

```
Subagent:   CREATED → RUNNING → COMPLETED / FAILED / INTERRUPTED
Subsession: ACTIVE → CLOSED
SubagentRun: PENDING → RUNNING → COMPLETED / FAILED / INTERRUPTED
```

## 3. 后端架构

### 3.1 四层架构

```
api (Controller/DTO)
  └── SessionController ← Subsession端点合并至此
  └── SubagentController ← 运行时管理
  └── SubagentRunController ← 运行管理 + SSE流式

application (UseCase/Port)
  └── SubagentUseCase ← 运行时创建/管理
  └── SubsessionUseCase ← 子会话CRUD
  └── SubagentRunUseCase ← 运行管理 + 流式
  └── AgentChatUseCase ← 加入自动创建Subagent逻辑

domain (Model)
  └── Subagent, Subsession, SubagentRun

infrastructure (DB/Agent)
  └── Mybatis*Repository ← 持久化
  └── SubagentEngine ← 核心执行引擎
```

### 3.2 领域模型

**Subagent** — Agent 运行时创建的子智能体
```java
class Subagent {
    String id, tenantId, workspaceId, parentAgentId;
    String name, description, systemPrompt, modelConfigId;
    String status; // ACTIVE / INACTIVE
    Instant createdAt, updatedAt;
}
```

**Subsession** — Session 下的子会话（已移除 agentId）
```java
class Subsession {
    String id, parentSessionId, subagentId;
    String name, status; // ACTIVE / CLOSED
    List<ChatMessage> messages;
}
```

**SubagentRun** — 单次执行记录
```java
class SubagentRun {
    String id, subagentId, subsessionId;
    String status; // PENDING / RUNNING / COMPLETED / FAILED / INTERRUPTED
    String input, output;
    Instant startedAt, endedAt;
}
```

### 3.3 API 端点总览

| 方法 | 路径 | 控制器 | 说明 |
|------|------|--------|------|
| POST | `/workspaces/{wsId}/agents/{agentId}/sessions` | SessionController | 创建会话 |
| GET | `/workspaces/{wsId}/agents/{agentId}/sessions` | SessionController | 列会话 |
| POST | `/workspaces/{wsId}/agents/{agentId}/sessions/{id}/subsessions` | SessionController | 创建子会话 |
| GET | `/workspaces/{wsId}/agents/{agentId}/sessions/{id}/subsessions` | SessionController | 列子会话 |
| GET | `/workspaces/{wsId}/agents/{agentId}/sessions/{id}/subsessions/{id}` | SessionController | 获取子会话 |
| POST | `/workspaces/{wsId}/agents/{agentId}/sessions/{id}/subsessions/{id}/close` | SessionController | 关闭子会话 |
| POST | `/workspaces/{wsId}/subsessions/{ssId}/subagents/{saId}/runs` | SubagentRunController | 启动运行 |
| POST | `/workspaces/{wsId}/subsessions/{ssId}/subagents/{saId}/runs/stream` | SubagentRunController | 流式执行 |
| GET | `/workspaces/{wsId}/subagents/{saId}/runs` | SubagentRunController | 列运行 |
| GET | `/workspaces/{wsId}/runs/{runId}` | SubagentRunController | 获取运行 |
| POST | `/workspaces/{wsId}/runs/{runId}/stop` | SubagentRunController | 停止运行 |

## 4. 关键变更

### 4.1 修复 JSQLParser 关键字冲突

**问题：** MyBatis-Plus 3.5.16 的 `TenantLineInnerInterceptor` 对每个 SQL 进行 JSQLParser 解析。
`input`/`output`/`status` 在 JSQLParser 5.2 中是保留字，导致 `subagent_run` 表的查询 SQL 解析失败。

**方案：**
1. 从 `SubagentRunEntity` 中移除 `tenant_id`/`workspace_id` 字段
2. 这样 `BaseContextLineHandler.ignoreTable()` 检测到该表无租户字段 → 返回 `true`（忽略）
3. TenantLineInterceptor 跳过 SQL 解析 → 不再触发关键字冲突
4. Java 字段 `input`/`output` 重命名为 `runInput`/`runOutput`（与列名一致，避免异常反射）
5. DB 列名改为 `run_input`/`run_output`/`run_status`

### 4.2 SessionController 合并 Subsession

`SubsessionController.java` 已删除，所有子会话端点移至 `SessionController.java`。
映射路径不变：`/workspaces/{wsId}/agents/{agentId}/sessions/{sessionId}/subsessions/...`

### 4.3 Subagent 运行时自动创建

`AgentChatUseCase` 中增加自动创建逻辑：
1. Agent 处理消息时判断是否为子任务
2. 若是，通过 `SubagentUseCase.create()` 创建 Subagent
3. 通过 `SubsessionUseCase.create()` 在当前 Session 中创建 Subsession
4. 通过 `SubagentEngine.stream()` 执行 Subagent 对话

### 4.4 前端树结构

`RuntimeChatView.vue` 中增加 Subsession 树节点：
```
Session 1
├── Subsession A (Subagent X)
├── Subsession B (Subagent Y)
Session 2
└── Subsession C (Subagent Z)
```

### 4.5 运行时管理

`SubagentManagementView.vue` 改为运行时监控面板：
- 显示所有 Subagent 状态
- 运行记录列表
- 停止/重启操作

## 5. 实施步骤

### Step 1: 修复 JSQLParser 关键字冲突
- 重命名 SubagentRunEntity 字段（移除租户字段 + 重命名 input/output）
- 更新 MybatisSubagentRunRepository 映射
- 更新 sql/schema.sql
- **验收：** 4个失败测试通过

### Step 2: SessionController 合并（已完成）
- **验收：** 编译通过

### Step 3: Subagent 运行时自动创建
- 修改 AgentChatUseCase
- **验收：** 覆盖自动创建场景

### Step 4: 前端 API 更新
- runtime-api.ts + subagent-api.ts
- **验收：** 前端编译无错误

### Step 5: 前端树结构 + 运行时管理
- RuntimeChatView.vue + SubagentManagementView.vue
- **验收：** 视图渲染正确

## 6. 集成测试

| # | 测试 | 当前状态 |
|---|------|---------|
| 1-16 | 基础 CRUD 测试 | ✅ 通过 |
| 17-20 | 运行管理测试 | 🔴 需修复 |
| 21 | 空列表测试 | ✅ 新增 |

## 7. 风险

| 风险 | 备选方案 |
|------|---------|
| JSQLParser 对其他列名冲突 | 所有列加 `sb_` 前缀 |
| Subagent 触发条件难定义 | 先手动触发，后续自动 |
