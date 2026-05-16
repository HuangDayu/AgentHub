# 数据模型

> 本文档定义 AgentHub 的核心领域模型、数据库实体及其关系。

---

## 目录

- [领域模型总览](#1-领域模型总览)
- [核心模型详解](#2-核心模型详解)
- [数据库实体关系](#3-数据库实体关系)
- [枚举定义](#4-枚举定义)

---

## 1. 领域模型总览

### 1.1 模型分类

| 分类 | 模型 | 说明 |
|------|------|------|
| **核心** | Agent, AgentTeam, Workflow, Memory, Skill | 聚合根 |
| **策略** | RetrievalStrategy, GuardrailStrategy, ModelStrategy, ToolStrategy | 策略配置 |
| **工具** | HttpTool, McpTool, SystemTool | 工具定义 |
| **知识库** | KnowledgeBase, IngestionDocument, IngestionJob, DocumentChunk, DocumentContent | 知识管理 |
| **会话** | Session, ChatMessage, SessionMessage, RuntimeSession | 会话管理 |
| **租户** | Tenant, Workspace, UserInfo, Permission | 多租户 |
| **配置** | AgentConfig, AgentConfigType, ModelConfig, PromptTemplateInfo, VectorStoreConfig | 配置项 |
| **其他** | ScheduledTask, PageResult, Citation, RetrievalResult, AuthTokens | 辅助模型 |

### 1.2 模型关系图

```
Tenant (1) ──→ (N) Workspace
Workspace (1) ──→ (N) Agent
Workspace (1) ──→ (N) KnowledgeBase
Workspace (1) ──→ (N) AgentTeam
Workspace (1) ──→ (N) Workflow
Workspace (1) ──→ (N) Skill

Agent (1) ──→ (N) AgentConfig
Agent (1) ──→ (N) Session
Agent (1) ──→ (N) Memory

KnowledgeBase (1) ──→ (N) IngestionJob
KnowledgeBase (1) ──→ (N) IngestionDocument
KnowledgeBase (1) ──→ (N) DocumentChunk
IngestionJob (1) ──→ (N) IngestionDocument

Session (1) ──→ (N) ChatMessage
```

---

## 2. 核心模型详解

### 2.1 Agent（智能体）

智能体聚合根，管理 Agent 的全生命周期。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 唯一标识 |
| tenantId | String | 所属租户 |
| workspaceId | String | 所属工作空间 |
| agentCode | String | Agent 编码 |
| name | String | 名称 |
| description | String | 描述 |
| status | String | 状态：DRAFT, PUBLISHED |
| enabled | boolean | 是否启用 |
| createdAt | Instant | 创建时间 |
| updatedAt | Instant | 更新时间 |

**状态变更方法**：
- `enabled()` → 发布 Agent（PUBLISHED）
- `unenabled()` → 取消发布（DRAFT）
- `update(name, description)` → 更新信息

### 2.2 AgentConfig（Agent 配置）

将各种资源（策略、工具、模型、提示词、知识库）关联到 Agent。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 唯一标识 |
| agentId | String | Agent ID |
| category | AgentConfigCategory | 配置分类 |
| configType | AgentConfigType | 配置类型 |
| configId | String | 配置资源 ID |
| enabled | boolean | 是否启用 |

**配置分类**：

| 分类 | 说明 | 配置类型 |
|------|------|---------|
| STRATEGY | 策略配置 | RETRIEVAL_STRATEGY, TOOL_STRATEGY, MODEL_STRATEGY, GUARDRAIL_STRATEGY |
| TOOL | 工具配置 | MCP_TOOL, SKILL_TOOL, SYSTEM_TOOL, HTTP_TOOL |
| PROMPT | 提示词配置 | SYSTEM_PROMPT, ASSISTANT_PROMPT |
| MODEL | 模型配置 | CHAT_MODEL, EMBEDDING_MODEL |
| KNOWLEDGE | 知识库配置 | KNOWLEDGE_BASE, KNOWLEDGE_WIKI |

### 2.3 AgentTeam（Agent 团队）

多 Agent 协作团队的管理聚合根。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 唯一标识 |
| tenantId | String | 所属租户 |
| workspaceId | String | 所属工作空间 |
| teamCode | String | 团队编码 |
| name | String | 团队名称 |
| description | String | 描述 |
| coordinationMode | String | 协作模式 |
| memberConfig | String | 成员配置（JSON） |
| status | String | 状态：DRAFT, ACTIVE, INACTIVE |

**协作模式**：
- `SEQUENTIAL`：顺序处理
- `BROADCAST`：广播处理
- `ROUTING`：路由分发
- `VOTING`：投票决策

### 2.4 Workflow（工作流）

工作流聚合根，基于 DAG 图定义。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 唯一标识 |
| tenantId | String | 所属租户 |
| workspaceId | String | 所属工作空间 |
| workflowCode | String | 工作流编码 |
| name | String | 名称 |
| description | String | 描述 |
| graphDefinition | String | DAG 图定义（JSON） |
| status | String | 状态：DRAFT, PUBLISHED |

### 2.5 Memory（记忆）

Agent 长期记忆存储聚合根。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 唯一标识 |
| tenantId | String | 所属租户 |
| workspaceId | String | 所属工作空间 |
| agentId | String | 所属 Agent |
| memoryType | String | 记忆类型 |
| content | String | 记忆内容 |
| metadata | String | 元数据（JSON） |
| importance | double | 重要性（0-1） |
| expiresAt | Instant | 过期时间 |

### 2.6 Skill（技能）

Agent 可调用的技能定义聚合根。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 唯一标识 |
| tenantId | String | 所属租户 |
| workspaceId | String | 所属工作空间 |
| skillCode | String | 技能编码 |
| name | String | 技能名称 |
| description | String | 描述 |
| skillType | String | 技能类型 |
| skillPath | String | 技能文件路径 |
| skillFilesTree | String | 技能文件树 |
| enabled | boolean | 是否启用 |

### 2.7 KnowledgeBase（知识库）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 唯一标识 |
| workspaceId | String | 所属工作空间 |
| name | String | 知识库名称 |
| description | String | 描述 |

### 2.8 IngestionDocument（入库文档）

不可变值对象，表示文档处理管道中的状态。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 文档唯一标识 |
| kbId | String | 所属知识库 ID |
| jobId | String | 所属任务 ID |
| fileName | String | 文件名 |
| contentType | String | MIME 类型 |
| size | long | 文件大小（字节） |
| storagePath | String | MinIO 存储路径 |
| status | DocumentStatus | 处理状态 |

**文档处理状态**：
```
UPLOADED → PARSED → CLEANED → CHUNKED → VECTORIZED
                                              ↓
                                           FAILED
```

### 2.9 RetrievalStrategy（检索策略）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 唯一标识 |
| tenantId | String | 所属租户 |
| workspaceId | String | 所属工作空间 |
| name | String | 策略名称 |
| description | String | 描述 |
| retrievalType | RetrievalType | 检索类型（HYBRID, SEMANTIC, KEYWORD） |
| topK | int | 返回结果数 |
| scoreThreshold | double | 分数阈值 |
| enableQueryRewrite | boolean | 启用查询改写 |
| enableTextSearch | boolean | 启用全文检索 |
| enableVectorSearch | boolean | 启用向量检索 |
| enableRerank | boolean | 启用重排 |
| vectorWeight | double | 向量检索权重 |
| keywordWeight | double | 关键词检索权重 |

### 2.10 GuardrailStrategy（护栏策略）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 唯一标识 |
| tenantId | String | 所属租户 |
| workspaceId | String | 所属工作空间 |
| name | String | 策略名称 |
| description | String | 描述 |
| inputValidationEnabled | boolean | 输入验证 |
| outputValidationEnabled | boolean | 输出验证 |
| piiDetectionEnabled | boolean | PII 检测 |
| piiMaskingEnabled | boolean | PII 脱敏 |
| promptInjectionDetection | boolean | 提示注入检测 |
| maxInputLength | int | 最大输入长度 |
| maxOutputLength | int | 最大输出长度 |

### 2.11 AgentLifecycleState（Agent 生命周期状态枚举）

```java
CREATED   - "已创建，未启动"
STARTING  - "正在启动"
RUNNING   - "运行中"
PAUSED    - "已暂停"
STOPPING  - "正在停止"
STOPPED   - "已停止"
ERROR     - "错误状态"
```

### 2.12 Permission（权限定义）

| 权限常量 | 说明 |
|---------|------|
| `tenant:read/update/delete` | 租户管理 |
| `workspace:create/read/update/delete` | 工作空间 |
| `knowledge:create/read/update/delete` | 知识库 |
| `agent:create/read/update/delete/publish` | Agent 管理 |
| `tool:create/read/update/delete/invoke` | 工具管理 |
| `strategy:create/read/update/delete` | 策略管理 |
| `member:create/read/update/delete` | 成员管理 |
| `audit:read/export` | 审计 |
| `billing:read/update` | 账单 |

**角色权限矩阵**：

| 权限 | OWNER | ADMIN | DEVELOPER | VIEWER | AUDITOR |
|------|-------|-------|-----------|--------|---------|
| Tenant 管理 | ✅ | ❌ | ❌ | ❌ | ❌ |
| Workspace 管理 | ✅ | ✅(部分) | ✅(只读) | ✅(只读) | ❌ |
| Knowledge 管理 | ✅ | ✅ | ✅(无删除) | ✅(只读) | ❌ |
| Agent 管理 | ✅ | ✅ | ✅(无删除) | ✅(只读) | ❌ |
| Tool 管理 | ✅ | ✅ | ✅(只读+调用) | ✅(只读) | ❌ |
| Strategy 管理 | ✅ | ✅ | ✅(无删除) | ✅(只读) | ❌ |
| Member 管理 | ✅ | ✅ | ❌ | ❌ | ❌ |
| Audit | ✅ | ✅ | ✅(只读) | ✅(只读) | ✅ |
| Billing | ✅ | ✅(只读) | ❌ | ❌ | ✅(只读) |

---

## 3. 数据库实体关系

### 3.1 Entity 映射

每个领域模型对应一个数据库实体，位于 `infrastructure/store/db/entity/`：

| Entity | 对应表（预估） | 说明 |
|--------|--------------|------|
| AgentEntity | agent | Agent 表 |
| AgentConfigEntity | agent_config | Agent 配置关联表 |
| AgentTeamEntity | agent_team | Agent 团队表 |
| AppUserEntity | app_user | 用户表 |
| ChatMessageEntity | chat_message | 聊天消息表 |
| DocumentChunkEntity | document_chunk | 文档分块表 |
| GuardrailStrategyEntity | guardrail_strategy | 护栏策略表 |
| HttpToolsEntity | http_tools | HTTP 工具表 |
| IngestionDocumentEntity | ingestion_document | 入库文档表 |
| IngestionJobEntity | ingestion_job | 入库任务表 |
| KnowledgeBaseEntity | knowledge_base | 知识库表 |
| McpToolEntity | mcp_tool | MCP 工具表 |
| MemoryEntity | memory | 记忆表 |
| ModelConfigEntity | model_config | 模型配置表 |
| ModelStrategyEntity | model_strategy | 模型策略表 |
| PromptTemplateEntity | prompt_template | 提示词模板表 |
| RefreshTokenSessionEntity | refresh_token_session | 刷新令牌表 |
| RetrievalStrategyEntity | retrieval_strategy | 检索策略表 |
| ScheduledTaskEntity | scheduled_task | 定时任务表 |
| SessionEntity | session | 会话表 |
| SkillEntity | skill | 技能表 |
| SystemToolsEntity | system_tools | 系统工具表 |
| TenantEntity | tenant | 租户表 |
| ToolPolicyBindingEntity | tool_policy_binding | 工具策略绑定表 |
| ToolStrategyEntity | tool_strategy | 工具策略表 |
| VectorStoreConfigEntity | vector_store_config | 向量存储配置表 |
| WorkflowEntity | workflow | 工作流表 |
| WorkspaceEntity | workspace | 工作空间表 |

### 3.2 仓储实现

每个 Repository 接口对应一个 MyBatis 实现：

| 仓储接口 | 实现类 |
|---------|--------|
| AgentRepository | MybatisAgentRepository |
| AgentConfigRepository | MybatisAgentConfigRepository |
| AgentTeamRepository | MybatisAgentTeamRepository |
| SessionRepository | MybatisSessionRepository |
| KnowledgeBaseRepository | MybatisKnowledgeBaseRepository |
| MemoryRepository | MybatisMemoryRepository |
| ...（共 25 个） | ... |

---

## 4. 枚举定义

| 枚举 | 位置 | 值 |
|------|------|-----|
| AgentLifecycleState | domain/model | CREATED, STARTING, RUNNING, PAUSED, STOPPING, STOPPED, ERROR |
| AgentConfigCategory | domain/model | STRATEGY, TOOL, PROMPT, MODEL, KNOWLEDGE |
| AgentConfigType | domain/model | 多种配置类型 |
| AgentTeamType | domain/model | 团队类型 |
| AgentToolType | domain/model | TOOL, MCP_TOOL, SYSTEM_TOOL |
| DocumentStatus | domain/model | UPLOADED, PARSED, CLEANED, CHUNKED, VECTORIZED, FAILED |
| JobPhase | domain/model | PARSING, CLEANING, CHUNKING, VECTORIZATION |
| JobStatus | domain/model | PENDING, PROCESSING, COMPLETED, FAILED |
| RetrievalType | domain/model | HYBRID, SEMANTIC, KEYWORD |
| IsolationLevel | domain/model | L1, L2, L3 |
| TenantStatus | domain/model | ACTIVE, SUSPENDED, DELETED |
| ModelSupplier | domain/model | OPENAI, OLLAMA, ALIBABA_CLOUD, AZURE_OPENAI |
| ModelType | domain/model | CHAT, EMBEDDING |
| VectorStoreType | domain/model | QDRANT, MILVUS, WEAVIATE, CHROMA, PGVECTOR |
| TaskType | domain/model | 任务类型 |
