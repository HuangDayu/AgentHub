# Subagent Runtime Fix Plan

## 背景

`docs/2026-05-29-subagent-subsession-system.md` 定义了 Subagent/Subsession 的目标形态：父 Agent 通过系统工具创建 Subagent，Subagent 使用独立 Subsession 保存消息，并支持后续通过 Subsession 继续对话。

当前代码已经具备表、仓储、系统工具、执行引擎和 Subsession API，但运行闭环没有真正打通，导致 Subagent 创建后不可用或无法稳定续聊。

## 当前断点

1. `SubagentManageTools.createSubagent` 返回 `Flux<AgentMessage>`，不符合工具“立即返回 subagentId”的设计。
2. `SubagentEngine.executeSubagent` 把 `Flux` 放入异步线程中创建，但没有订阅；Reactor 流是惰性的，因此后台任务不会执行。
3. `executeSubagent` 在异步线程内才设置 `streamFlux`，方法返回时可能是 `null`。
4. Subagent 上下文只填充了 agent/session/model/prompt/tools/knowledge/workspace，缺少父 Agent 已解析出的 model/tool/guardrail/retrieval strategy，部分 Agent 工厂会空指针或配置缺失。
5. `SubagentExecutionPort.stream` 只能从内存 `RUNNING_TASKS` 找 Agent，Subagent 已完成、服务重启或内存丢失后，Subsession 续聊会直接 404。
6. `stopSubagent` 只移除内存任务，没有调用底层 Agent interrupt；状态枚举也缺少 `INTERRUPTED`。

## 修复方案

### 1. 创建即后台执行

`SubagentManageTools.createSubagent` 改为返回字符串结果：创建 Subagent 和 Subsession 后调用 `SubagentEngine.executeSubagent(...)` 启动后台执行，并立即返回 subagent/subsession 信息。

`SubagentEngine.executeSubagent` 不再向工具调用方返回流，而是在内部创建并订阅流：

- 保存 USER 消息。
- 订阅 Agent 输出并保存 ASSISTANT/TOOL/SYSTEM 消息。
- 开始时状态为 `RUNNING`。
- 完成时状态为 `COMPLETED`。
- 错误时状态为 `FAILED`。
- 被停止时状态为 `INTERRUPTED`。

### 2. Subsession 续聊按数据库重建 Agent

`SubagentExecutionPort.stream(SubAgentChatCommand)` 保持应用层端口不变。

`SubagentEngine.stream` 调整为：

- 先根据 `subSessionId` 查询 Subsession。
- 再根据 Subsession 的 `subagentId` 查询 Subagent。
- 如果内存里有同一 Subsession/Subagent 的运行中上下文，则返回该流。
- 否则基于数据库中的 Subagent/Subsession 重建 `ReActAgentContext` 和 `AbstractReActAgent`，再调用 `AgentStreamExecutor.streamMessages(...)` 返回 SSE 流。

这样前端打开历史 Subsession 后仍可继续对话，不依赖创建时留下的内存对象。

### 3. 继承父 Agent 的运行配置

创建 Subagent 上下文时继承父 Agent 已解析的：

- `modelStrategy`
- `toolStrategy`
- `guardrailStrategy`
- `retrievalStrategy`
- `agentConfigs`
- `workspace`

Subagent 可覆盖 `modelConfigId`、`systemPrompt`、`tools`、`knowledgeIds`，没有覆盖时沿用父 Agent 的模型和策略。

### 4. 停止运行中的 Subagent

`stopSubagent` 通过 `SubagentEngine.stop(...)` 调用底层 Agent 的 `interrupt()`，移除内存上下文并持久化 `INTERRUPTED` 状态。

### 5. 本轮暂不扩大范围

`parentSubagentId` 和 `parentSubsessionId` 已在 schema 中出现，但领域模型和实体尚未完整接入。递归树展示、嵌套查询和前端层级交互不纳入本次修复；本次只保证“父 Agent 创建 Subagent、后台执行、消息可查、Subsession 可续聊、可停止”的闭环可用。

## 验证

1. 运行后端编译或相关测试，确认签名调整没有破坏 Spring Bean 创建。
2. 优先运行 Subagent 相关测试；若现有测试依赖外部服务或环境不可用，至少运行能覆盖编译的 Gradle 检查并记录失败原因。
