# agentscope-studio 客户端数据处理机制深度分析报告

## 一、概述

本报告深入分析 agentscope-studio 项目如何处理客户端提交的数据，重点关注以下TRPC接口：
- `/trpc/registerRun` - 注册运行
- `/trpc/requestUserInput` - 请求用户输入
- 数据关联机制
- 数据分析流程

---

## 二、TRPC接口实现

### 2.1 核心TRPC路由文件

**文件路径**: `E:/Code/vibe/agentscope-studio/packages/server/src/trpc/router.ts`

### 2.2 `/trpc/registerRun` 接口实现

**功能**: 注册一个新的运行实例

**输入参数**:
```typescript
{
    id: string,           // 运行ID
    project: string,      // 项目名称
    name: string,         // 运行名称
    timestamp: string,    // 时间戳
    pid: number,          // 进程ID
    status: Status,       // 运行状态
    run_dir?: string      // 运行目录（可选）
}
```

**实现代码** (行号: 105-131):
```typescript
registerRun: t.procedure
    .input(
        z.object({
            id: z.string(),
            project: z.string(),
            name: z.string(),
            timestamp: z.string(),
            pid: z.number(),
            status: z.enum(Object.values(Status) as [string, ...string[]]),
            run_dir: z.string().optional().nullable(),
        }),
    )
    .mutation(async ({ input }) => {
        const runData = {
            id: input.id,
            project: input.project,
            name: input.name,
            timestamp: input.timestamp,
            run_dir: input.run_dir || '',
            pid: input.pid,
            status: input.status,
        } as RunData;

        // 保存到数据库
        await RunDao.addRun(runData);

        // 实时通知订阅者
        SocketManager.broadcastRunToProjectRoom(input.project);
        SocketManager.broadcastRunToProjectListRoom();
        SocketManager.broadcastOverviewDataToDashboardRoom();
    }),
```

**数据流向**:
```
客户端调用 registerRun
    ↓
验证输入参数（Zod schema）
    ↓
构造 RunData 对象
    ↓
RunDao.addRun() 保存到数据库
    ↓
SocketManager 广播更新到三个房间：
    1. 项目房间 (project)
    2. 项目列表房间 (ProjectListRoom)
    3. 仪表板概览房间 (OverviewRoom)
    ↓
前端实时更新
```

### 2.3 `/trpc/requestUserInput` 接口实现

**功能**: 请求用户输入，实现人机交互

**输入参数**:
```typescript
{
    requestId: string,              // 请求ID
    runId: string,                  // 运行ID
    agentId: string,                // Agent ID
    agentName: string,              // Agent名称
    structuredInput: Record<string, unknown> | null  // 结构化输入
}
```

**实现代码** (行号: 132-180):
```typescript
requestUserInput: t.procedure
    .input(
        z.object({
            requestId: z.string(),
            runId: z.string(),
            agentId: z.string(),
            agentName: z.string(),
            structuredInput: z.record(z.unknown()).nullable(),
        }),
    )
    .mutation(async ({ input }) => {
        // 验证Run是否存在
        const runExist = await RunDao.doesRunExist(input.runId);

        if (!runExist) {
            throw new TRPCError({
                code: 'BAD_REQUEST',
                message: `Run with id ${input.runId} does not exist`,
            });
        }

        try {
            // 保存输入请求到数据库
            await InputRequestDao.saveInputRequest({
                requestId: input.requestId,
                runId: input.runId,
                agentId: input.agentId,
                agentName: input.agentName,
                structuredInput: input.structuredInput,
            });

            // 广播输入请求到运行房间
            SocketManager.broadcastInputRequestToRunRoom(input.runId, {
                requestId: input.requestId,
                agentId: input.agentId,
                agentName: input.agentName,
                structuredInput: input.structuredInput,
            } as InputRequestData);
        } catch (error) {
            console.error(error);
            throw new TRPCError({
                code: 'BAD_REQUEST',
                message: 'Failed to save input request',
            });
        }
    }),
```

**数据流向**:
```
客户端调用 requestUserInput
    ↓
验证 Run 是否存在
    ↓
InputRequestDao.saveInputRequest() 保存到数据库
    ↓
SocketManager.broadcastInputRequestToRunRoom() 广播到运行房间
    ↓
前端显示输入请求表单
    ↓
用户填写并提交
    ↓
Socket sendUserInputToServer 事件
    ↓
Python SDK 接收用户输入
    ↓
继续执行
```

### 2.4 其他关键TRPC接口

| 接口名称 | 功能 | 类型 |
|---------|------|------|
| `registerReply` | 注册回复消息 | mutation |
| `getProjects` | 获取项目列表（支持分页、排序、过滤） | query |
| `getRuns` | 获取运行列表 | query |
| `getTrace` | 获取追踪数据 | query |
| `getTraceStatistic` | 获取追踪统计信息 | query |
| `getModelInvocationData` | 获取模型调用数据 | query |

---

## 三、数据存储机制

### 3.1 数据库技术栈

- **ORM**: TypeORM
- **数据库**: SQLite (better-sqlite3)
- **位置**: 用户应用数据目录下的 `agentscope-studio.db`

### 3.2 数据库初始化

**文件路径**: `E:/Code/vibe/agentscope-studio/packages/server/src/database.ts`

```typescript
export const initializeDatabase = async (databaseConfig: DataSourceOptions): Promise<void> => {
    const options = {
        ...databaseConfig,
        entities: [
            RunTable,
            RunView,
            MessageTable,
            ReplyTable,
            InputRequestTable,
            SpanTable,
            ModelInvocationView,
            FridayAppMessageTable,
            FridayAppReplyTable,
            FridayAppReplyView,
        ],
        synchronize: true,
        migrations: migrations,
        migrationsRun: true,
        logging: false,
    };

    const dataSource = new DataSource(options);
    await dataSource.initialize();

    // 启动时更新状态
    await RunDao.updateRunStatusAtBeginning();
    await InputRequestDao.updateInputRequests();
};
```

### 3.3 核心数据模型

#### 3.3.1 RunTable (运行表)

**文件路径**: `E:/Code/vibe/agentscope-studio/packages/server/src/models/Run.ts`

**表结构**:
```typescript
@Entity()
export class RunTable extends BaseEntity {
    @PrimaryColumn()
    id: string;                    // 主键：运行ID

    @Column()
    project: string;               // 项目名称

    @Column()
    name: string;                  // 运行名称

    @Column()
    timestamp: string;             // 时间戳

    @Column()
    run_dir: string;               // 运行目录

    @Column()
    pid: number;                   // 进程ID

    @Column({ type: 'varchar', enum: Status, default: Status.DONE })
    status: Status;                // 运行状态

    // 关联关系
    @OneToMany(() => ReplyTable, (reply) => reply.runId)
    replies: ReplyTable[];         // 一对多：回复

    @OneToMany(() => SpanTable, (span) => span.conversationId)
    spans: SpanTable[];            // 一对多：追踪数据

    @OneToMany(() => InputRequestTable, (inputRequest) => inputRequest.runId)
    inputRequests: InputRequestTable[];  // 一对多：输入请求
}
```

**字段说明**:
- `id`: 运行唯一标识符，由客户端生成
- `project`: 项目名称，用于分组管理
- `name`: 运行名称，描述性信息
- `timestamp`: 运行开始时间
- `pid`: 进程ID，用于进程管理
- `status`: 运行状态（RUNNING、DONE、ERROR等）

#### 3.3.2 SpanTable (追踪数据表)

**文件路径**: `E:/Code/vibe/agentscope-studio/packages/server/src/models/Trace.ts`

**表结构**:
```typescript
@Entity()
@Index(['traceId'])
@Index(['spanId'])
@Index(['parentSpanId'])
@Index(['startTimeUnixNano'])
@Index(['statusCode'])
@Index(['latencyNs'])
@Index(['serviceName'])
@Index(['operationName'])
@Index(['model'])
@Index(['conversationId'])
export class SpanTable extends BaseEntity {
    @PrimaryColumn()
    id: string;                    // 主键

    @Column()
    traceId: string;               // 追踪ID

    @Column()
    spanId: string;                // Span ID

    @Column({ nullable: true })
    parentSpanId?: string;         // 父Span ID

    @Column()
    name: string;                  // Span名称

    @Column()
    kind: number;                  // Span类型

    @Column()
    startTimeUnixNano: string;     // 开始时间（纳秒）

    @Column()
    endTimeUnixNano: string;       // 结束时间（纳秒）

    @Column('json')
    attributes: Record<string, unknown>;  // 属性（JSON）

    @Column('json')
    status: Record<string, unknown>;      // 状态（JSON）

    @Column('json')
    resource: Record<string, unknown>;    // 资源（JSON）

    @Column('json')
    scope: Record<string, unknown>;       // 作用域（JSON）

    @Column({ nullable: true })
    statusCode?: number;           // 状态码

    @Column({ nullable: true })
    serviceName?: string;          // 服务名称

    @Column({ nullable: true })
    operationName?: string;        // 操作名称

    @Column({ nullable: true })
    model?: string;                // 模型名称

    @Column({ nullable: true })
    inputTokens?: number;          // 输入Token数

    @Column({ nullable: true })
    outputTokens?: number;         // 输出Token数

    @Column({ nullable: true })
    totalTokens?: number;          // 总Token数

    @Column({ nullable: true })
    conversationId?: string;       // 会话ID（关联Run）

    @Column('float')
    latencyNs: number;             // 延迟（纳秒）
}
```

**索引说明**:
- `traceId`: 快速查询同一追踪的所有Span
- `spanId`: 快速查询单个Span
- `parentSpanId`: 快速查询父子关系
- `startTimeUnixNano`: 时间范围查询
- `statusCode`: 状态过滤
- `latencyNs`: 性能分析
- `serviceName`: 服务分组
- `operationName`: 操作分组
- `model`: 模型分组
- `conversationId`: 关联Run查询

#### 3.3.3 InputRequestTable (输入请求表)

**文件路径**: `E:/Code/vibe/agentscope-studio/packages/server/src/models/InputRequest.ts`

**表结构**:
```typescript
@Entity()
export class InputRequestTable extends BaseEntity {
    @PrimaryColumn({ nullable: false })
    requestId: string;             // 请求ID

    @Column()
    agentId: string;               // Agent ID

    @Column()
    agentName: string;             // Agent名称

    @Column('json', { nullable: true })
    structuredInput: Record<string, unknown> | null;  // 结构化输入

    @ManyToOne(() => RunTable, { nullable: false, onDelete: 'CASCADE' })
    @JoinColumn({ name: 'run_id' })
    runId: string;                 // 关联Run（外键，级联删除）
}
```

#### 3.3.4 ReplyTable (回复表)

**文件路径**: `E:/Code/vibe/agentscope-studio/packages/server/src/models/Reply.ts`

**表结构**:
```typescript
@Entity()
export class ReplyTable extends BaseEntity {
    @PrimaryColumn()
    replyId: string;               // 回复ID

    @Column()
    replyRole: string;             // 回复角色

    @Column()
    replyName: string;             // 回复名称

    @ManyToOne(() => RunTable, { nullable: false, onDelete: 'CASCADE' })
    @JoinColumn({ name: 'run_id' })
    runId: string;                 // 关联Run（外键，级联删除）

    @Column()
    createdAt: string;             // 创建时间

    @Column({ type: 'varchar', nullable: true })
    finishedAt: string | null;     // 完成时间

    @OneToMany(() => MessageTable, (message) => message.replyId)
    messages: MessageTable[];      // 一对多：消息
}
```

#### 3.3.5 MessageTable (消息表)

**文件路径**: `E:/Code/vibe/agentscope-studio/packages/server/src/models/Message.ts`

**表结构**:
```typescript
@Entity()
export class MessageTable extends BaseEntity {
    @PrimaryColumn({ nullable: false })
    id: string;                    // 消息ID

    @ManyToOne(() => RunTable, { nullable: false, onDelete: 'CASCADE' })
    @JoinColumn({ name: 'run_id' })
    runId: string;                 // 关联Run（外键，级联删除）

    @ManyToOne(() => ReplyTable, { nullable: false, onDelete: 'CASCADE' })
    @JoinColumn({ name: 'replyId' })
    replyId: string;               // 关联Reply（外键，级联删除）

    @Column('json')
    msg: {                         // 消息内容
        name: string;
        role: string;
        content: ContentType;
        metadata: object;
        timestamp: string;
    };

    @Column('json', { nullable: true })
    speech: AudioBlock[] | null;   // 语音数据
}
```

### 3.4 视图模型

#### 3.4.1 RunView (运行统计视图)

**文件路径**: `E:/Code/vibe/agentscope-studio/packages/server/src/models/RunView.ts`

**功能**: 提供运行聚合统计

**字段**:
- `totalProjects`: 总项目数
- `totalRuns`: 总运行数
- `projectsWeekAgo/projectsMonthAgo/projectsYearAgo`: 时间段项目数
- `runsWeekAgo/runsMonthAgo/runsYearAgo`: 时间段运行数
- `monthlyRuns`: 月度运行统计（JSON）

#### 3.4.2 ModelInvocationView (模型调用视图)

**文件路径**: `E:/Code/vibe/agentscope-studio/packages/server/src/models/ModelInvocationView.ts`

**功能**: 提供模型调用统计

**字段**:
- `totalModelInvocations`: 总模型调用数
- `totalTokens`: 总Token数
- `chatModelInvocations`: 聊天模型调用数
- `tokensWeekAgo/tokensMonthAgo/tokensYearAgo`: 时间段Token统计

---

## 四、数据关联机制

### 4.1 Run ID 生成和使用

**生成方式**:
- 由客户端生成
- 通常使用 UUID 或时间戳+随机数

**使用场景**:
1. **注册运行**: 客户端调用 `registerRun` 时提供
2. **关联数据**: 所有相关数据都通过 `conversationId` 或 `runId` 关联
3. **数据查询**: 通过 Run ID 查询相关的 Spans、Replies、InputRequests

### 4.2 数据关联关系图

```
RunTable (id)
    ├── ReplyTable (runId) [OneToMany]
    │   └── MessageTable (replyId) [OneToMany]
    ├── SpanTable (conversationId) [OneToMany]
    └── InputRequestTable (runId) [OneToMany]

SpanTable
    ├── traceId: 同一追踪的所有Span
    ├── parentSpanId: 父子Span关系
    └── conversationId: 关联到Run
```

### 4.3 父子关系建立

#### 4.3.1 Span父子关系

**建立方式**:
- 通过 `parentSpanId` 字段建立
- `parentSpanId` 为空表示根Span
- 形成调用链树结构

**示例**:
```
Root Span (parentSpanId = null)
    ├── Child Span 1 (parentSpanId = rootSpanId)
    │   ├── Grandchild Span 1.1
    │   └── Grandchild Span 1.2
    └── Child Span 2 (parentSpanId = rootSpanId)
        └── Grandchild Span 2.1
```

#### 4.3.2 Run与Span关系

**建立方式**:
- Span的 `conversationId` 字段关联到Run的 `id`
- 一个Run可以有多个Span（追踪数据）

**查询示例**:
```typescript
// 查询Run的所有Span
const spans = await SpanTable.find({
    where: { conversationId: runId }
});
```

#### 4.3.3 Run与Reply关系

**建立方式**:
- Reply的 `runId` 字段关联到Run的 `id`
- 一个Run可以有多个Reply（对话回复）

#### 4.3.4 Reply与Message关系

**建立方式**:
- Message的 `replyId` 字段关联到Reply的 `replyId`
- 一个Reply可以有多个Message（流式消息）

### 4.4 级联删除

**配置**:
```typescript
@ManyToOne(() => RunTable, { nullable: false, onDelete: 'CASCADE' })
```

**效果**:
- 删除Run时，自动删除关联的Reply、Span、InputRequest
- 删除Reply时，自动删除关联的Message

---

## 五、数据分析流程

### 5.1 统计分析服务

#### 5.1.1 Trace统计

**文件路径**: `E:/Code/vibe/agentscope-studio/packages/server/src/dao/Trace.ts`

**方法**: `getTraceStatistic()`

**签名**:
```typescript
static async getTraceStatistic(filters?: {
    startTime?: string;
    endTime?: string;
    traceId?: string;
}): Promise<{
    totalTraces: number;
    totalSpans: number;
    errorTraces: number;
    avgDuration: number;
    totalTokens: number;
    tracesByStatus: Array<{ status: number; count: number }>;
}>
```

**计算逻辑**:
1. 根据时间范围过滤Span
2. 计算唯一Trace数量（DISTINCT traceId）
3. 计算错误Trace数量（statusCode = 2）
4. 计算平均持续时间（endTime - startTime）
5. 统计总Token数
6. 按状态分组统计

**SQL示例**:
```sql
-- 计算总Trace数
SELECT COUNT(DISTINCT traceId) as totalTraces
FROM span_table
WHERE startTimeUnixNano >= :startTime
  AND startTimeUnixNano <= :endTime;

-- 计算错误Trace数
SELECT COUNT(DISTINCT traceId) as errorTraces
FROM span_table
WHERE statusCode = 2
  AND startTimeUnixNano >= :startTime
  AND startTimeUnixNano <= :endTime;

-- 计算平均持续时间
SELECT AVG(
    CAST(endTimeUnixNano AS REAL) - CAST(startTimeUnixNano AS REAL)
) as avgDuration
FROM span_table
WHERE parentSpanId IS NULL;

-- 按状态分组
SELECT statusCode as status, COUNT(*) as count
FROM span_table
GROUP BY statusCode;
```

#### 5.1.2 模型调用统计

**方法**: `getModelInvocationData(conversationId: string)`

**计算内容**:
- 总调用次数
- 聊天模型调用次数
- Token统计（总Token、平均Token）
- 按模型分组的统计

**SQL示例**:
```sql
-- 总调用次数
SELECT COUNT(*) as totalInvocations
FROM span_table
WHERE conversationId = :conversationId
  AND model IS NOT NULL;

-- Token统计
SELECT
    SUM(totalTokens) as totalTokens,
    AVG(totalTokens) as avgTokens
FROM span_table
WHERE conversationId = :conversationId
  AND model IS NOT NULL;

-- 按模型分组
SELECT
    model,
    COUNT(*) as count,
    SUM(totalTokens) as tokens
FROM span_table
WHERE conversationId = :conversationId
  AND model IS NOT NULL
GROUP BY model;
```

### 5.2 性能指标计算

#### 5.2.1 延迟计算

**公式**:
```typescript
latencyNs = endTimeUnixNano - startTimeUnixNano
```

**单位转换**:
```typescript
latencyMs = latencyNs / 1_000_000  // 纳秒转毫秒
latencyS = latencyNs / 1_000_000_000  // 纳秒转秒
```

#### 5.2.2 Token统计

**公式**:
```typescript
totalTokens = inputTokens + outputTokens
```

#### 5.2.3 平均持续时间

**公式**:
```typescript
avgDuration = sum(durations) / count
```

### 5.3 数据聚合视图

**使用SQL视图预计算统计数据**:

#### 5.3.1 RunView

**SQL定义**:
```sql
CREATE VIEW run_view AS
SELECT
    COUNT(DISTINCT project) as totalProjects,
    COUNT(*) as totalRuns,
    COUNT(DISTINCT CASE
        WHEN timestamp >= datetime('now', '-7 days')
        THEN project
    END) as projectsWeekAgo,
    COUNT(CASE
        WHEN timestamp >= datetime('now', '-7 days')
        THEN 1
    END) as runsWeekAgo,
    -- ... 其他统计字段
FROM run_table;
```

#### 5.3.2 ModelInvocationView

**SQL定义**:
```sql
CREATE VIEW model_invocation_view AS
SELECT
    COUNT(*) as totalModelInvocations,
    SUM(totalTokens) as totalTokens,
    COUNT(CASE WHEN kind = 1 THEN 1 END) as chatModelInvocations,
    SUM(CASE
        WHEN startTimeUnixNano >= :weekAgo
        THEN totalTokens
    END) as tokensWeekAgo,
    -- ... 其他统计字段
FROM span_table
WHERE model IS NOT NULL;
```

---

## 六、关键文件清单

### 6.1 TRPC路由文件

| 文件路径 | 功能 |
|---------|------|
| `packages/server/src/trpc/router.ts` | 主路由定义 |
| `packages/server/src/trpc/socket.ts` | Socket.IO管理 |

### 6.2 数据模型文件

| 文件路径 | 功能 |
|---------|------|
| `packages/server/src/models/Run.ts` | Run模型 |
| `packages/server/src/models/Trace.ts` | Span模型 |
| `packages/server/src/models/InputRequest.ts` | InputRequest模型 |
| `packages/server/src/models/Reply.ts` | Reply模型 |
| `packages/server/src/models/Message.ts` | Message模型 |
| `packages/server/src/models/RunView.ts` | Run视图 |
| `packages/server/src/models/ModelInvocationView.ts` | 模型调用视图 |
| `packages/server/src/models/FridayApp.ts` | Friday应用模型 |
| `packages/server/src/models/FridayAppView.ts` | Friday应用视图 |

### 6.3 数据访问层文件

| 文件路径 | 功能 |
|---------|------|
| `packages/server/src/dao/Run.ts` | Run DAO |
| `packages/server/src/dao/Trace.ts` | Span DAO |
| `packages/server/src/dao/InputRequest.ts` | InputRequest DAO |
| `packages/server/src/dao/Reply.ts` | Reply DAO |
| `packages/server/src/dao/Message.ts` | Message DAO |
| `packages/server/src/dao/FridayAppMessage.ts` | Friday应用消息DAO |

### 6.4 数据分析相关文件

| 文件路径 | 功能 |
|---------|------|
| `packages/server/src/dao/Trace.ts` | 统计分析方法 |
| `packages/server/src/models/RunView.ts` | 运行统计视图 |
| `packages/server/src/models/ModelInvocationView.ts` | 模型调用统计视图 |

### 6.5 OpenTelemetry处理文件

| 文件路径 | 功能 |
|---------|------|
| `packages/server/src/otel/router.ts` | OTEL HTTP路由 |
| `packages/server/src/otel/grpc-server.ts` | OTEL gRPC服务器 |
| `packages/server/src/otel/processor.ts` | Span处理器 |

### 6.6 配置和初始化文件

| 文件路径 | 功能 |
|---------|------|
| `packages/server/src/index.ts` | 服务器入口 |
| `packages/server/src/database.ts` | 数据库初始化 |
| `packages/shared/src/config/server.ts` | 服务器配置 |
| `packages/shared/src/config/common.ts` | 通用配置 |

### 6.7 类型定义文件

| 文件路径 | 功能 |
|---------|------|
| `packages/shared/src/types/trpc.ts` | TRPC类型定义 |
| `packages/shared/src/types/trace.ts` | Trace类型定义 |
| `packages/shared/src/types/messageForm.ts` | 消息类型定义 |
| `packages/shared/src/types/usage.ts` | 使用量类型定义 |

### 6.8 数据库迁移文件

| 文件路径 | 功能 |
|---------|------|
| `packages/server/src/migrations/index.ts` | 迁移索引 |
| `packages/server/src/migrations/1730000000000-AddMessageReplyForeignKey.ts` | 消息外键迁移 |
| `packages/server/src/migrations/1740000000000-MigrateSpanTable.ts` | Span表迁移 |

---

## 七、数据流向总结

### 7.1 客户端数据上报流程

```
Python SDK
    ↓
OpenTelemetry Exporter
    ↓
OTEL gRPC/HTTP Server (端口 4317)
    ↓
SpanProcessor.batchProcessOTLPTraces()
    ↓
SpanDao.saveSpans()
    ↓
SQLite Database
    ↓
SocketManager.broadcastSpanDataToRunRoom()
    ↓
前端实时更新
```

### 7.2 运行注册流程

```
Python SDK
    ↓
TRPC Client
    ↓
registerRun mutation
    ↓
RunDao.addRun()
    ↓
SQLite Database
    ↓
SocketManager 广播更新
    ↓
前端项目列表/仪表板更新
```

### 7.3 用户输入请求流程

```
Python SDK
    ↓
TRPC Client
    ↓
requestUserInput mutation
    ↓
验证Run存在
    ↓
InputRequestDao.saveInputRequest()
    ↓
SQLite Database
    ↓
SocketManager.broadcastInputRequestToRunRoom()
    ↓
前端显示输入请求
    ↓
用户填写输入
    ↓
Socket forwardUserInput
    ↓
Python SDK接收输入
```

### 7.4 数据查询流程

```
前端
    ↓
TRPC Client
    ↓
getProjects/getRuns/getTrace
    ↓
RunDao/SpanDao 查询方法
    ↓
SQLite Database (带分页、排序、过滤)
    ↓
返回 TableData<T>
    ↓
前端渲染
```

---

## 八、Socket.IO 实时通信机制

### 8.1 房间管理

**文件路径**: `E:/Code/vibe/agentscope-studio/packages/server/src/trpc/socket.ts`

**房间类型**:
- `ProjectListRoom`: 项目列表房间
- `OverviewRoom`: 仪表板概览房间
- `FridayAppRoom`: Friday应用房间
- `run-{runId}`: 运行房间（动态）

### 8.2 事件类型

**服务端推送事件**:
| 事件名称 | 功能 |
|---------|------|
| `pushOverviewData` | 推送概览数据 |
| `pushProjects` | 推送项目列表 |
| `pushRunsData` | 推送运行数据 |
| `pushRunData` | 推送单个运行数据 |
| `pushInputRequests` | 推送输入请求 |
| `pushMessages` | 推送消息 |
| `pushSpans` | 推送追踪数据 |
| `pushModelInvocationData` | 推送模型调用数据 |

**客户端事件**:
| 事件名称 | 功能 |
|---------|------|
| `joinOverviewRoom` | 加入概览房间 |
| `joinProjectListRoom` | 加入项目列表房间 |
| `joinProjectRoom` | 加入项目房间 |
| `joinRunRoom` | 加入运行房间 |
| `sendUserInputToServer` | 发送用户输入 |

### 8.3 广播机制

**广播到项目房间**:
```typescript
static broadcastRunToProjectRoom(project: string) {
    RunDao.getAllProjectRuns(project).then((runs) => {
        this.io
            .of('/client')
            .to(project)
            .emit(SocketEvents.server.pushRunsData, runs);
    });
}
```

**广播Span数据到运行房间**:
```typescript
static broadcastSpanDataToRunRoom(spanDataArray: SpanData[]) {
    // 按runId分组
    const groupedSpans: Record<string, SpanData[]> = {};
    spanDataArray.forEach((spanData) => {
        if (!groupedSpans[spanData.conversationId]) {
            groupedSpans[spanData.conversationId] = [];
        }
        groupedSpans[spanData.conversationId].push(spanData);
    });

    // 发送到各个运行房间
    for (const runId in groupedSpans) {
        this.io
            .of('/client')
            .to(`run-${runId}`)
            .emit(SocketEvents.server.pushSpans, groupedSpans[runId]);
    }
}
```

---

## 九、OpenTelemetry 数据处理

### 9.1 数据接收

**HTTP方式**: `POST /v1/traces`
**gRPC方式**: OpenTelemetry TraceService (端口 4317)

### 9.2 数据处理流程

```typescript
// 1. 接收数据
otelRouter.post('/traces', async (req, res) => {
    // 2. 解压缩
    if (contentEncoding === 'gzip') {
        body = await gunzipAsync(body);
    }

    // 3. 解析数据
    if (contentType.includes('application/x-protobuf')) {
        traceData = opentelemetry.proto.trace.v1.TracesData.deserializeBinary(body);
    } else if (contentType.includes('application/json')) {
        traceData = JSON.parse(body.toString());
    }

    // 4. 处理Span
    const spans = SpanProcessor.batchProcessOTLPTraces(resourceSpans);

    // 5. 保存到数据库
    await SpanDao.saveSpans(spans);

    // 6. 广播更新
    SocketManager.broadcastSpanDataToRunRoom(spans);
});
```

### 9.3 Span处理

**文件路径**: `E:/Code/vibe/agentscope-studio/packages/server/src/otel/processor.ts`

**处理步骤**:
1. 解码Resource信息
2. 解码Scope信息
3. 解码Span数据
4. 提取关键字段（serviceName、operationName、model、tokens等）
5. 计算延迟
6. 关联conversationId

**关键代码**:
```typescript
static batchProcessOTLPTraces(resourceSpans: any[]): SpanData[] {
    const spans: SpanData[] = [];

    resourceSpans.forEach((resourceSpan) => {
        // 解码Resource
        const resource = this.decodeResource(resourceSpan.resource);
        const serviceName = resource.attributes['service.name'];

        resourceSpan.scopeSpans.forEach((scopeSpan: any) => {
            // 解码Scope
            const scope = this.decodeScope(scopeSpan.scope);

            scopeSpan.spans.forEach((span: any) => {
                // 解码Span
                const spanData = this.decodeSpan(span);

                // 提取关键字段
                spanData.serviceName = serviceName;
                spanData.operationName = spanData.name;
                spanData.model = spanData.attributes.get('gen_ai.request.model');
                spanData.inputTokens = spanData.attributes.get('gen_ai.usage.input_tokens');
                spanData.outputTokens = spanData.attributes.get('gen_ai.usage.output_tokens');
                spanData.totalTokens = (spanData.inputTokens || 0) + (spanData.outputTokens || 0);

                // 计算延迟
                spanData.latencyNs = parseInt(spanData.endTimeUnixNano) - parseInt(spanData.startTimeUnixNano);

                // 关联conversationId
                spanData.conversationId = spanData.attributes.get('conversation.id');

                spans.push(spanData);
            });
        });
    });

    return spans;
}
```

---

## 十、总结

### 10.1 架构特点

agentscope-studio项目采用了现代化的全栈架构来处理客户端数据：

1. **接口层**: 使用TRPC提供类型安全的API，支持mutation和query
2. **存储层**: 使用TypeORM + SQLite实现数据持久化，支持关系映射和视图
3. **关联层**: 通过外键和索引建立数据关联，支持级联删除
4. **分析层**: 使用SQL视图和聚合查询实现统计分析
5. **实时层**: 使用Socket.IO实现数据实时推送
6. **追踪层**: 支持OpenTelemetry标准，通过gRPC/HTTP接收追踪数据

### 10.2 数据关联核心

**关键关联字段**:
- `Run.id` ↔ `Span.conversationId` - Run与Span关联
- `Run.id` ↔ `Reply.runId` - Run与Reply关联
- `Run.id` ↔ `InputRequest.runId` - Run与InputRequest关联
- `Reply.replyId` ↔ `Message.replyId` - Reply与Message关联
- `Span.traceId` - 同一追踪的所有Span
- `Span.parentSpanId` - Span父子关系

### 10.3 数据分析核心

**统计维度**:
- 时间维度：按时间段统计（周、月、年）
- 状态维度：按状态分组统计
- 模型维度：按模型分组统计
- 性能维度：延迟、Token统计

**计算方式**:
- SQL视图预计算
- 聚合查询实时计算
- 索引优化查询性能

### 10.4 实时通信核心

**Socket.IO机制**:
- 房间管理：按项目、运行分组
- 事件驱动：数据变更立即推送
- 双向通信：支持用户输入交互

### 10.5 OpenTelemetry集成

**数据流**:
- Python SDK → OTEL Exporter → Server → Database → Frontend
- 支持标准OTLP协议
- 自动提取关键字段
- 实时处理和推送

---

## 十一、技术亮点

1. **类型安全**: TRPC提供端到端类型安全
2. **实时更新**: Socket.IO实现毫秒级数据推送
3. **标准协议**: 支持OpenTelemetry标准
4. **关系映射**: TypeORM提供强大的ORM功能
5. **性能优化**: 索引、视图、预计算
6. **级联删除**: 保证数据一致性
7. **分页排序**: 支持大数据量查询
8. **灵活过滤**: 支持多维度数据过滤

---

**报告生成时间**: 2026-05-24
**分析项目**: agentscope-studio
**报告版本**: v1.0

🎯
