# AgentScope v2 升级方案

> 日期：2026-06-10 · 状态：待审核

## 1. 需求概述

将项目中的 AgentScope 框架从 `1.1.0-RC2` 升级到 `2.0.0-RC2`，修复因 API 不兼容导致的编译和运行时错误，并利用 v2 新能力完善 Agent 相关功能。

## 2. 升级影响总览

| 指标 | 数值 |
|------|------|
| 当前版本 | `1.1.0-RC2` |
| 目标版本 | `2.0.0-RC2` |
| 使用 AgentScope API 的文件数 | 27 个（24 个 aliyun 包 + 3 个 telemetry 包） |
| 自定义 `io.agentscope.*` 扩展文件 | 7 个（可能与 v2 内置类冲突） |
| 测试覆盖 | 0 |

### 2.1 Breaking Change 分类

| 类别 | 影响文件数 | 说明 |
|------|-----------|------|
| **Part A — 必须迁移**（编译失败） | ~15 | Hook→Middleware, Msg 构造, Session→AgentStateStore, PlanNotebook 删除, Knowledge 弃用, Agent 无状态化 |
| **Part B — 推荐迁移**（Deprecated） | ~20 | stream()→streamEvents(), Memory→AgentStateStore, SkillBox→SkillRepository, RAG/LongTermMemory 弃用 |
| **自定义扩展冲突** | 7 | `io.agentscope.core.util.*`, `io.agentscope.core.studio.*`, `io.agentscope.core.tracing.*` 可能与 v2 同名类冲突 |
| **Maven 坐标变更** | 1 | `agentscope-extensions-studio` 可能在 v2 中已重构 |

## 3. 架构设计

### Phase 1：依赖与基础设施（无代码改动层）

| 层 | 文件 | 操作 |
|----|------|------|
| build | `build.gradle` | 修改 agentscopeVersion → 2.0.0-RC2，调整 gRPC 策略，检查 extension 坐标 |
| infra | `io.agentscope.*` (7 files) | 评估与 v2 内置类的冲突，决定保留/删除/重命名 |

### Phase 2：Part A 强制迁移（编译修复）

| 层 | 文件 | 操作 |
|----|------|------|
| infra/hook | `ToolStrategyHook.java` | Hook→Middleware 迁移 |
| infra/hook | `RetrievalStrategyHook.java` | Hook→Middleware 迁移 |
| infra/session | `SessionFactory.java` | InMemorySession/JsonSession → AgentStateStore |
| infra/memory | `MemoryConfigFactory.java` | Memory → AgentStateStore + CompactionConfig |
| infra/knowledge | `AgentScopeKnowledge.java` | Knowledge 接口弃用，评估替代方案 |
| infra/agent | `AgentScopeHarnessAgent.java` | Msg 构造重构、stream()→streamEvents()、无状态适配 |
| infra/agent | `AgentScopeHarnessAgentFactory.java` | Builder 链更新（移除知识/记忆，增加 stateStore） |
| infra/agent | `AgentScopeSpringModelAdapter.java` | Msg 构造重构 |
| infra/agent | `MsgToSpringMessageConverter.java` | 无变化（仅读取 Msg 属性） |
| infra/agent | `AgentScopeReActAgentConfig.java` | Model/Tool 类型检查 |
| infra/model | 全部 6 个 Model 工厂 | Model 接口检查 |
| infra/tools | 全部 3 个工具文件 | Toolkit 构造检查 |
| infra/telemetry | `OpenTelemetryConfiguration.java` | TracerRegistry/StudioManager API 检查 |
| infra/telemetry | `LoggingAgentStudioMessageHandler.java` | Studio POJO 包路径检查 |

### Phase 3：Part B 推荐迁移（消除 Deprecated）

| 层 | 文件 | 操作 |
|----|------|------|
| infra/agent | `AgentScopeHarnessAgent.java` | stream()→streamEvents() 迁移 |
| infra/agent | `AgentScopeHarnessAgentFactory.java` | skillBox→skillRepository |
| infra/telemetry | 全部 | Hook→Middleware、streamEvents() 适配 |

### Phase 4：利用 v2 新能力完善 Agent 功能

| 层 | 文件 | 操作 |
|----|------|------|
| infra/agent | 新增 `PermissionMiddleware` | 利用 v2 Permission 系统实现工具权限管控 |
| infra/agent | 新增 `TemplateMiddleware` | 利用 v2 Middleware 替代旧 Hook |
| infra/agent | `AgentScopeHarnessAgentFactory.java` | 集成 ModelRegistry/ModelCard 简化模型配置 |
| test | 新增 `*AgentScopeV2Test.java` | 补齐 AgentScope 集成测试 |

### Phase 5：验证

| 层 | 操作 |
|----|------|
| 整体 | `gradle compileJava` 确认编译通过 |
| 整体 | `gradle test --tests "*AgentHubCleanArchitectureTest*"` ArchUnit 17/17 |
| 整体 | `gradle test` 全部集成测试通过 |

## 4. 详细变更清单

### 4.1 build.gradle 变更

```groovy
// 版本升级
agentscopeVersion = '2.0.0-RC2'

// 依赖调整
implementation "io.agentscope:agentscope-core:${agentscopeVersion}"
implementation "io.agentscope:agentscope-harness:${agentscopeVersion}"
// agentscope-extensions-studio 可能已更名或合并到 core
// 需验证后确定
```

#### gRPC 版本策略
v2 可能已解决 gRPC 依赖冲突，需在升级后评估是否仍需要 `resolutionStrategy` 强制锁定。

### 4.2 自定义 io.agentscope.* 文件处理策略

| 文件 | 冲突风险 | 处理方案 |
|------|---------|---------|
| `io.agentscope.core.studio.StudioClient` | **高** — v2 可能有内置 StudioClient | 重命名为 `CustomStudioClient` 或删除改用 v2 内置 |
| `io.agentscope.core.util.JsonCodec` | **高** — v2 内置同名接口 | 检查兼容性，删除自定义版本 |
| `io.agentscope.core.util.JacksonJsonCodec` | **高** — v2 内置同名类 | 同上 |
| `io.agentscope.core.util.JsonUtils` | **高** — v2 内置同名类 | 同上 |
| `io.agentscope.core.util.JsonSchemaUtils` | **中** — v2 可能有替代 | 评估后适配 |
| `io.agentscope.core.tracing.telemetry.AttributesExtractors` | **中** — v2 可能有内置方案 | 保留但适配新 API |
| `io.agentscope.core.tracing.telemetry.GenAiIncubatingAttributes` | **中** — v2 可能有内置常量 | 检查版本一致性 |

**推荐策略**：升级后编译期会直接暴露冲突（包级类重复），逐个处理。

### 4.3 Part A 具体 API 迁移

#### A.1 Hook → Middleware（优先级最高）

```java
// v1 (当前)
public class ToolStrategyHook implements Hook {
    @Override
    public int priority() { return 50; }
    @Override
    public void onEvent(HookEvent event) { ... }
}

// v2 方式
public class ToolStrategyMiddleware implements MiddlewareBase {
    @Override
    public Flux<AgentEvent> onActing(...) { ... }
    @Override
    public Flux<AgentEvent> onReasoning(...) { ... }
}
```

**影响文件**：
- `ToolStrategyHook.java` → 迁移到 `ToolStrategyMiddleware`
- `RetrievalStrategyHook.java` → 迁移到 `RetrievalStrategyMiddleware`
- `AgentScopeHarnessAgentFactory.java` → `.hook(...)` → `.middleware(...)`

#### A.2 Msg 构造重构

```java
// v1 (当前)
Msg.builder().role(MsgRole.USER).textContent("hello").build()
Msg.builder().role(MsgRole.SYSTEM).textContent("system").build()

// v2
new UserMessage("hello")
new SystemMessage("system")
new AssistantMessage("reply")
```

**影响文件**：约 10 处 Msg 构造调用。

#### A.3 Session → AgentStateStore

```java
// v1 (当前)
new InMemorySession()
new JsonSession(path)

// v2
new InMemoryAgentStateStore()
new JsonFileAgentStateStore(path)
```

**影响文件**：`SessionFactory.java` 删除，`AgentScopeHarnessAgentFactory.java` 改造。

#### A.4 Memory → AgentStateStore + CompactionConfig

```java
// v1 (当前)
CompactionConfig.builder().triggerMessages(30).keepMessages(10)...

// v2 — 基本兼容，但 builder 方法名可能微调
// 关键变化：agent 不再 builder().memory(Memory)，改用 stateStore
```

**影响文件**：`MemoryConfigFactory.java`。

#### A.5 PlanNotebook 完全删除

```diff
- agentBuilder.planNotebook(planNotebook)
+ agentBuilder.enablePlanMode()
```

当前项目是否使用了 PlanNotebook？需全局搜索确认。

#### A.6 Agent 无状态化

```java
// v1
agent.getCurrentSessionId()  // 已删除

// v2 通过 RuntimeContext 获取
RuntimeContext ctx = RuntimeContext.builder().userId("").sessionId("").build()
agent.call(msgs, ctx)
```

### 4.4 模型工厂 API 检查

v2 中 `Model` 接口和 `OpenAIChatModel` builder 接口基本保持兼容，需编译验证。主要检查点：

- `OpenAIChatModel.builder().modelName().apiKey().baseUrl().stream().build()`
- `OllamaChatModel.builder().modelName().baseUrl().build()`
- `DeepSeekFormatter` 构造
- `GenerateOptions.builder()` 构造

### 4.5 工具系统 API 检查

v2 中 `Toolkit` 新增了 `registerTool(Object)` 注解式注册，`AgentTool` 接口基本兼容。需检查：

- `Toolkit` 构造（无参构造兼容）
- `SpringToolAdapter` 实现 `AgentTool` 接口（方法签名兼容）
- `ToolCallParam` 使用（新增 `getRuntimeContext()`）

### 4.6 遥测 API 检查

v2 中 `TracerRegistry`、`StudioManager`、`StudioConfig` 等类可能存在位置/方法变化。

## 5. 实施步骤

### Step 1: 依赖升级 + 编译探测

```bash
# 1. 修改 build.gradle agentscopeVersion
# 2. 删除 gRPC 强制锁定（先试不锁定）
# 3. 执行编译，记录所有错误
gradle compileJava > compile-errors.log 2>&1
```

### Step 2: 自定义扩展冲突处理

对 7 个 `io.agentscope.*` 文件的冲突，逐个处理：
- 若 v2 提供了等效内置类 → 删除自定义版本
- 若 v2 没有提供 → 保留但加入包名前缀避免冲突

### Step 3: Part A 逐文件修复

按优先级：
1. Hook → Middleware（2 个 hook 文件）
2. Session → AgentStateStore（SessionFactory）
3. Memory → AgentStateStore（MemoryConfigFactory）
4. Msg 构造重构（全局替换）
5. Builder 链更新（AgentScopeHarnessAgentFactory）
6. Knowledge 弃用处理（AgentScopeKnowledge）
7. Agent 无状态适配（AgentScopeHarnessAgent）
8. 遥测 API 适配

### Step 4: Part B 推荐迁移

1. stream() → streamEvents()
2. skillBox → skillRepository
3. 其他 @Deprecated API

### Step 5: 新增功能

1. 基于 Middleware 重新实现工具策略和检索策略
2. 利用 Permission 系统增强工具安全管控
3. 集成 ModelRegistry 简化模型配置

### Step 6: 测试

1. 补齐 AgentScope 集成测试
2. ArchUnit 校验
3. 功能验证

## 6. 边界情况

- [ ] v2 的 `agentscope-extensions-studio` 坐标变更 → 需要查 Maven 仓库确认
- [ ] 自定义 StudioClient 与 v2 内置 StudioClient 冲突 → 需要评估合并
- [ ] 自定义 JSON 工具 (`JsonCodec`/`JacksonJsonCodec`/`JsonUtils`) → v2 内置版本是否兼容
- [ ] gRPC 版本策略 → v2 是否已解决冲突，能否移除强制锁定
- [ ] `Knowledge` 弃用后，RAG 功能如何替代（v2 文档说新 RAG 在后续 minor 版本上线）
- [ ] `LongTermMemory` 弃用后，长期记忆功能如何替代
- [ ] AgentScopeTeamAgent/TeamAgentFactory 的循环依赖是否需要调整
- [ ] v2 中 `AgentEvent` 和 `Event` 的混用点（HarnessAgent.streamEvents() 暂时不转发子 agent 事件）

## 7. 风险与缓解

| 风险 | 概率 | 影响 | 缓解措施 |
|------|------|------|---------|
| Maven 坐标不兼容（extension 模块重构） | 高 | 构建失败 | 查 Maven 仓库确认 v2 的 artifact 列表 |
| 自定义 io.agentscope.* 包冲突 | 高 | 编译失败 | 重命名或删除自定义类 |
| v2 缺少某些 v1 API（如 RAG） | 中 | 功能缺失 | 自实现替代方案或等待 v2.x 更新 |
| gRPC 版本冲突 | 中 | 运行时错误 | 保留 force 策略或升级 gRPC 版本 |
| HarnessAgent builder 行为变化 | 中 | 运行时异常 | 逐个 builder 参数对比文档 |
| 现有业务代码使用 PlanNotebook | 低 | 编译失败 | 全局搜索确认 |

## 8. 检查清单

- [ ] 依赖版本升级（build.gradle）
- [ ] 自定义 io.agentscope.* 文件冲突解决
- [ ] Hook → Middleware 迁移（2 个文件）
- [ ] Session → AgentStateStore 迁移
- [ ] Memory → AgentStateStore 迁移
- [ ] Msg 构造重构（全局替换）
- [ ] Agent 无状态化适配
- [ ] Builder 链更新
- [ ] stream() → streamEvents() 迁移
- [ ] Studio 遥测适配
- [ ] 编译通过
- [ ] ArchUnit 17/17
- [ ] 集成测试通过

## 9. 完成情况

- ArchUnit: __/17 ⬜
- 集成测试: __/__ ⬜
- 编译通过: ⬜

## 10. 反思

- 升级前需确认：
  1. v2 的 Maven 坐标完整列表
  2. v2 是否已移除 gRPC 强制锁定需求
  3. 自定义 `io.agentscope.*` 文件与 v2 内置类的兼容性
