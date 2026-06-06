# Block 5 前端实现方案
> 日期：2026-06-06 · 状态：实施中

## 1. 需求概述
为 Apache Camel 数据源方案（Block 1-4 已完成）实现 Vue 3 + TypeScript 前端：
- 4 个 View 页面：AgentDataSourceView / AuditLogView（顶层路由）
- 2 个内嵌组件：SchemaPanel / PermissionStrategyPanel（AgentDataSource 详情抽屉）
- 浮动新增按钮 + 路由 + 类型 + API 客户端

## 2. 架构设计

### 2.1 新增文件清单

| 路径 | 用途 |
|------|------|
| `src/main/web/src/types/agent-data-source.ts` | 数据源/Schema/Table/Column 类型 |
| `src/main/web/src/types/permission-strategy.ts` | 权限策略类型 |
| `src/main/web/src/types/audit-log.ts` | 审计日志类型 |
| `src/main/web/src/api/agent-data-source-api.ts` | 数据源 CRUD + enable/disable + test + invoke |
| `src/main/web/src/api/agent-data-source-component-api.ts` | 协议描述符 |
| `src/main/web/src/api/data-source-schema-api.ts` | Schema CRUD |
| `src/main/web/src/api/permission-strategy-api.ts` | 权限策略 CRUD |
| `src/main/web/src/api/audit-log-api.ts` | 审计日志查询 |
| `src/main/web/src/views/agenthub/AgentDataSourceView.vue` | 数据源列表/详情/测试 |
| `src/main/web/src/views/agenthub/AuditLogView.vue` | 审计日志查询 |
| `src/main/web/src/components/datasource/SchemaPanel.vue` | Schema 抽屉（表 + 字段） |
| `src/main/web/src/components/strategy/PermissionStrategyPanel.vue` | 权限策略抽屉（数据源详情内嵌） |
| 更新：`src/main/web/src/router/index.ts` | 2 路由 |
| 更新：`src/main/web/src/components/AgentHubLayout.vue` | showAddButton 已包含 `agent-data-sources/permission-strategies`（**已完成**） |

### 2.2 路由映射
- `/agenthub/agent-data-sources` → AgentDataSourceView
- `/agenthub/audit-logs` → AuditLogView

## 3. API 设计

### 3.1 AgentDataSource（已存在）
- `GET    /api/v1/workspaces/{w}/agent-data-sources` → list
- `GET    /api/v1/workspaces/{w}/agent-data-sources/{id}` → get
- `POST   /api/v1/workspaces/{w}/agent-data-sources` → create
- `PATCH  /api/v1/workspaces/{w}/agent-data-sources/{id}` → update
- `POST   /api/v1/workspaces/{w}/agent-data-sources/{id}/enable` → enable
- `POST   /api/v1/workspaces/{w}/agent-data-sources/{id}/disable` → disable
- `DELETE /api/v1/workspaces/{w}/agent-data-sources/{id}` → delete
- `POST   /api/v1/workspaces/{w}/agent-data-sources/{id}/test` → test
- `POST   /api/v1/workspaces/{w}/agent-data-sources/{id}/invoke` → invoke

### 3.2 Schema
- `GET    /api/v1/workspaces/{w}/agent-data-sources/{id}/schema` → get
- `PUT    /api/v1/workspaces/{w}/agent-data-sources/{id}/schema` → replace
- `POST   /api/v1/workspaces/{w}/agent-data-sources/{id}/schema/introspect` → introspect
- `POST   /api/v1/workspaces/{w}/agent-data-sources/{id}/schema/tables` → addTable
- `DELETE /api/v1/workspaces/{w}/agent-data-sources/{id}/schema/tables/{t}` → deleteTable

### 3.3 Component
- `GET    /api/v1/agent-data-source-components` → listComponents

### 3.4 PermissionStrategy
- `GET    /api/v1/workspaces/{w}/permission-strategies` → list
- `GET    /api/v1/workspaces/{w}/permission-strategies/{id}` → get
- `PUT    /api/v1/workspaces/{w}/permission-strategies` → upsert
- `DELETE /api/v1/workspaces/{w}/permission-strategies/{id}` → delete

### 3.5 AuditLog（租户级）
- `GET    /api/v1/audit-logs?resourceType=...&actorId=...&action=...&from=...&to=...&page=...&size=...` → query
- `GET    /api/v1/audit-logs/resource-types` → listResourceTypes
- `GET    /api/v1/audit-logs/actions` → listActions

## 4. 边界情况
- [ ] workspace 未选 → 显示提示
- [ ] invoke 后端无 JDBC 驱动 → 200 + success=false（不弹错）
- [ ] schema introspect 失败 → 显示 500 错误信息
- [ ] 权限策略名称重复 → 显示 409 Conflict
- [ ] 审计日志分页为空 → 显示"无数据"

## 5. 检查清单
- [ ] 不使用 emoji
- [ ] 每个 .vue 单文件 < 500 行
- [ ] 中文 Javadoc 注释
- [ ] 类型严格（无 any）
- [ ] 复用 `ModalDialog` / `CustomButton` / `CustomSelect` 组件
- [ ] 监听 `global-add` 事件触发新增弹窗
- [ ] `useWorkspaceStore` 提供 tenantId/workspaceId

## 完成情况
- [ ] AgentDataSourceView 列表/CRUD/详情抽屉
- [ ] AuditLogView 查询 + 过滤 + 分页
- [ ] SchemaPanel 抽屉
- [ ] PermissionStrategyPanel
- [ ] 路由 + 类型 + API
- [ ] frontend build 通过

## 反思
- 遇到的问题：
- 解决方案：
