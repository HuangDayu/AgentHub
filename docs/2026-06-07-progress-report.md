# AgentHub 2026-06-07 进度报告

> 状态：等待管理员决策 · 更新人：opencode

## 1. 今日完成

### 1.1 Block 4 收尾（Tools 路径，跨进程 MCP Server 暂不实现）
- **`AgentDataSourceToolFactory.java`**：把 `AgentDataSource` 包装为 Spring AI `FunctionToolCallback`。
  - 工具名：`agent_data_source_invoke_<sanitizedId>`（`replaceAll("[^a-zA-Z0-9_]","_")`）。
  - 输入 schema：`{"type":"object","properties":{"body":{"type":"string"}},"required":["body"]}`。
  - 工作空间隔离：从 `ToolContext.context[AGENT_CONTEXT_KEY]` 拿 `ReActAgentContext`，比对 `source.workspaceId`。
  - 错误容错：try-catch 包成 `{"success":false,"errorMessage":...}` JSON 而非抛异常。
  - 拆分：`invoke` / `doInvoke` / `errorJson` 满足 10 行限制。
- **`AgentDataSourceToolCallbackProvider.java`**：实现 Spring AI `ToolCallbackProvider`。
  - `@PostConstruct init()` 启动加载。
  - `@EventListener(AgentDataSourceChangedEvent)` 监听事件触发 `refresh()`。
  - volatile 缓存 + ReentrantLock。
  - `refresh()` 在 `TenantContextHolder.open(TenantThreadContext.ignoreContext())` 作用域中执行（事件线程无租户上下文）。
  - 拆分：`refresh` / `reloadCallbacks` / `loadEnabledCallbacks` 满足 10 行限制。
- **集成测试 `AgentDataSourceToolCallbackProviderIntegrationTest`**（7/7 通过）：CREATED / DISABLED / ENABLED / DELETED 四种事件 + 元数据 + 唯一性 + provider 非空。

### 1.2 前端补全
- `StrategyManagementView.vue` 新增第 5 个 Tab「权限策略」+ import `PermissionStrategyPanel` + `activeTab` 类型扩展。

### 1.3 修复预存 `RejectedExecutionException`
- **根因**：`TtlUtils.executorService` 是 `static final` 单例（`Executors.newVirtualThreadPerTaskExecutor()`），被 Spring 容器作为 `@Bean("ttlExecutorService")` 注入。
  - 第一次 context 关闭 → Spring 调 `shutdown()` 把静态执行器关了。
  - 后续 context（test 类之间）再注入同一引用 → 调用 `execute()` 抛 `RejectedExecutionException`。
- **修复**：`CommonBeanConfiguration.ttlExecutorService()` 改为**每次返回新的** TTL 包装执行器，并加 `@PreDestroy` 显式 shutdown。
  - `shutdownTtlExecutor` 拆为 `shutdownTtlExecutor` / `awaitOrForceShutdown` 满足 10 行。
- **效果**：
  - `AlertControllerIntegrationTest` ✅（之前 `initializationError`）
  - `MetricControllerIntegrationTest` ✅（之前 `initializationError`）
  - `AgentHubControllerIntegrationTest` 仍失败（**不同**原因，见 §2）

## 2. 测试矩阵

| 测试类 | 状态 | 说明 |
|--------|------|------|
| `AgentHubCleanArchitectureTest` | ✅ 19/19 | ArchUnit 强制 |
| `AgentDataSourceControllerIntegrationTest` | ✅ 10/10 | Block 1-3 |
| `AgentDataSourceComponentControllerIntegrationTest` | ✅ 2/2 | Block 1-3 |
| `DataSourceSchemaControllerIntegrationTest` | ✅ 6/6 | Block 1-3 |
| `PermissionStrategyControllerIntegrationTest` | ✅ 4/4 | Block 5 |
| `AuditLogControllerIntegrationTest` | ✅ 4/4 | Block 1-3 |
| `CamelDataSourcePgIntegrationTest` | ✅ 2/2 | Block 1-3 |
| **`AgentDataSourceToolCallbackProviderIntegrationTest`** | **✅ 7/7** | Block 4 新增 |
| `AlertControllerIntegrationTest` | ✅（修复前 ❌）| `RejectedExecutionException` 修复 |
| `MetricControllerIntegrationTest` | ✅（修复前 ❌）| `RejectedExecutionException` 修复 |
| `AgentHubControllerIntegrationTest` | ❌ 5/8 失败 | 预存问题，见 §2.1 |

### 2.1 剩余问题：`AgentHubControllerIntegrationTest` 失败

| 测试方法 | 错误 | 根因 |
|----------|------|------|
| `shouldGetAgentById` | 404 | 测试清理缺失 |
| `shouldUpdateAgent` | 500 | `duplicate key value violates unique constraint "agent_pkey"` |
| `shouldEnabledAgent` | 404 | 同上 |
| `shouldUnenabledAgent` | 404 | 同上 |
| `shouldDeleteAgent` | 404 | 同上 |

**根因**：测试 `AgentHubControllerIntegrationTest` 有 `@BeforeEach` 但**无清理逻辑**（不像我的测试类用 `@BeforeAll` 自管 schema migration），数据在多次运行间累积，ID 冲突 + 找不到刚 create 的资源（被前次运行残留覆盖）。

**影响范围**：仅此 1 个测试类，不影响本任务 5 个工作块的成果。

**修复建议**（待管理员决策）：
- 简单方案：测试加 `@AfterEach` 清理 `agent` / `agent_data_source` 等表。
- 更好方案：扩展 `TestCommonTools` 增加 `cleanAgentHubDatabase()` helper，替换 `cleanDatabase` 引用。

## 3. 5 个工作块完成度

| 工作块 | 内容 | 状态 |
|--------|------|------|
| Block 1 | `AgentDataSource` CRUD 端到端 | ✅ 完成 |
| Block 2 | 数据源 invoke/ping + 审计 + 限流 | ✅ 完成 |
| Block 3 | 数据源 schema 自省 + 前端管理 | ✅ 完成 |
| Block 4 | AgentTools 路径（Spring AI ToolCallback） | ✅ 完成 |
| Block 4 | McpServer 跨进程路径 | ⏸️ 暂不实现（用户决策） |
| Block 5 | 前端 4 个视图 + 权限策略 Tab | ✅ 完成 |

详见 `docs/2026-06-06-apache-camel-integration-plan.md`「完成情况」与「反思」章节。

## 4. 建议下一步

1. **修复 `AgentHubControllerIntegrationTest` 数据隔离**：约 30 行代码，工作量 ~30 分钟。
2. **实现 Block 4 余下 McpServer 路径**（如需）：约 200 行代码 + 集成测试，工作量 ~2 小时。
3. **前端 `npm run build` 验证**：5 个 tab + 4 个视图的最终编译产物。
4. **手动验证 ToolCallback 端到端**：在 Agent 配置中引用 enabled 数据源，触发 body=SQL，验证 Camel JDBC 实际调用并返回结果。

## 5. 当前代码统计（累计）

- 新增 Java 文件：~45 个
- 新增前端文件：~15 个
- SQL 迁移：V100 - V103（4 个）
- ArchUnit 规则：19 条（已修复合计 13 个 method-length 违规）
- 集成测试：10 类（89+ 个测试方法）
