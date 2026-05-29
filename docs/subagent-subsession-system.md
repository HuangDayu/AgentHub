# Subagent / Subsession 运行时系统设计文档

## 概述

Agent 可以在运行时**自主创建** Subagent 处理子任务，每个 Subagent 拥有独立的 Subsession 和 ChatMessage 对话记录。Subagent 可递归创建子 Subagent。

```
Agent (智能体)
  ├── Session (会话) ← REST 创建
  │     ├── ChatMessage (用户↔Agent 对话)
  │     ├── Subsession (子会话) ← REST 创建
  │     │     ├── Subagent (子智能体) ← Agent 运行时自动创建
  │     │     └── ChatMessage (用户↔Subagent 对话)
  │     └── Subsession...
  └── Session...
```

---

## 数据库设计

### subagent 表

```sql
CREATE TABLE subagent (
    id              VARCHAR(64)  PRIMARY KEY,
    parent_agent_id VARCHAR(64)  NOT NULL,
    parent_subagent_id VARCHAR(64),          -- 递归嵌套
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    system_prompt   TEXT,
    model_config_id VARCHAR(64),
    status          VARCHAR(32)  NOT NULL DEFAULT 'ACTIVE',
    tenant_id       VARCHAR(64),
    workspace_id    VARCHAR(64),
    created_at      TIMESTAMP,
    updated_at      TIMESTAMP
);
```

### subsession 表

```sql
CREATE TABLE subsession (
    id                VARCHAR(64)  PRIMARY KEY,
    parent_session_id VARCHAR(64)  NOT NULL,  -- 指向父 Session
    subagent_id       VARCHAR(64)  NOT NULL,  -- 指向 Subagent
    parent_subsession_id VARCHAR(64),          -- 递归嵌套
    name              VARCHAR(255),
    status            VARCHAR(32)  NOT NULL DEFAULT 'ACTIVE',
    created_at        TIMESTAMP,
    updated_at        TIMESTAMP
);
```

### 消息存储

Subagent 的对话消息与普通 Session 共用 `chat_message` 表，`session_id` 字段指向 **Subsession ID**。

```sql
-- chat_message 表（既有表，无变更）
SELECT * FROM chat_message WHERE session_id = '<subsession_id>';
```

---

## 后端架构

### 4 层 Clean Architecture

```
api (Controller/DTO) → SubsessionUseCase (application)
                            │
                    SubagentExecutionPort (application/port/out)
                            │
                    SubagentEngine (infrastructure, 实现 Port)
```

### 新增文件

| 文件 | 层 | 说明 |
|------|----|------|
| `SubagentExecutionPort.java` | application/port/out/agent | Subagent 执行端口接口 |
| `SubagentManageTools.java` | infrastructure/tools | Agent 可调用的 6 个系统工具 |

### 修改文件

| 文件 | 变更 |
|------|------|
| `Subagent.java` (domain) | 新增 `parentSubagentId` 字段 |
| `SubagentEntity.java` (infra) | 新增 `parentSubagentId` 映射 |
| `SubagentOutput.java` (application) | 新增 `parentSubagentId` |
| `Subsession.java` (domain) | 新增 `parentSubsessionId` |
| `SubsessionEntity.java` (infra) | 新增 `parentSubsessionId` 映射 |
| `SessionController.java` (api) | 新增 subsession CRUD + 消息端点 |
| `SessionUseCase.java` (application) | `list()` 支持回退查 Subsession |
| `SubsessionUseCase.java` (application) | 新增 `getMessages()` / `streamMessage()` |
| `SubagentEngine.java` (infrastructure) | 实现 `SubagentExecutionPort` |
| `CommonBeanConfiguration.java` (infra) | 新增 `subagentExecutor` Bean |
| `sql/schema.sql` | 新增列 + 表 |
| `SessionController.java` | 新增 `streamSubsessionMessage` 端点 |

### 删除文件

SubagentRun 相关 17 个文件全部删除（`SubagentRun.java`, `SubagentRunUseCase.java`, `SubagentRunController.java`, 等）。

---

## 后端 API

### Subsession CRUD（通过 SessionController）

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/sessions/{id}/subsessions` | 创建 Subsession |
| `GET` | `/sessions/{id}/subsessions` | 列表 |
| `GET` | `/sessions/{id}/subsessions/{subId}` | 详情 |
| `POST` | `/sessions/{id}/subsessions/{subId}/close` | 关闭 |
| `GET` | `/sessions/{id}/subsessions/{subId}/messages` | 获取 Subsession 消息 |
| `POST` | `/sessions/{id}/subsessions/{subId}/messages/stream` | 流式发送消息到 Subagent |

### Subagent 监控（只读）

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/agents/{agentId}/subagents` | Subagent 列表 |
| `GET` | `/agents/{agentId}/subagents/{id}` | Subagent 详情 |

Subagent **不通过 REST API 创建**，由 Agent 运行时通过 `SubagentManageTools` 自动创建。

---

## 关键实现细节

### 1. SubagentManageTools（6 个 Agent 工具）

```java
@AgentTools(name = "SubagentManageTools", description = "子智能体工具")
public class SubagentManageTools {

    @Tool(description = "创建子Agent并异步执行任务")
    String createSubagent(name, systemPrompt, task, tools, knowledgeIds, modelConfigId)
    // → 创建 Subagent + Subsession → 提交异步执行 → 返回 subagentId

    @Tool(description = "获取子Agent列表")
    String listSubagents()

    @Tool(description = "获取子Agent状态")
    String getSubagentStatus(subagentId)

    @Tool(description = "停止子Agent")
    String stopSubagent(subagentId)      // 设置 INTERRUPTED 标志

    @Tool(description = "获取子Agent对话记录")
    String getSubagentMessages(subagentId)
}
```

注册方式：`@AgentTools` 注解 + `SystemToolsFactory.init()` 自动扫描。

### 2. 异步执行机制

`createSubagent` 调用后立即返回，通过 `ThreadPoolTaskExecutor` 异步执行：

```
createSubagent()
  → 创建 Subagent（status=RUNNING）
  → 创建 Subsession
  → 提交到 subagentExecutor 线程池
  → 返回 subagentId

[异步线程]:
  → reActAgentFactory.create(ctx)
  → agentStreamExecutor.streamMessages()
  → 每条 ASSISTANT 消息 → saveMessage() 写入 ChatMessage
  → 完成 → status=COMPLETED
  → 错误 → status=FAILED
  → stopSubagent → status=INTERRUPTED（线程检测标志后停止）
```

### 3. Subsession 消息流式发送

前端发送消息到 Subsession 时，路径为：
```
POST /sessions/{parentSessionId}/subsessions/{subsessionId}/messages/stream
```

后端通过 `SubsessionUseCase.streamMessage()` → `SubagentExecutionPort.streamAndSave()` 执行 Subagent 并返回 `Flux<AgentMessage>`，Controller 映射为 `Flux<AgentMessageResponse>`，SSE 格式与普通 Session 一致。

---

## 前端实现

### 文件变更

| 文件 | 变更 |
|------|------|
| `RuntimeChatView.vue` | 运行视图新增「子Agent」标签页；会话列表显示 Subsession 子节点 |
| `SubagentManagementView.vue` | 改为只读监控页面（去除 CRUD 操作） |
| `runtime-api.ts` | 新增 `listSubsessionMessages()`，`sendMessageStream` 支持 `subsessionId` 参数 |
| `router/index.ts` | 移除 `subagent-chat` 路由 |

### 运行视图「子Agent」标签

```
子Agent 标签
├── Subagent 列表（可点击选中）
│   ├── 数据分析师  [COMPLETED]
│   └── 图表绘制师  [RUNNING]
│
└── [选中后显示]
    ├── 子会话 区
    │   ├── 名称 / 状态 / 创建时间
    │
    └── 对话记录 区
        ├── [USER] 分析销售数据...
        └── [ASSISTANT] 分析结果如下...
```

### 会话列表 Subsession 子节点

Subsession 只在**当前选中的 Session 下**显示。点击 Subsession 节点 → 在对话区显示该 Subsession 的 ChatMessage 历史消息。

```
会话列表
└─ 📋 我的会话（选中）
     └─ 🔺 数据分析师 ← Subsession 节点
          (ACTIVE)
```

### 发送消息到 Subsession

当对话区正在查看 Subsession 历史消息时，`handleSend` 自动使用 subsession 流式端点发送消息：

```typescript
// RuntimeChatView.vue handleSend()
const subsessionId = isSubsessionView.value ? currentSessionId : undefined
await sendMessageStream(selection, agentId,
  isSubsessionView.value ? currentSubsessionParentId.value : currentSessionId,
  content, filePaths, callbacks, subsessionId)
```

`sendMessageStream` 收到 `subsessionId` 参数后自动切换 URL 路径。

---

## 前端数据流

```
点击 Subsession 节点
  → selectSubsession(ss)
    → selectedSubsessionId = ss.id
    → selectedSessionId = ss.id（跳过 loadRuntimeData watch）
    → listSubsessionMessages() 加载 ChatMessage
    → messages.value = msgs
    → 对话区显示 Subsession 历史消息

点击 Session 节点
  → selectSession(sessionId)
    → selectedSessionId = sessionId
    → selectedSubsessionId = ''（退出 subsession 模式）
    → loadMessages() 加载父 Session 消息
```

---

## 关键约束

1. **Clean Architecture**：Controller 只能调用 UseCase，不能直接使用 Repository
2. **方法 ≤ 10 行**：所有方法控制在 10 行逻辑代码以内
3. **Subagent 只读管理**：Subagent 不由 REST 创建，仅提供查询（监控）端点
4. **共用消息表**：Subsession 与 Session 共用 `chat_message` 表，通过 `session_id` 区分
5. **异步执行**：Subagent 执行不阻塞父 Agent
