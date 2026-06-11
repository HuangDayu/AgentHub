# Subagent 异步运行时实现方案

## 背景

Subagent 的目标不是同步函数调用，而是主 Agent 可以并发创建多个子 Agent，让它们在独立 subsession 中后台执行任务，并在执行过程中查询状态、消息、结果或中断任务。

当前实现应遵循：

- 主 Agent 调用 `createSubagent` 后立即获得运行句柄。
- Subagent 在后台异步执行，不阻塞主 Agent。
- 每个 subagent 任务都有独立 `subagentId + subsessionId`。
- 主 Agent 后续通过句柄查询状态、消息、结果或停止任务。
- Subsession 消息与主 session 消息结构一致，仅请求接口不同。
- 工具返回结构化 DTO，而不是依赖字符串拼接。

## 设计原则

1. Clean Architecture
   - `infrastructure/tools` 只做工具适配。
   - `application/usecase` 负责 subagent 创建、查询和控制编排。
   - `application/port/out` 定义执行端口。
   - `infrastructure/agents/subagent` 实现后台执行和中断。

2. 异步优先
   - 创建 subagent 只启动后台任务并返回句柄。
   - 禁止在 runtime tool path 中使用同步等待结果的 `executeAndWait`。

3. 句柄驱动
   - 所有后续操作使用 `subagentId + subsessionId`。
   - 不再通过 `subagentId` 查找第一个 subsession，避免多任务时串会话。

4. 结构化输出
   - 工具方法可以返回 DTO 或 `List<DTO>`。
   - 主 Agent 不需要解析自然语言字符串。

5. 消息模型复用
   - 主 session 与 subsession 的消息请求和响应模型一致。
   - 前端渲染共用 `ChatMessage` 解析和渲染逻辑。
   - 两者唯一差异是 API URL。

## 后端方案

### 工具层

文件：`src/main/java/com/agenthub/infrastructure/tools/system_tools/base_tools/SubagentManageTools.java`

`SubagentManageTools` 只负责把工具输入转换为 application command，并返回 application output。

工具方法：

- `createSubagent(CreateSubagentToolInput, ToolContext): SubagentRunOutput`
- `listSubagents(ToolContext): List<SubagentRuntimeOutput>`
- `getSubagentStatus(SubagentHandleToolInput, ToolContext): SubagentRuntimeOutput`
- `getSubagentMessages(SubagentHandleToolInput, ToolContext): List<SubagentMessageOutput>`
- `getSubagentResult(SubagentHandleToolInput, ToolContext): SubagentRuntimeOutput`
- `stopSubagent(SubagentHandleToolInput, ToolContext): SubagentRuntimeOutput`

输入对象：

- `CreateSubagentToolInput`
  - `name`
  - `systemPrompt`
  - `task`
  - `tools`
  - `knowledgeIds`
  - `modelConfigId`

- `SubagentHandleToolInput`
  - `subagentId`
  - `subsessionId`

### Application 层

文件：`src/main/java/com/agenthub/application/usecase/SubagentRuntimeUseCase.java`

核心职责：

- 创建 `Subagent`。
- 创建独立 `Subsession`。
- 基于父 Agent 上下文构建子 Agent `ReActAgentContext`。
- 继承模型、工具、知识库、workspace 和策略配置。
- 调用 `SubagentExecutionPort.execute(...)` 异步启动。
- 提供基于句柄的状态、消息、结果、停止能力。

`run(RunSubagentCommand)` 流程：

```java
Subagent subagent = createSubagent(command);
Subsession subsession = createSubsession(command, subagent);
subagentExecutionPort.execute(executionCommand(command, subagent, subsession));
return runOutput(subagent, subsession, null);
```

关键点：

- 不等待 subagent 结果。
- 返回 `SubagentRunOutput`，包含 `subagentId`、`subsessionId`、`status`。
- 后续操作通过 `SubagentHandleToolInput` 的句柄定位。

### 执行端口

文件：`src/main/java/com/agenthub/application/port/out/agent/SubagentExecutionPort.java`

端口方法：

```java
void execute(SubagentExecutionCommand command);
Flux<AgentMessage> stream(SubAgentChatCommand command);
boolean stop(Subagent subagent, String subsessionId);
```

说明：

- `execute(...)` 用于后台执行 subagent 初始任务。
- `stream(...)` 用于已有 subsession 的后续对话。
- `stop(...)` 返回是否真正停止了运行中的任务。
- 不保留同步等待结果的 runtime tool 路径。

### 执行实现

文件：`src/main/java/com/agenthub/infrastructure/agents/subagent/SubagentEngine.java`

运行状态存储：

```java
Table<String, String, SubagentEngineContext> RUNNING_TASKS
```

Key：

- row：`subsessionId`
- column：`subagentId`

执行流程：

1. 创建 `AbstractReActAgent`。
2. 写入 `RUNNING_TASKS`。
3. 调用 `AgentStreamExecutor.streamMessages(...)`。
4. `subscribeOn(ttlExecutorService)` 后订阅执行。
5. 完成时标记 `COMPLETED`。
6. 失败时标记 `FAILED`。
7. finally 从 `RUNNING_TASKS` 移除。

停止流程：

1. 用 `subsessionId + subagentId` 从 `RUNNING_TASKS` 移除上下文。
2. 调用 `agent.interrupt()`。
3. dispose Reactor subscription。
4. 返回 `true`。
5. 若未找到运行任务，返回 `false`，不误标记已完成任务为 interrupted。

### 消息持久化

文件：`src/main/java/com/agenthub/infrastructure/agents/subagent/AgentStreamExecutor.java`

职责：

- 保存用户消息。
- 保存 assistant 文本消息。
- 保存 assistant tool call 消息。
- 保存 tool result 消息。
- 保存 system/error 消息。

要求：

- 过滤 AgentScope 内部 `fragment` 和 `__fragment__` 工具调用。
- 避免无意义工具调用污染 subsession 历史。

### AgentScope 消息适配

文件：`src/main/java/com/agenthub/infrastructure/agents/aliyun/AgentScopeHarnessAgent.java`

要求：

- `ToolUseBlock` 转换为 `AgentMessage.ToolCall` 时过滤：
  - `fragment`
  - `__fragment__`

## 工具传递方案

Subagent 构建时必须继承父 Agent 的可执行工具，而不是只传工具名称字符串。

工具匹配规则：

- 可通过 `AgentToolInfo.name` 匹配。
- 可通过 `AgentToolInfo.configId` 匹配。
- Subagent 不继承 `SubagentManageTools` 本身，避免递归创建和控制混乱。
- 如果用户未指定工具列表，默认继承父 Agent 可执行工具。

如果运行时使用 Spring AI `ToolCallback`，subagent context 也必须携带对应 callback，使子 Agent 知道工具 input schema。

## 前端方案

主 session 与 subsession 的消息结构一致：

- `ChatMessage`
- `StreamMessage`
- `ToolCall`
- `ToolResponse`

因此前端只应区分请求接口，不应复制一套 subsession 消息渲染。

### API 区分

文件：`src/main/web/src/api/runtime-api.ts`

主 session：

- `listMessages(selection, agentId, sessionId)`
- `sendMessageStream(selection, agentId, sessionId, content, filePaths, callbacks)`

Subsession：

- `listSubsessionMessages(selection, agentId, parentSessionId, subsessionId)`
- `sendMessageStream(..., parentSessionId, ..., subsessionId)`

### 渲染复用

文件：`src/main/web/src/views/agenthub/RuntimeChatView.vue`

复用主 session 的处理函数：

- `parseChatMessages(...)`
- `parseChatMessage(...)`
- `parseToolCalls(...)`
- `parseToolResponses(...)`
- `handleStreamMessage(...)`
- `getMessageRoleLabel(...)`

复用主 session 的渲染组件：

- `MarkdownRenderer`
- `ToolCallMessage`
- `ToolResultMessage`
- `SkillMessage`

Subsession 只在加载历史和发送消息时换 API：

- 历史消息走 `listSubsessionMessages(...)`。
- 流式发送走 `sendMessageStream(..., subsessionId)`。
- 返回消息进入同一个 `ChatMessage` 渲染链路。

## 数据流

### 创建 subagent

```text
Main Agent
  -> createSubagent tool
  -> SubagentManageTools
  -> SubagentRuntimeUseCase.run
  -> SubagentRepository.save
  -> SubsessionRepository.save
  -> SubagentExecutionPort.execute
  -> SubagentEngine 后台执行
  <- SubagentRunOutput(subagentId, subsessionId, RUNNING)
```

### 查询状态

```text
Main Agent
  -> getSubagentStatus({subagentId, subsessionId})
  -> SubagentRuntimeUseCase.status
  <- SubagentRuntimeOutput
```

### 查询消息

```text
Main Agent
  -> getSubagentMessages({subagentId, subsessionId})
  -> SubagentRuntimeUseCase.messages
  -> SubsessionRepository.findByIdWithMessages
  <- List<SubagentMessageOutput>
```

### 查询结果

```text
Main Agent
  -> getSubagentResult({subagentId, subsessionId})
  -> SubagentRuntimeUseCase.result
  -> latest assistant text message
  <- SubagentRuntimeOutput(result)
```

### 停止任务

```text
Main Agent
  -> stopSubagent({subagentId, subsessionId})
  -> SubagentRuntimeUseCase.stop
  -> SubagentExecutionPort.stop
  -> SubagentEngine removes RUNNING_TASKS and interrupts agent
  <- SubagentRuntimeOutput(message/status)
```

## 验证方案

后端：

```bash
gradle compileJava
gradle test --tests "*AgentHubCleanArchitectureTest*"
gradle test --tests "*SubagentIntegrationTest*"
```

前端：

```bash
npm --prefix src/main/web run build
```

手动验证：

1. 主 Agent 连续创建两个 subagent。
2. 两次创建都应立即返回不同的 `subagentId` 和 `subsessionId`。
3. 主 Agent 可分别查询两个 subagent 的状态和消息。
4. Subagent 消息中不应出现 `fragment` 或 `__fragment__` 工具调用。
5. Subagent 可以正常调用父 Agent 传入的工具，并带有正确 input schema。
6. 前端打开 subsession 历史消息时，应与主 session 一样渲染 markdown、工具调用、工具结果和 skill 消息。
7. 停止正在运行的 subagent 后，状态应变为 `INTERRUPTED`；停止已结束任务不应误改状态。

## 非目标

- 不新增独立进度表。
- 不为 subsession 复制一套消息模型。
- 不让工具返回自然语言字符串作为机器可读协议。
- 不允许 subagent 默认继承 `SubagentManageTools` 形成递归控制。
