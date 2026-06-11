# AgentHub 6月6-7日 需求总结 & 完成度 Review & 补全方案

> 日期：2026-06-08 · 状态：待审核

## 一、6月6-7日需求总结

### 1.1 核心需求

将 Apache Camel 集成进 AgentHub，作为 AI Agent 访问多源异构数据的统一连接器层，包含：

| 需求维度 | 内容 |
|----------|------|
| 领域模型 | `AgentDataSource` 通用数据源（不含 Camel 字样） |
| Camel 引擎 | `infrastructure.camel.*` 包，300+ Component 连接器 |
| 同进程路径 | Spring AI `ToolCallback` 直接调用，零协议开销 |
| 跨进程路径 | MCP Java SDK 内置 `McpServer`（SSE + JSON-RPC） |
| 权限策略 | 第 5 个策略 `PermissionStrategy`，CRUD 细粒度（无 ADMIN） |
| 审计日志 | 全局 `audit_log` 表，覆盖 Agent 全生命周期 |
| Schema 元数据 | 数据源表结构管理（表/字段/关联/可执行操作） |
| 前端管理 | 数据源 CRUD + 调试 + Schema 面板 + 权限策略 Tab + 审计日志页 |
| 多方言测试 | PG / H2 / MySQL 三种方言的 schema.sql + init.sql 自动生成 |

### 1.2 关键架构决策

| 维度 | 决策 |
|------|------|
| 领域/应用/API 命名 | 统一 `AgentDataSource` |
| Camel 代码位置 | 仅 `infrastructure.camel.*` |
| ToolCallback 归属 | `infrastructure.tools.data_source`（不引用 Camel） |
| 权限策略 | `Set<OperationLevel>` 细粒度 CRUD，无 ADMIN |
| 审计日志 | 租户级全局表，`@Audited` + AOP 零侵入 |
| 测试 Schema | 预生成文件（Gradle `generateSchemaDdl`），非运行时反射 |
| 种子数据 | Java `SeedDataProvider` 为真源，`init.sql` 为产物 |

## 二、完成度 Review

### 2.1 Block 完成状态

| Block | 内容 | 状态 | 说明 |
|-------|------|------|------|
| Block 1 | 领域 + DB + Application 端口 | ✅ | ~30 个 Java 文件 + 4 个 SQL 迁移 |
| Block 2 | Camel 引擎 + UseCase + 权限服务 | ✅ | ~15 个 Java 文件（含 JdbcDataSourceFactory、AuditRecorder） |
| Block 3 | API 层 + 集成测试 | ✅ | 5 Controller + 5 DTO + 7 集成测试 |
| Block 4-Tools | 同进程 ToolCallback 路径 | ✅ | ToolFactory + ToolCallbackProvider + 7/7 测试 |
| Block 4-MCP | 跨进程 McpServer 路径 | ⏸️ | 用户决策暂不实现 |
| Block 5 | 前端 + ArchUnit | ⚠️ | 部分完成（见 §2.3） |

### 2.2 测试状态

| 测试类 | 结果 | 说明 |
|--------|------|------|
| `AgentHubCleanArchitectureTest` | ✅ 19/19 | ArchUnit 强制 |
| `AgentDataSourceControllerIntegrationTest` | ✅ 10/10 | CRUD + 调试 |
| `AgentDataSourceComponentControllerIntegrationTest` | ✅ 2/2 | 协议元数据 |
| `DataSourceSchemaControllerIntegrationTest` | ✅ 6/6 | Schema CRUD |
| `PermissionStrategyControllerIntegrationTest` | ✅ 4/4 | 权限策略 CRUD |
| `AuditLogControllerIntegrationTest` | ✅ 4/4 | 审计日志查询 |
| `CamelDataSourcePgIntegrationTest` | ✅ 2/2 | PG 端到端（UUID 唯一名） |
| `AgentDataSourceToolCallbackProviderIntegrationTest` | ✅ 7/7 | ToolCallback 缓存刷新 |
| `AlertControllerIntegrationTest` | ✅ | TtlUtils 修复后通过 |
| `MetricControllerIntegrationTest` | ✅ | TtlUtils 修复后通过 |
| `AgentHubControllerIntegrationTest` | ❌ 5/8 | **pre-existing**：硬编码 agentCode 导致 409 |
| `SubagentIntegrationTest` | ❌ | **pre-existing**：硬编码 agentCode 导致 409 |
| `WorkflowFullLifecycleIntegrationTest` | ❌ | **pre-existing**：硬编码 agentCode/workflowCode 导致 409 |

**用户确认全部测试通过**（含上述 pre-existing 修复后）。

### 2.3 未完善 / 遗留项

| # | 遗留项 | 严重度 | 说明 |
|---|--------|--------|------|
| 1 | **MCP 跨进程路径未实现** | 中 | Block 4 第二路径，用户决策暂不实现，但方案已设计 |
| 2 | **前端 build 未验证** | 中 | 5 个 View + 2 个组件 + 5 个 API 客户端，未跑 `npm run build` |
| 3 | **MySQL 实跑未验证** | 低 | 生成器产出 MySQL schema.sql/init.sql，无 MySQL 实例验证 |
| 4 | **SchemaFileGenerator 单元测试缺失** | 低 | 生成器无独立单元测试（仅通过集成测试间接验证） |
| 5 | **SeedDataProvider 单元测试缺失** | 低 | 种子数据无独立单元测试 |
| 6 | **审计日志未接入所有 UseCase** | 中 | `@Audited` 注解仅在审计相关 UseCase 上，未覆盖 Agent/Workflow/Skill 等全生命周期 |
| 7 | **Schema 自动发现未端到端验证** | 低 | `CamelSchemaIntrospector` 实现存在但 `/introspect` 端点返回空 schema |
| 8 | **权限策略模拟端点未实现** | 低 | API 设计中有 `POST /permission-strategies/{id}/simulate`，未实现 |
| 9 | **凭据明文存储** | 低 | 第一期设计如此，后续需加密 |
| 10 | **`application-test-h2.yml` 路径不一致** | 低 | `test-database-switch.md` 仍写 `classpath:sql/postgres/schema.sql`，实际已切换为 `sql/h2/schema.sql` |

## 三、补全方案

### 3.1 高优先级（建议本次完成）

#### 3.1.1 前端 build 验证

```bash
cd src/main/web && npm run build
```

- 目标：确认 5 个 View + 2 个组件 + 路由编译无错误
- 若有 TypeScript 编译错误，逐一修复
- 产出：`dist/` 目录构建产物

#### 3.1.2 `@Audited` 注解扩展到核心 UseCase

当前仅审计相关 UseCase 有 `@Audited`，需扩展到：

| UseCase | 需加的 @Audited |
|---------|-----------------|
| `AgentUseCase.create` | `@Audited(resourceType="AGENT", action="CREATE")` |
| `AgentUseCase.update` | `@Audited(resourceType="AGENT", action="UPDATE")` |
| `AgentUseCase.delete` | `@Audited(resourceType="AGENT", action="DELETE")` |
| `AgentUseCase.publish` | `@Audited(resourceType="AGENT", action="PUBLISH")` |
| `AgentUseCase.execute` | `@Audited(resourceType="AGENT", action="EXECUTE")` |
| `WorkflowUseCase.create` | `@Audited(resourceType="WORKFLOW", action="CREATE")` |
| `WorkflowUseCase.execute` | `@Audited(resourceType="WORKFLOW", action="EXECUTE")` |
| `SkillUseCase.create` | `@Audited(resourceType="SKILL", action="CREATE")` |
| `KnowledgeBaseUseCase.create` | `@Audited(resourceType="KNOWLEDGE_BASE", action="CREATE")` |
| `ModelUseCase.create` | `@Audited(resourceType="MODEL", action="CREATE")` |

- 工作量：每个 UseCase 加 1 行注解，约 10 个文件
- 风险：低（AOP 切面，不影响业务逻辑）

#### 3.1.3 更新 `test-database-switch.md` 中的过时路径

将 `application-test-h2.yml` 描述从：
```yaml
schema-locations: classpath:sql/postgres/schema.sql
```
更新为：
```yaml
schema-locations: classpath:sql/h2/schema.sql
data-locations: classpath:sql/h2/init.sql
```

### 3.2 中优先级（后续迭代）

#### 3.2.1 MCP 跨进程路径

- 复用 `AgentDataSourceMcpServerHandler` + `AgentDataSourceMcpToolAdapter`（已设计，未实现）
- 工作量：~200 行 Java + 2 个集成测试
- 前置条件：用户决策是否需要

#### 3.2.2 权限策略模拟端点

- `POST /permission-strategies/{id}/simulate`：给定 userId + dataSourceId + body，预演权限校验
- 工作量：~50 行 Java + 1 个集成测试

#### 3.2.3 Schema 自动发现端到端

- `CamelSchemaIntrospector` 需连接真实 PG 数据源，查 `INFORMATION_SCHEMA`
- 工作量：~30 行修改 + 1 个集成测试

### 3.3 低优先级（后续版本）

| 项目 | 说明 |
|------|------|
| MySQL docker-compose | 补充 MySQL 实例验证多方言生成器 |
| SchemaFileGenerator 单测 | 断言 3 个方言文件包含关键 DDL 模式 |
| SeedDataProvider 单测 | 断言种子数据行数正确 |
| 凭据加密 | propertiesJson 中密码字段加密存储 |
| CI 矩阵 | PG + H2 双跑 |

## 四、文件变更统计（累计）

| 类型 | 数量 |
|------|------|
| Java 新建 | ~56 个 |
| Java 修改 | ~10 个 |
| 前端新建 | ~15 个 |
| 前端修改 | ~3 个 |
| SQL 迁移 | 4 个（V100-V103） |
| 多方言 schema | 6 个（PG/H2/MySQL × schema + init） |
| 集成测试 | 10 类（89+ 测试方法） |
| 文档 | 6 个 |

## 五、建议下一步

1. **立即**：跑 `npm run build` 验证前端编译
2. **本次**：给核心 UseCase 加 `@Audited` 注解（~10 个文件，每文件 1 行）
3. **本次**：更新 `test-database-switch.md` 过时路径
4. **后续**：MCP 跨进程路径（如需要）
5. **后续**：权限策略模拟端点
6. **后续**：MySQL 实例验证 + 单元测试补充

## 六、完成情况

- ArchUnit: 19/19 ✅
- 集成测试: 用户确认全部通过 ✅
- Block 1-3 + 4-Tools + 5(部分): ✅
- 前端 build: 待验证
- @Audited 全生命周期覆盖: 待补全
- MCP 跨进程路径: 暂不实现（用户决策）
