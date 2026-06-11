# 架构设计文档

> 本文档详细描述 AgentHub 的系统架构、分层设计、组件交互和关键设计决策。

---

## 1. 架构总览

AgentHub 采用**整洁架构（Clean Architecture）**，将系统分为四层，遵循依赖反转原则，确保核心业务逻辑不依赖于外部框架和基础设施。

```
┌─────────────────────────────────────────────────────────────────────┐
│                        api (接口适配层)                               │
│   Controller · Request/Response DTO · Mapper · Exception Handler     │
│                           ↑ 依赖                                     │
├─────────────────────────────────────────────────────────────────────┤
│                    application (应用层)                               │
│   UseCase · Command · ApplicationDTO · Service · Port(Interface)     │
│                           ↑ 依赖                                     │
├─────────────────────────────────────────────────────────────────────┤
│                      domain (领域层)                                  │
│   Model · Event · Exception · 核心业务规则 · ValueObject              │
│                           ↑ 依赖                                     │
├─────────────────────────────────────────────────────────────────────┤
│                  infrastructure (基础设施层)                           │
│   agents · auth · etl · rag · store · tools · vector · web · wiki    │
│   (实现 application/port 定义的接口)                                   │
└─────────────────────────────────────────────────────────────────────┘
```

### 核心设计原则

1. **依赖反转**：高层模块不依赖于底层模块，两者都依赖于抽象（接口）
2. **接口隔离**：`application/port/` 定义清晰的输入/输出端口
3. **领域独立**：领域层是系统的核心，不依赖任何外部框架
4. **可测试性**：通过端口接口可以轻松进行单元测试和集成测试

---

## 2. 分层详解

### 2.1 API 层（Interface Adapters）

**职责**：处理 HTTP 请求/响应，协议适配，参数验证

**位置**：`src/main/java/com/agenthub/api/`

| 子模块 | 说明 |
|-------|------|
| `controller/` | REST 控制器，定义 API 端点（22个控制器） |
| `dto/` | 请求/响应 DTO（93个） |
| `mapper/` | DTO 与领域模型之间的转换器 |
| `exception/` | 全局异常处理与错误响应 |

**典型结构**：

```java
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/agents")
public class AgentHubController {
    private final AgentUseCase agentUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AgentResponse createAgent(@PathVariable String workspaceId,
                                      @RequestBody CreateAgentRequest request) {
        AgentOutput output = agentUseCase.create(workspaceId, request);
        return AgentResponse.from(output);
    }
}
```

### 2.2 应用层（Application）

**职责**：用例编排、事务管理、输入验证、输出转换

**位置**：`src/main/java/com/agenthub/application/`

| 子模块 | 说明 |
|-------|------|
| `usecase/` | 用例实现（33个），编排业务逻辑流程 |
| `command/` | 命令对象，封装用例输入参数 |
| `dto/` | 应用层输出 DTO |
| `service/` | 应用服务，跨用例的公共服务 |
| `executor/` | 命令执行器 |
| `port/in/` | 输入端口（驱动端口） |
| `port/out/` | 输出端口（驱动适配端口） |

**端口分类**：

```
application/port/out/
├── agent/          # Agent 运行端口
├── etl/            # ETL 管道端口
├── rag/            # RAG 检索端口
├── repositories/   # 数据仓储端口（25个接口）
└── tools/          # 工具调用端口
```

**典型用例**：

```java
@Component
@RequiredArgsConstructor
public class AgentUseCase {
    private final AgentRepository agentRepository;

    public AgentOutput get(String agentId) {
        Agent agent = agentRepository.findById(agentId)
            .orElseThrow(() -> new NotFoundException("Agent not found: " + agentId));
        return AgentOutput.from(agent);
    }
}
```

### 2.3 领域层（Domain）

**职责**：核心业务逻辑、业务规则、领域模型、领域事件

**位置**：`src/main/java/com/agenthub/domain/`

| 子模块 | 说明 |
|-------|------|
| `model/` | 领域模型（57个），含聚合根和值对象 |
| `event/` | 领域事件定义 |
| `exception/` | 领域异常（17个） |

**核心领域模型**：

| 领域模型 | 类型 | 说明 |
|---------|------|------|
| `Agent` | 聚合根 | Agent 实体，管理生命周期状态 |
| `AgentTeam` | 聚合根 | 多 Agent 团队，管理协作模式 |
| `Workflow` | 聚合根 | 工作流定义，包含 DAG 图 |
| `Memory` | 聚合根 | Agent 长期记忆 |
| `Skill` | 聚合根 | 技能定义 |
| `Session` | 实体 | 聊天会话 |
| `KnowledgeBase` | 实体 | 知识库 |
| `IngestionDocument` | 值对象 | 入库文档（不可变） |
| `RetrievalStrategy` | 聚合根 | 检索策略配置 |
| `GuardrailStrategy` | 聚合根 | 护栏策略配置 |
| `Permission` | 值对象 | 权限常量与角色映射 |

**领域层约束**（ArchUnit 强制校验）：

- 不得依赖 Spring 框架注解
- 不得依赖 JPA/HTTP/Kafka 等外部框架类型
- 不得依赖基础设施层或 API 层

### 2.4 基础设施层（Infrastructure）

**职责**：实现端口接口，适配外部框架和 SDK

**位置**：`src/main/java/com/agenthub/infrastructure/`

| 子模块 | 说明 |
|-------|------|
| `agents/` | Agent 运行时实现（Spring AI Alibaba + AgentScope） |
| `auth/` | JWT 认证、凭证验证、Token 管理 |
| `context/` | 多租户上下文、工作空间上下文、请求上下文 |
| `etl/` | 文档 ETL 管道（解析→清洗→分块→向量化） |
| `rag/` | RAG 流水线（查询改写→双路检索→重排→过滤） |
| `store/` | 数据存储实现（DB/Redis/MinIO） |
| `tools/` | 工具调用实现（HTTP/MCP/技能/系统） |
| `vector/` | 多向量数据库适配（Qdrant/Milvus/Weaviate 等） |
| `web/` | Web 相关配置 |
| `wiki/` | Wiki 集成 |

---

## 3. 核心业务流程

### 3.1 Agent 生命周期

```
┌─────────┐     ┌─────────┐     ┌─────────┐     ┌─────────┐     ┌─────────┐
│ CREATED │────▶│ STARTING│────▶│ RUNNING │────▶│ STOPPING│────▶│ STOPPED │
└─────────┘     └─────────┘     └─────────┘     └─────────┘     └─────────┘
                                    │                                │
                                    ▼                                ▼
                               ┌─────────┐                     ┌─────────┐
                               │ PAUSED  │                     │  ERROR  │
                               └─────────┘                     └─────────┘
```

### 3.2 知识库文档处理流水线（ETL）

```
上传文档 → 解析(Parser) → 清洗(Cleaner) → 分块(Chunker) → 向量化(Vectorization) → 存储
   │          │               │               │                │                  │
   ▼          ▼               ▼               ▼                ▼                  ▼
UPLOADED   PARSED         CLEANED         CHUNKED         VECTORIZED        COMPLETED
                                                                                │
                                                                                ▼
                                                                              FAILED
```

### 3.3 RAG 检索流水线

```
用户查询
    │
    ▼
┌──────────────────┐
│ 查询改写(Optional) │
│ - 翻译查询        │
│ - 压缩查询        │
└──────────────────┘
    │
    ▼
┌──────────────────────────────┐
│      双路检索(Hybrid)         │
│  ┌──────────┐ ┌────────────┐ │
│  │语义检索   │ │关键词检索   │ │
│  │(向量搜索) │ │(ES全文搜索) │ │
│  └──────────┘ └────────────┘ │
│  weight=0.7    weight=0.3   │
└──────────────────────────────┘
    │
    ▼
┌──────────────────┐
│ 合并与重排(Rerank)│
│ - Cross-Encoder  │
│ - 关键词重排      │
└──────────────────┘
    │
    ▼
┌──────────────────┐
│ 过滤与引用生成    │
│ - 分数阈值过滤    │
│ - Top-K 截断      │
│ - 引用标注        │
└──────────────────┘
    │
    ▼
   检索结果
```

### 3.4 Agent 聊天流程

```
用户 → [Controller] → [AgentChatUseCase] → [RagChatUseCase]
                                              │
                                              ├─ 验证 Agent 状态
                                              ├─ 输入护栏检查
                                              ├─ 加载策略配置
                                              ├─ 加载知识库配置
                                              ├─ 加载提示词模板
                                              ├─ 调用 RAG 检索
                                              ├─ 调用 AI 模型
                                              ├─ 输出护栏检查
                                              └─ 保存会话消息
                                              │
                                              ▼
                                           返回结果
```

---

## 4. 多 Agent 团队协作

Agent Team 支持多种协作模式：

| 模式 | 说明 |
|------|------|
| **顺序（Sequential）** | Agent 按顺序依次处理，后一个接收前一个的输出 |
| **广播（Broadcast）** | 所有 Agent 同时处理相同输入 |
| **路由（Routing）** | 根据规则将输入路由到特定 Agent |
| **投票（Voting）** | 多个 Agent 独立处理并投票决定最终输出 |

---

## 5. 数据存储架构

```
┌─────────────────────────────────────────────────────┐
│                     AgentHub                          │
├─────────────────────────────────────────────────────┤
│                                                        │
│  ┌──────────┐  ┌───────┐  ┌───────────┐  ┌────────┐ │
│  │PostgreSQL│  │ Redis │  │ MinIO     │  │Kafka   │ │
│  │(主数据库) │  │(缓存) │  │(对象存储) │  │(消息)  │ │
│  └──────────┘  └───────┘  └───────────┘  └────────┘ │
│                                                        │
│  ┌─────────────────────────────────────────────┐      │
│  │         向量数据库（可选一种或多种）           │      │
│  │  Qdrant │ Milvus │ Weaviate │ Chroma │ PGVector│   │
│  └─────────────────────────────────────────────┘      │
│                                                        │
│  ┌────────────────┐                                   │
│  │ Elasticsearch  │ (全文检索，可选)                   │
│  └────────────────┘                                   │
└─────────────────────────────────────────────────────┘
```

---

## 6. 多租户架构

AgentHub 支持三层隔离模型：

| 隔离级别 | 说明 |
|---------|------|
| L1 | 共享数据库 Schema，数据通过 tenant_id 逻辑隔离 |
| L2 | 独立 Schema，每个租户拥有独立的数据库 Schema |
| L3 | 独立数据库实例，完全物理隔离 |

### 租户与工作空间关系

```
Tenant (租户)
  ├── Workspace (工作空间)
  │   ├── Agents (智能体)
  │   ├── Knowledge Bases (知识库)
  │   ├── Skills (技能)
  │   ├── Strategies (策略)
  │   ├── Workflows (工作流)
  │   └── Teams (团队)
  ├── Users (用户)
  └── Billing (计费)
```

### 角色权限体系

| 角色 | 权限级别 |
|------|---------|
| **OWNER** | 全部权限，包括租户管理、账单管理 |
| **ADMIN** | 工作空间管理、知识库、Agent、工具、策略、成员管理 |
| **DEVELOPER** | 知识库读写、Agent 创建/更新/发布、工具调用、策略配置 |
| **VIEWER** | 只读权限，可查看工作空间内容 |
| **AUDITOR** | 审计日志读取和导出权限 |

---

## 7. 安全架构

```
┌────────────────────────────────────────┐
│           客户端请求                      │
│         + Authorization: Bearer JWT     │
└────────────────┬───────────────────────┘
                 │
                 ▼
┌────────────────────────────────────────┐
│         AuthFilter (JWT验证)            │
│  1. 解析 Token                         │
│  2. 验证签名和过期时间                   │
│  3. 提取用户信息和权限                   │
└────────────────┬───────────────────────┘
                 │
                 ▼
┌────────────────────────────────────────┐
│      TenantContext (租户上下文)          │
│  1. 设置当前租户                        │
│  2. 设置当前用户                        │
│  3. 设置当前工作空间                     │
└────────────────┬───────────────────────┘
                 │
                 ▼
┌────────────────────────────────────────┐
│       请求处理 → 权限校验                 │
│  - 基于 Permission 常量的细粒度校验       │
│  - 数据级隔离（tenant_id 过滤）           │
└────────────────────────────────────────┘
```

---

## 8. 设计模式

| 模式 | 应用场景 |
|------|---------|
| **Port & Adapter** | `application/port/` 接口与 `infrastructure/` 实现 |
| **Strategy** | 多向量数据库切换、多种检索策略、多种协作模式 |
| **Factory** | Agent 工厂（ReActAgentFactory）、工具工厂（ToolsFactory） |
| **Builder** | RAG 流水线构建、查询构建 |
| **Adapter** | ETL 管道适配器、RAG 适配器、向量存储适配器 |
| **Template Method** | AbstractReActAgent、AbstractTeamAgent |
| **Domain Event** | AgentConfigChangedEvent、AgentConfigDeletedEvent |
| **Repository** | 25个 MyBatis 仓储实现 |
| **Chain of Responsibility** | ETL 流水线、RAG 检索流水线 |

---

## 9. 关键技术决策

### 9.1 为何选择 Spring AI Alibaba Agent Framework

- 与 Spring AI 生态深度集成
- 支持 ReAct 模式的 Agent 实现
- 提供 MCP 客户端支持
- 与阿里云 AI 服务无缝对接

### 9.2 为何支持多种向量数据库

- 不同场景对向量数据库需求不同
- 避免供应商锁定
- 通过策略模式实现无缝切换

### 9.3 为何采用 MyBatis-Plus 而非 JPA

- 更灵活的 SQL 控制
- 更好的分页查询支持
- 与整洁架构更适配（Repository 模式）

---

## 10. 相关文档

| 文档 | 说明 |
|------|------|
| [数据模型](data-model.md) | 领域模型与实体定义 |
| [API 参考](api-reference.md) | REST API 端点参考 |
| [配置参考](configuration-reference.md) | 配置属性完整参考 |
| [开发指南](development-guide.md) | 开发者规范与流程 |
| [部署指南](deployment-guide.md) | 环境配置与部署 |
| [Spring AI ChatClient vs Agent 对比](spring-ai-chatclient-vs-agent.md) | 技术选型分析 |
