# A2A 协议对齐方案

> 基于 https://a2acn.com/specification/ 官方规范
> 版本：v1.0.0-rc

---

## 一、当前实现 vs 官方协议

### 官方 A2A 核心数据结构

| 结构 | 说明 | 我们的实现 |
|------|------|-----------|
| AgentCard | Agent 能力描述卡（id, name, version, capabilities, metadata） | AgentCard（简化版，缺少 version/capabilities 结构） |
| Task | 工作单元，生命周期：Created → Waiting → Executing → Completed/Failed/Cancelled | A2ATask（状态不同） |
| Message | 交互载体，包含 Part 列表 | A2AMessage（无 Part 概念） |
| Artifact | 结果载体，Task 的输出 | 无 |
| Part | 内容片段：TextPart / FilePart / DataPart | 无 |

### 官方 A2A 操作

| 操作 | 说明 | 我们的实现 |
|------|------|-----------|
| SendMessage | 发送消息给 Agent | sendTask（简化） |
| SendStreamingMessage | 流式消息 | 无 |
| GetTask | 获取任务状态 | getTaskResult |
| ListTasks | 列出任务 | getIncomingTasks/getOutgoingTasks |
| CancelTask | 取消任务 | 无 |
| SubscribeToTask | 订阅任务更新 | awaitTaskResult（轮询） |
| GetExtendedAgentCard | 获取扩展能力卡 | 无 |

### 官方 A2A 协议绑定

- JSON-RPC over HTTP（主要）
- gRPC
- HTTP+JSON/REST

### 关键差异

1. **Task 生命周期不同**：官方是 Created → Waiting → Executing → Completed/Failed/Cancelled，我们是 PENDING → RUNNING → COMPLETED/FAILED
2. **Message 结构不同**：官方 Message 包含 Part 列表（TextPart/FilePart/DataPart），我们只有 content 字符串
3. **缺少 Artifact**：官方有独立的 Artifact 概念承载结果
4. **缺少 JSON-RPC 传输层**：官方通过 HTTP JSON-RPC 通信，我们是内存调用
5. **AgentCard 结构不同**：官方有 version、capabilities 结构化描述、metadata

---

## 二、对齐方案

### 2.1 数据结构对齐

**修改 AgentCard**：
```
新增字段：version, capabilities(List<AgentCapability>), metadata(Map)
AgentCapability: { type, description, inputs, outputs }
```

**修改 Task（替代 A2ATask）**：
```
状态改为：SUBMITTED → WORKING → COMPLETED / FAILED / CANCELED
新增字段：artifacts(List<Artifact>), history(List<Message>), metadata
```

**新增 Message（替代 A2AMessage）**：
```
字段：role(USER/AGENT), parts(List<Part>), metadata
Part: TextPart(text) / DataPart(data, mimeType) / FilePart(uri, name, bytes)
```

**新增 Artifact**：
```
字段：id, name, parts(List<Part>), metadata
```

### 2.2 操作对齐

**A2A Client（调用方）**：
```
sendMessage(agentUrl, message) → Task
getTask(agentUrl, taskId) → Task
listTasks(agentUrl, filter) → List<Task>
cancelTask(agentUrl, taskId) → Task
```

**A2A Server（被调方）**：
```
暴露 HTTP JSON-RPC 端点
处理 SendMessage / GetTask / ListTasks / CancelTask
```

### 2.3 传输层

**新增 A2A HTTP 端点**：
```
POST /a2a/v1  — JSON-RPC 入口
```

**JSON-RPC 请求格式**：
```json
{
  "jsonrpc": "2.0",
  "id": "req-1",
  "method": "message/send",
  "params": {
    "message": { "role": "user", "parts": [{"type": "text", "text": "..."}] },
    "configuration": { "acceptedOutputModes": ["text"] }
  }
}
```

---

## 三、文件清单

### 修改文件

```
domain/model/a2a/AgentCard.java — 对齐官方字段
domain/model/a2a/A2ATask.java — 重命名为 Task，对齐状态
domain/model/a2a/A2AMessage.java — 重命名为 Message，对齐 Part
```

### 新增文件

```
domain/model/a2a/Part.java — TextPart/DataPart/FilePart
domain/model/a2a/Artifact.java — 结果载体
domain/model/a2a/TaskStatus.java — 官方状态枚举
domain/model/a2a/AgentCapability.java — 能力描述

application/port/out/a2a/A2AClientPort.java — 客户端端口
application/port/out/a2a/A2AServerPort.java — 服务端端口
application/usecase/a2a/A2AClientUseCase.java — 客户端用例
application/usecase/a2a/A2AServerUseCase.java — 服务端用例

infrastructure/a2a/A2AHttpClient.java — HTTP JSON-RPC 客户端
infrastructure/a2a/A2AHttpController.java — HTTP JSON-RPC 端点
infrastructure/a2a/InMemoryTaskStore.java — 任务存储
```

### 修改文件

```
A2ATools.java — 对齐新的 API
AgentCardDTO.java — 对齐新的字段
A2ATaskResult.java — 对齐新的状态
```

---

## 四、实施顺序

```
1. 领域模型对齐（Part, Artifact, TaskStatus, AgentCapability）
2. 修改 AgentCard, Task, Message
3. 创建 A2AClientPort + A2AHttpClient
4. 创建 A2AServerPort + A2AHttpController
5. 修改 A2ATools 适配新 API
6. 编译验证 + ArchUnit
```
