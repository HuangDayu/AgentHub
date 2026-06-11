# 2026-06-08 修复会话总结

## 1. AgentDataSourceToolFactory DTO 重构

**目标**：用强类型 DTO 替换 `optionsJson` 模糊参数，参考 `AutomationTools` 的 `@Tool/@ToolParam` 模式。

### 新建文件

| 文件 | 说明 |
|------|------|
| `infrastructure/tools/data_source/params/HttpOptions.java` | HTTP 请求参数（path/body/queryParams） |
| `infrastructure/tools/data_source/params/MongoOptions.java` | MongoDB 操作参数（query/document） |
| `infrastructure/tools/data_source/params/KafkaOptions.java` | Kafka 消息参数（topic/key/headers） |
| `infrastructure/tools/data_source/params/RedisOptions.java` | Redis 操作参数（value/args） |
| `infrastructure/tools/data_source/params/FileOptions.java` | 文件操作参数（content） |
| `infrastructure/tools/data_source/DataSourceToolAdapter.java` | 适配器：每种协议一个 `@Tool` 方法，参数 ≤3 |

### 修改文件

| 文件 | 变更 |
|------|------|
| `AgentDataSourceToolFactory.java` | 按协议分发到 `DataSourceToolAdapter` 方法，`FunctionToolCallback` 注册 |

### 设计要点

- 每种协议一个 DTO，字段清晰，无 `optionsJson` 模糊语义
- `DataSourceToolAdapter` 方法参数 ≤3（ArchUnit 规则）
- 工具命名前缀：`sql_query_`/`http_call_`/`mongo_op_`/`kafka_send_`/`redis_cmd_`/`file_op_`

---

## 2. 审计日志 tenant_id null 修复

**问题**：`@IgnoreTenantContext` 方法（如 `AgentPoolUseCase.executeTask()`）运行时上下文 `tenantId=null`，AuditAspect 不跳过 → AuditEvent.tenantId=null → PostgreSQL `NOT NULL` 约束冲突。

### 修复方案

| 文件 | 修复 |
|------|------|
| `AuditAspect.java` | `audit()` 方法增加 `shouldSkip()`，跳过 `@IgnoreTenantContext` 注解方法 |
| `AuditRecorder.java` | `insertWithTenantContext()` 增加 tenantId null 检查，null 时跳过插入 |

### ArchUnit 合规

原 `audit()` 12行 → 拆分为 `audit()` + `shouldSkip()` + `buildEvent()`，均 ≤10行。
原 `insertWithTenantContext()` 11行 → 拆分为 `insertWithTenantContext()` + `doInsert()`，均 ≤10行。

---

## 3. ToolStrategy null NPE 修复

**问题**：`ToolStrategyHook` 中 `context.getToolStrategy()` 返回 null，直接调用 `.beforeToolCall()` 导致 NPE，工作流执行失败。

### 修复方案

| 文件 | 修复 |
|------|------|
| `ToolStrategyHook.java` | `handleBeforeToolCall()` 和 `handleAfterToolCall()` 增加 `strategy == null` 守护，null 时直接 return |

---

## 测试结果

| 测试组 | 结果 |
|--------|------|
| ArchUnit 19/19 | ✅ 全部通过 |
| 集成测试 38/38 | ✅ 全部通过 |
| WorkflowFullLifecycleIntegrationTest | ✅ Step 9 通过（之前因 strategy null 失败） |
| AgentDataSourceToolCallbackProviderIntegrationTest | ✅ 14/14 通过 |
