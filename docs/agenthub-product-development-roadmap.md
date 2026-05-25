# AgentHub 后续开发与迭代演进计划

> 版本：2026-05-25  
> 适用范围：AgentHub 产品规划、技术演进、研发排期与质量治理  
> 基线认知：AgentHub 是企业级 AI Agent 全生命周期管理平台，采用 Java 21、Spring Boot、Vue 3、整洁架构、TDD 和 ArchUnit 规则，核心能力覆盖 Agent、Agent Team、运行视图、RAG、工具、策略、工作流、多租户与审计。

---

## 1. 总体目标

### 1.1 产品目标

AgentHub 后续应从“功能完备的 Agent 管理平台”演进为“企业级 AgentOps 平台”。重点不是继续堆叠页面，而是让 Agent 从创建、调试、运行、观测、评估到治理形成闭环。

核心产品目标：

| 目标 | 说明 | 衡量标准 |
| --- | --- | --- |
| Agent 可配置 | 非研发用户能创建、配置、测试 Agent | 80% 常见 Agent 场景无需改代码 |
| Agent 可运行 | 会话、任务、工作流可稳定执行 | 运行成功率、失败原因可追踪 |
| Agent 可观测 | Run、Trace、Span、Token、模型调用可查询 | 单次会话 3 秒内定位关键调用 |
| Agent 可评估 | 支持质量、成本、时延、安全维度评估 | 每个 Agent 有版本化评测报告 |
| Agent 可治理 | 权限、审计、策略、护栏可落地 | 企业环境可按租户和工作空间隔离 |
| Agent 可扩展 | 模型、工具、记忆、知识库、工作流插件化 | 新适配器不破坏核心架构 |

### 1.2 技术目标

| 目标 | 说明 |
| --- | --- |
| 保持整洁架构 | 严格维持 `api -> application -> domain <- infrastructure` 依赖方向 |
| 统一运行数据模型 | 运行视图以 Run、Trace、Span、Metric、Token 为统一模型，避免重复接口 |
| 降低前端复杂度 | 建立可复用组件和状态管理，减少页面级巨型组件 |
| 提升测试可信度 | Controller 集成测试、UseCase 单元测试、ArchUnit 必须常态化通过 |
| 支持规模化运行 | 运行数据按租户、工作空间、Agent、Session 分区查询和归档 |
| 便于企业部署 | 配置、迁移、监控、日志、备份、升级过程标准化 |

---

## 2. 当前能力基线

### 2.1 已具备能力

| 模块 | 当前状态 | 后续重点 |
| --- | --- | --- |
| Agent 管理 | 支持 Agent 配置与运行 | 强化版本、发布、回滚、运行模板 |
| Agent Team | 支持多 Agent 协作模型 | 增加团队运行观测与协作诊断 |
| 会话运行 | 支持聊天会话、流式消息 | 增强会话到 Run 的一致关联 |
| 运行视图 | 已接入运行、Token、Trace、Span 详情 | 完善树结构、筛选、错误定位、导出 |
| RAG/知识库 | 支持知识库、检索、向量配置 | 增加检索评测、召回分析、文档质量诊断 |
| 工具/MCP | 支持 HTTP 工具、MCP 工具、系统工具、技能 | 增加工具调试、权限、调用审计 |
| 策略管理 | 支持模型、工具、检索、护栏策略 | 增加策略版本、灰度和生效范围 |
| 工作流 | 支持 DAG 编辑和执行 | 增加运行历史、节点 Trace、失败恢复 |
| 多租户 | 支持租户和工作空间上下文 | 强化权限矩阵与资源隔离测试 |
| 架构治理 | 有 ArchUnit 和整洁架构约束 | 引入更多边界和命名规则检查 |

### 2.2 主要问题

| 问题 | 影响 | 优先级 |
| --- | --- | --- |
| 前端页面组件过大 | 运行视图、聊天、会话管理耦合，迭代风险高 | P0 |
| 运行数据模型仍需收敛 | Trace、Metric、Alert、Span 容易重复设计 | P0 |
| Controller 测试覆盖需持续补齐 | 新接口容易绕过集成测试要求 | P0 |
| 评测体系缺失 | Agent 质量无法量化比较 | P1 |
| 工作流观测不足 | DAG 执行失败后定位成本高 | P1 |
| 策略缺少版本治理 | 企业场景下变更不可追踪 | P1 |
| 部署和运维文档偏分散 | 交付成本较高 | P2 |

---

## 3. 产品路线图

### 3.1 V1.1：运行视图与 AgentOps 基线

目标：把当前“运行视图”打磨成可用于日常调试的核心入口。

产品范围：

| 功能 | 说明 | 验收标准 |
| --- | --- | --- |
| 会话入口运行视图 | 会话列表可直接打开当前会话运行视图 | 点击后自动加载 Run、Token、Trace |
| 运行信息表格 | 展示 Run ID、状态、耗时、Span、PID 等 | 数据为空时显示稳定占位 |
| Token 信息表格 | 展示总计、提示词、生成内容、平均值 | 与后端 DataView 响应一致 |
| Trace 树 | 使用简洁树结构展示 Span 层级 | 父子关系、倒序、展开收起正确 |
| Span 详情弹窗 | 点击树节点查看输入、输出、属性、事件 | 不挤占树列表空间 |
| 错误 Span 突出 | 失败节点醒目标识并可筛选 | 1 次点击定位失败 Span |
| 数据刷新 | 运行/追踪标签后提供刷新按钮 | 加载状态明确，不重复触发 |

后续增强：

| 功能 | 说明 |
| --- | --- |
| Trace 搜索增强 | 支持按模型、工具、状态、耗时区间过滤 |
| Trace 导出 | 导出 JSON 或 Markdown 调试报告 |
| Run 对比 | 对比两次运行的耗时、Token、模型调用差异 |
| 慢调用定位 | 自动标记耗时最长 Top N Span |

### 3.2 V1.2：Agent 配置、调试与发布闭环

目标：让 Agent 从配置到验证再到发布形成标准流程。

产品范围：

| 功能 | 说明 | 验收标准 |
| --- | --- | --- |
| Agent 草稿/发布版本 | 配置变更先保存草稿，再发布 | 支持版本列表和回滚 |
| Agent 调试面板 | 调试模型、工具、知识库、记忆组合 | 调试记录进入运行视图 |
| 配置差异对比 | 展示版本间模型、策略、工具差异 | 支持发布前确认 |
| 发布校验 | 发布前校验模型、工具、知识库可用性 | 不可用项阻止发布 |
| Agent 模板 | 提供客服、检索问答、工具执行等模板 | 新建 Agent 可基于模板 |

### 3.3 V1.3：知识库与 RAG 质量中心

目标：不仅能检索，还能诊断检索质量。

产品范围：

| 功能 | 说明 | 验收标准 |
| --- | --- | --- |
| 文档处理状态面板 | 展示解析、清洗、分块、向量化状态 | 单文档可查看失败原因 |
| 检索调试台 | 输入 Query，展示召回片段、得分、来源 | 支持策略切换对比 |
| 召回质量评测 | 建立 Query-Expected Chunk 数据集 | 输出 Recall@K、MRR 等指标 |
| Chunk 质量检查 | 发现过短、过长、重复、乱码 Chunk | 提供修复建议 |
| 知识库版本 | 文档集变更可追踪 | 支持版本回滚和重建索引 |

### 3.4 V1.4：工作流执行与观测

目标：让 DAG 工作流从可编辑走向可运营。

产品范围：

| 功能 | 说明 | 验收标准 |
| --- | --- | --- |
| 工作流运行历史 | 每次执行生成 Run | 可按状态、时间、触发者查询 |
| 节点级 Trace | 每个节点对应 Span 或子 Run | 点击节点查看输入输出 |
| 失败恢复 | 支持从失败节点重试 | 重试记录可追踪 |
| 参数面板 | 工作流输入参数结构化配置 | 执行前校验 |
| 发布和灰度 | 发布版本可灰度给部分 Agent/用户 | 有回滚入口 |

### 3.5 V1.5：评测、成本与治理

目标：支持企业级 Agent 质量管理。

产品范围：

| 功能 | 说明 | 验收标准 |
| --- | --- | --- |
| Agent Eval 数据集 | 问题、期望答案、评分规则管理 | 支持导入导出 |
| 自动评测任务 | 定时或发布前执行评测 | 输出通过率和明细 |
| 成本分析 | 按租户、工作空间、Agent、模型统计 Token 成本 | 可按日/月汇总 |
| 护栏报告 | 统计 PII、提示注入、违规输出 | 可定位原始运行 |
| 审计中心 | 记录配置变更、发布、执行、删除操作 | 支持按用户和资源查询 |

---

## 4. 开发计划

### 4.1 架构演进计划

| 阶段 | 任务 | 交付物 |
| --- | --- | --- |
| P0 | 固化运行数据聚合模型 | `RuntimeDataViewUseCase`、统一 DTO、集成测试 |
| P0 | 删除重复观测接口 | 明确保留 DataView，废弃重复 OTLP/Metric/Alert 聚合入口 |
| P0 | 拆分前端运行视图 | `RuntimePanel`、`RunInfoTable`、`TokenInfoTable`、`TraceTree`、`SpanDetailDialog` |
| P1 | 抽象 Agent 运行事件 | domain 中定义 Run/Span/Metric 领域模型边界 |
| P1 | 工作流执行观测接入 | Workflow Run 与 Runtime DataView 统一关联 |
| P2 | 多租户数据归档 | 按租户/工作空间分区归档运行数据 |

### 4.2 后端开发计划

#### 4.2.1 运行视图后端

| 任务 | 说明 | 测试要求 |
| --- | --- | --- |
| 完善 DataView 查询 | 支持按 Agent + Session 查询当前 Run | Controller 集成测试 |
| Span 树排序字段 | 后端保证 startTime/endTime 完整 | UseCase 单元测试 |
| Token 明细聚合 | total/avg/prompt/completion 按 run 聚合 | Repository + UseCase 测试 |
| 错误 Span 聚合 | 提供 error count、first error、slow spans | DataView 集成测试 |
| 查询性能优化 | 增加必要索引和分页 | SQL explain 记录 |

#### 4.2.2 Agent 发布与版本（暂缓）

> 当前决策：Agent 版本管理暂且不做。以下内容保留为后续规划，不进入当前开发范围。

| 任务 | 说明 | 测试要求 |
| --- | --- | --- |
| Agent 发布快照模型 | 记录配置快照、发布状态、发布人 | Domain 单元测试 |
| 发布 UseCase | 草稿转发布版本，支持回滚 | UseCase 测试 |
| 配置差异服务 | 比较模型、工具、策略、知识库变化 | 单元测试 |
| 发布校验器 | 校验依赖资源是否可用 | 集成测试 |

#### 4.2.3 RAG 质量中心

| 任务 | 说明 | 测试要求 |
| --- | --- | --- |
| RetrievalEvalSet | 管理检索评测集 | Controller 集成测试 |
| RetrievalEvalRun | 执行评测并保存结果 | UseCase 测试 |
| Chunk 质量扫描 | 检测重复、过短、过长、空内容 | Controller 集成测试 |
| 检索调试接口 | 返回召回片段、分数、策略信息 | 集成测试 |

#### 4.2.4 工作流观测

| 任务 | 说明 | 测试要求 |
| --- | --- | --- |
| WorkflowRun 模型 | 记录执行实例和状态 | Domain 测试 |
| NodeRun 模型 | 节点级输入、输出、错误、耗时 | Repository 测试 |
| Workflow Trace 适配 | 将节点运行映射为 Span | UseCase 测试 |
| 失败节点重试 | 从失败节点恢复执行 | 集成测试 |

### 4.3 前端开发计划

#### 4.3.1 前端重构优先级

| 优先级 | 任务 | 目标 |
| --- | --- | --- |
| P0 | 拆分 `RuntimeChatView.vue` | 降低单文件复杂度 |
| P0 | 抽取运行视图组件 | 提升可测试性和复用性 |
| P0 | 统一 API 错误显示 | 避免页面各自处理异常 |
| P1 | 建立 AgentOps 组件库 | 表格、状态标签、Trace 树、详情弹窗复用 |
| P1 | 引入前端单元测试 | Trace 树排序、过滤、展开逻辑可测试 |
| P2 | 可访问性优化 | 键盘操作、aria、焦点管理 |

建议组件结构：

```text
src/main/web/src/views/agenthub/
  RuntimeChatView.vue

src/main/web/src/components/runtime/
  RuntimePanel.vue
  RunInfoTable.vue
  TokenInfoTable.vue
  ModelInvocationList.vue
  TraceTree.vue
  TraceTreeRow.vue
  SpanDetailDialog.vue
  RuntimeRefreshButton.vue

src/main/web/src/composables/runtime/
  useRuntimeDataView.ts
  useTraceTree.ts
  useRuntimeSelection.ts
```

#### 4.3.2 前端体验规范

| 模块 | 规范 |
| --- | --- |
| 运行视图 | 保持紧凑、表格化、可扫描，不使用过重卡片 |
| Trace 树 | 使用树状文本结构，点击节点弹窗详情 |
| 表格 | 空值统一显示 `-`，数字统一 `formatNumber` |
| 错误 | 请求失败显示明确错误，不吞异常 |
| 加载 | 刷新按钮显示旋转状态，避免重复触发 |
| 删除/危险操作 | 必须确认，且不影响当前运行视图状态 |

### 4.4 数据库与数据治理计划

| 任务 | 说明 |
| --- | --- |
| 运行数据索引 | 为 runId、agentId、sessionId、workspaceId、timestamp 建索引 |
| Span 数据归档 | 长期运行数据按时间归档或冷热分层 |
| Token 成本表 | 保存模型价格快照，避免历史成本漂移 |
| 审计日志标准化 | 所有配置变更记录 actor、resource、diff |
| 数据清理任务 | 支持按租户配置运行数据保留周期 |

---

## 5. 质量与测试计划

### 5.1 必须长期保持的质量门禁

| 门禁 | 要求 |
| --- | --- |
| Java 编译 | `gradle compileJava` 必须通过 |
| 测试编译 | `gradle compileTestJava` 必须通过 |
| Controller 集成测试 | 新增/修改 Controller 必须覆盖 |
| ArchUnit | 分层依赖规则必须通过 |
| 前端构建 | `npm run build` 必须通过 |
| 方法长度 | Java 方法不超过 10 行 |
| 架构依赖 | 禁止 application 依赖 infrastructure |
| 领域纯净 | domain 禁止依赖 Spring、HTTP、数据库框架 |

### 5.2 推荐测试矩阵

| 类型 | 覆盖对象 | 示例 |
| --- | --- | --- |
| Domain 单元测试 | 状态机、值对象、业务规则 | Agent 发布状态流转 |
| UseCase 单元测试 | 用例编排、异常分支 | RuntimeDataView 聚合 |
| Repository 集成测试 | SQL 映射、索引查询 | Span 按 run 查询 |
| Controller 集成测试 | HTTP 路径、租户上下文、DTO | RuntimeDataViewController |
| ArchUnit 测试 | 分层、命名、依赖 | AgentHubCleanArchitectureTest |
| 前端构建测试 | TypeScript 和 Vue 模板 | npm run build |
| 前端逻辑测试 | Trace 树排序、过滤、前缀 | useTraceTree |

### 5.3 回归测试清单

每次涉及运行视图、会话、Agent 运行时的改动，至少执行：

```bash
gradle --no-daemon test --tests "com.agenthub.test.integration.RuntimeDataViewControllerIntegrationTest" --tests "com.agenthub.test.architecture.AgentHubCleanArchitectureTest"
cd src/main/web
npm run build -- --outDir dist-check
```

每次涉及全局架构、仓储、租户上下文的改动，执行：

```bash
gradle --no-daemon test
```

---

## 6. 迭代节奏

### 6.1 版本节奏建议

| 周期 | 目标 | 输出 |
| --- | --- | --- |
| 每周 | 小迭代 | 1 到 3 个可验证功能点 |
| 每两周 | 集成回归 | 运行视图、会话、Agent、工作流主链路回归 |
| 每月 | 版本发布 | 发布说明、迁移说明、已知问题 |
| 每季度 | 架构复盘 | 模块复杂度、测试覆盖、性能瓶颈、技术债 |

### 6.2 近期三阶段计划

#### 阶段一：1 到 2 周

目标：稳定运行视图。

任务：

- 拆分 RuntimeChatView 中运行视图组件。
- 为 Trace 树构建逻辑补前端单元测试。
- 后端 RuntimeDataViewController 集成测试补齐空数据、有数据、跨租户隔离场景。
- 优化运行数据查询索引。
- 清理已废弃的重复观测入口和文档。

#### 阶段二：3 到 6 周

目标：Agent 调试闭环，版本管理暂缓。

任务：

- 增加发布前依赖校验。
- 调试运行自动生成 Run。
- 运行视图支持 Run 对比和慢调用标记。
- Agent 配置差异展示。
 - 保留 Agent 版本管理设计，不在当前阶段落地。

#### 阶段三：7 到 12 周

目标：RAG 质量中心和工作流观测。

任务：

- 增加检索评测数据集。
- 增加检索调试台。
- 工作流节点执行接入 Runtime DataView。
- 节点失败重试。
- 成本统计报表初版。

---

## 7. 风险与应对

| 风险 | 表现 | 应对 |
| --- | --- | --- |
| 前端继续膨胀 | 单文件超过可维护范围 | 强制组件拆分和 composable 抽取 |
| 运行数据重复设计 | Trace、Metric、Alert 多套入口混乱 | 统一 DataView 聚合入口，其他作为底层数据源 |
| 架构规则被绕过 | application 直接调用 infrastructure | ArchUnit 增加细粒度规则 |
| 集成测试成本高 | 改动后测试慢或不稳定 | 分层测试，Controller 集成测试聚焦主链路 |
| AgentScope/Spring AI 双运行时差异 | 行为和观测字段不一致 | 定义统一 AgentRuntimePort 和 Telemetry 事件模型 |
| 数据量增长 | Span、Token、消息表膨胀 | 索引、归档、保留策略、分页查询 |
| 企业权限复杂 | 跨租户访问风险 | 所有 Controller 集成测试覆盖租户隔离 |

---

## 8. 技术债清单

| 编号 | 技术债 | 建议处理 |
| --- | --- | --- |
| TD-001 | 运行视图前端仍集中在聊天页 | 拆成 runtime 组件和 composable |
| TD-002 | 部分监控文档仍保留 OTLP 旧方案描述 | 更新或归档旧文档 |
| TD-003 | 部分 Controller 方法和 UseCase 方法可能超过 10 行 | 按领域动作拆私有方法 |
| TD-004 | 前端缺少单元测试 | 先覆盖 Trace 树和运行视图数据转换 |
| TD-005 | 运行数据查询性能未形成指标 | 增加 Explain 记录和索引基线 |
| TD-006 | 策略缺少版本和生效范围 | 引入 StrategyVersion 和 scope |
| TD-007 | 工作流执行和 Agent Run 关系需统一 | WorkflowRun 适配 Runtime DataView |

---

## 9. 推荐验收模板

每个后续需求建议按以下模板验收：

```text
需求名称：
业务目标：
影响模块：
接口变更：
数据模型变更：
前端页面变更：
测试用例：
ArchUnit 影响：
迁移脚本：
回归范围：
验收标准：
```

每个 PR 或变更提交前至少回答：

- 是否破坏 Clean Architecture 依赖方向？
- Controller 是否有集成测试？
- 是否有跨租户隔离风险？
- 是否影响运行视图主链路？
- 是否需要数据库迁移？
- 是否需要更新文档？

---

## 10. 建议优先级总表

| 优先级 | 工作项 | 原因 |
| --- | --- | --- |
| P0 | 稳定运行视图 | AgentOps 核心调试入口 |
| P0 | 拆分 RuntimeChatView | 降低后续迭代风险 |
| P0 | 运行数据模型统一 | 避免 OTLP/Metric/Alert/Span 重复设计 |
| P0 | Controller 集成测试常态化 | 保证 API 行为稳定 |
| P1 | Agent 版本和发布 | 企业使用必需 |
| P1 | RAG 质量中心 | 知识型 Agent 的核心竞争力 |
| P1 | 工作流观测 | DAG 执行可运营 |
| P2 | 成本中心 | 企业预算治理 |
| P2 | 审计中心增强 | 企业合规 |
| P2 | 部署运维标准化 | 降低交付成本 |

---

## 11. 下一步建议

建议从以下三个任务开始：

1. 将 `RuntimeChatView.vue` 中运行视图拆分为独立组件。
2. 为 Trace 树构建逻辑增加前端单元测试。
3. 为 Runtime DataView 后端补齐跨租户、空数据、有数据、异常路径集成测试。

这三个任务能同时降低当前复杂度、稳住核心功能，并为后续 AgentOps 能力扩展打基础。
