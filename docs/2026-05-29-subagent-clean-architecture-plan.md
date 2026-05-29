# Subagent Clean Architecture Optimization Plan

## 目标

把 Subagent 运行时从“系统工具直接操作仓储和引擎”调整为 Clean Architecture：

- `api` 和系统工具只做输入适配。
- `application` 编排创建、查询、停止、消息读取和执行请求。
- `domain` 保持纯模型和状态行为。
- `infrastructure` 只实现执行细节、Agent 工厂、消息流保存和工具回调。

同时保持一个硬约束：新增和改动方法的逻辑行数不超过 10 行。

## 当前问题

1. `SubagentManageTools` 位于 infrastructure，却直接依赖 `SubagentRepository`、`SubsessionRepository` 和 `SubagentEngine`，把创建、上下文组装、持久化和执行调度放在工具适配器里。
2. `SubagentEngine` 同时负责运行中任务表、执行、Subsession/Subagent 查询、父上下文重建和状态持久化，职责偏宽。
3. `SubagentUseCase` 仍保留 REST CRUD 风格的创建/更新/停用能力，和“Subagent 只由 Agent 运行时创建、REST 只读监控”的目标不一致。
4. `SubagentManageTools` 多个方法超过 10 行，不符合项目约束。
5. 工具返回值格式由工具层拼接，应用层没有统一的运行结果 DTO。

## 优化方案

### 1. 新增应用层命令和输出

新增：

- `RunSubagentCommand`
- `SubagentRunOutput`

命令包含父 `ReActAgentContext`、名称、系统提示词、任务、工具列表、知识库列表和可选模型配置。

输出包含：

- `subagentId`
- `subsessionId`
- `status`

### 2. 新增 SubagentRuntimeUseCase

新增 `SubagentRuntimeUseCase`，集中处理运行时工具需要的用例：

- `run(RunSubagentCommand)`：创建 Subagent、创建 Subsession、组装子上下文、调用 `SubagentExecutionPort.execute(...)`。
- `list(ReActAgentContext)`：按当前父会话列出 Subagent。
- `status(String)`：查询单个 Subagent 状态。
- `stop(String, String)`：停止执行并保存 `INTERRUPTED`。
- `messages(String)`：读取 Subagent 对话历史。

工具层只调用该 UseCase，不再直接依赖仓储或执行引擎。

### 3. 调整执行端口

`SubagentExecutionPort` 增加：

- `void execute(Subagent subagent, Subsession subsession, ReActAgentContext context, String input)`
- `void stop(Subagent subagent, String subsessionId)`

`stream(SubAgentChatCommand)` 保持给 Subsession API 使用。

这样应用层只依赖端口，基础设施 `SubagentEngine` 作为实现。

### 4. 收敛工具适配器

`SubagentManageTools` 变成薄适配器：

- 从 `ToolContext` 取父 Agent 上下文。
- 构造 `RunSubagentCommand`。
- 调用 `SubagentRuntimeUseCase`。
- 格式化简单字符串返回。

工具类不创建领域对象，不操作仓储，不知道执行引擎。

### 5. 保持 SubagentEngine 为基础设施实现

`SubagentEngine` 继续负责运行时细节：

- 内存中的运行任务表。
- Agent 实例创建。
- 后台订阅执行。
- SSE 续聊时按数据库重建 Agent。
- 调用底层 Agent interrupt。

本轮不拆出额外运行任务仓储，避免扩大范围。

## 不做事项

- 不新增 REST 创建/更新 Subagent 能力。
- 不实现递归 Subagent 树展示。
- 不引入新的持久化任务表。
- 不改前端交互。

## 验证

1. `gradle compileJava`
2. `gradle test --tests "*SubagentIntegrationTest*"`
3. 如架构测试可用，再运行 `gradle test --tests "*ArchTest*"`
