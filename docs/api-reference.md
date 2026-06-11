# API 参考文档

> 本文档提供 AgentHub 完整的 REST API 端点参考。所有 API 统一前缀为 `/api/v1`，认证通过 JWT Token（Bearer Authentication）进行。

---

## 目录

- [认证 API](#1-认证-api)
- [租户管理 API](#2-租户管理-api)
- [工作空间 API](#3-工作空间-api)
- [Agent 管理 API](#4-agent-管理-api)
- [Agent 配置 API](#5-agent-配置-api)
- [Agent 团队 API](#6-agent-团队-api)
- [会话 API](#7-会话-api)
- [知识库 API](#8-知识库-api)
- [工作流 API](#9-工作流-api)
- [模型配置 API](#10-模型配置-api)
- [模型策略 API](#11-模型策略-api)
- [向量存储 API](#12-向量存储-api)
- [MCP 工具 API](#13-mcp-工具-api)
- [HTTP 工具 API](#14-http-工具-api)
- [系统工具 API](#15-系统工具-api)
- [技能管理 API](#16-技能管理-api)
- [提示词模板 API](#17-提示词模板-api)
- [检索策略 API](#18-检索策略-api)
- [工具策略 API](#19-工具策略-api)
- [护栏策略 API](#20-护栏策略-api)
- [记忆 API](#21-记忆-api)
- [定时任务 API](#22-定时任务-api)
- [配置类型 API](#23-配置类型-api)
- [AgentHub 聚合 API](#24-agenthub-聚合-api)

---

## 通用说明

### 认证方式

所有 API（除登录外）需要在请求头中携带 JWT Token：

```
Authorization: Bearer <access_token>
```

### 基础 URL

```
http://localhost:8080/api/v1
```

### 路径参数

| 参数 | 说明 |
|------|------|
| `{workspaceId}` | 工作空间 ID |
| `{agentId}` | Agent ID |
| `{sessionId}` | 会话 ID |
| `{kbId}` | 知识库 ID |
| `{teamId}` | 团队 ID |
| `{workflowId}` | 工作流 ID |
| `{skillId}` | 技能 ID |

### 通用响应格式

**成功响应**：直接返回 JSON 对象或数组

**错误响应**：
```json
{
  "error": "错误描述",
  "status": 400
}
```

---

## 1. 认证 API

### POST /api/v1/auth/login

用户登录，获取访问令牌。

**请求体**：
```json
{
  "username": "string",
  "password": "string"
}
```

**响应**：
```json
{
  "accessToken": "string",
  "refreshToken": "string",
  "expiresIn": 3600000
}
```

### POST /api/v1/auth/refresh

刷新访问令牌。

**请求体**：
```json
{
  "refreshToken": "string"
}
```

**响应**：
```json
{
  "accessToken": "string",
  "refreshToken": "string",
  "expiresIn": 3600000
}
```

### POST /api/v1/auth/logout

登出并失效令牌。

---

## 2. 租户管理 API

基础路径：`/api/v1/tenants`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/tenants` | 查询租户列表 |
| POST | `/api/v1/tenants` | 创建租户 |
| GET | `/api/v1/tenants/{tenantId}` | 获取租户详情 |
| PATCH | `/api/v1/tenants/{tenantId}` | 更新租户信息 |

### POST /api/v1/tenants

创建租户。

**请求体**：
```json
{
  "tenantCode": "string",
  "name": "string",
  "planCode": "string",
  "region": "string"
}
```

**响应**：
```json
{
  "id": "string",
  "tenantCode": "string",
  "name": "string",
  "planCode": "string",
  "isolationLevel": "L1|L2|L3",
  "region": "string",
  "status": "ACTIVE|SUSPENDED|DELETED",
  "createdAt": "2026-01-01T00:00:00Z"
}
```

---

## 3. 工作空间 API

基础路径：`/api/v1/workspaces`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/workspaces` | 查询工作空间列表 |
| POST | `/api/v1/workspaces` | 创建工作空间 |
| GET | `/api/v1/workspaces/{workspaceId}` | 获取工作空间详情 |
| PATCH | `/api/v1/workspaces/{workspaceId}` | 更新工作空间 |
| DELETE | `/api/v1/workspaces/{workspaceId}` | 删除工作空间 |

---

## 4. Agent 管理 API

基础路径：`/api/v1/workspaces/{workspaceId}/agents`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/workspaces/{workspaceId}/agents` | 查询 Agent 列表 |
| POST | `/api/v1/workspaces/{workspaceId}/agents` | 创建 Agent |
| GET | `/api/v1/workspaces/{workspaceId}/agents/{agentId}` | 获取 Agent 详情 |
| PATCH | `/api/v1/workspaces/{workspaceId}/agents/{agentId}` | 更新 Agent |
| DELETE | `/api/v1/workspaces/{workspaceId}/agents/{agentId}` | 删除 Agent |
| POST | `.../agents/{agentId}/publish` | 发布 Agent |
| POST | `.../agents/{agentId}/unpublish` | 取消发布 Agent |

### POST /api/v1/workspaces/{workspaceId}/agents

创建 Agent。

**请求体**：
```json
{
  "agentCode": "string",
  "name": "string",
  "description": "string"
}
```

**响应**：
```json
{
  "id": "string",
  "agentCode": "string",
  "name": "string",
  "description": "string",
  "status": "DRAFT|PUBLISHED",
  "enabled": false,
  "createdAt": "2026-01-01T00:00:00Z"
}
```

---

## 5. Agent 配置 API

基础路径：`/api/v1/workspaces/{workspaceId}/agents/{agentId}/configs`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/workspaces/{workspaceId}/agents/{agentId}/configs` | 查询配置列表（支持 ?category= 过滤） |
| POST | `/api/v1/workspaces/{workspaceId}/agents/{agentId}/configs` | 设置配置 |
| GET | `.../agents/{agentId}/configs/{configId}` | 获取配置详情 |
| PUT | `.../agents/{agentId}/configs/{configId}` | 更新配置 |
| DELETE | `.../agents/{agentId}/configs/{configId}` | 删除配置 |
| DELETE | `.../agents/{agentId}/configs` | 删除所有配置 |
| POST | `.../agents/{agentId}/configs/sync` | 同步配置 |

### 配置分类

| 分类 | 说明 | 支持类型 |
|------|------|---------|
| `STRATEGY` | 策略配置 | RETRIEVAL_STRATEGY, TOOL_STRATEGY, MODEL_STRATEGY, GUARDRAIL_STRATEGY |
| `TOOL` | 工具配置 | MCP_TOOL, SKILL_TOOL, SYSTEM_TOOL, HTTP_TOOL |
| `PROMPT` | 提示词配置 | SYSTEM_PROMPT, ASSISTANT_PROMPT |
| `MODEL` | 模型配置 | CHAT_MODEL, EMBEDDING_MODEL |
| `KNOWLEDGE` | 知识库配置 | KNOWLEDGE_BASE, KNOWLEDGE_WIKI |

### POST /api/v1/workspaces/{workspaceId}/agents/{agentId}/configs

设置配置。

**请求体**：
```json
{
  "category": "STRATEGY",
  "configType": "RETRIEVAL_STRATEGY",
  "configId": "string",
  "enabled": true
}
```

---

## 6. Agent 团队 API

基础路径：`/api/v1/workspaces/{workspaceId}/teams`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/workspaces/{workspaceId}/teams` | 查询团队列表 |
| POST | `/api/v1/workspaces/{workspaceId}/teams` | 创建团队 |
| GET | `.../teams/{teamId}` | 获取团队详情 |
| PATCH | `.../teams/{teamId}` | 更新团队 |
| DELETE | `.../teams/{teamId}` | 删除团队 |
| POST | `.../teams/{teamId}/activate` | 激活团队 |
| POST | `.../teams/{teamId}/deactivate` | 停用团队 |

### POST /api/v1/workspaces/{workspaceId}/teams

创建团队。

**请求体**：
```json
{
  "teamCode": "string",
  "name": "string",
  "description": "string",
  "coordinationMode": "SEQUENTIAL|BROADCAST|ROUTING|VOTING",
  "memberConfig": "string"
}
```

**响应**：
```json
{
  "id": "string",
  "teamCode": "string",
  "name": "string",
  "description": "string",
  "coordinationMode": "SEQUENTIAL",
  "memberConfig": "string",
  "status": "DRAFT|ACTIVE|INACTIVE",
  "createdAt": "2026-01-01T00:00:00Z"
}
```

---

## 7. 会话 API

基础路径：`/api/v1/workspaces/{workspaceId}/agents/{agentId}/sessions`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `.../agents/{agentId}/sessions` | 查询会话列表 |
| POST | `.../agents/{agentId}/sessions` | 创建会话 |
| DELETE | `.../sessions/{sessionId}` | 删除会话 |
| GET | `.../sessions/{sessionId}/messages` | 获取消息历史 |
| POST | `.../sessions/{sessionId}/messages` | 发送消息 |
| POST | `.../sessions/{sessionId}/messages/stream` | 流式发送消息（SSE） |

### POST .../sessions/{sessionId}/messages

发送消息并获取回复（非流式）。

**请求体**：
```json
{
  "content": "你的问题"
}
```

**响应**：
```json
{
  "content": "Agent的回复内容"
}
```

### POST .../sessions/{sessionId}/messages/stream

流式发送消息，返回 Server-Sent Events (SSE) 流。

```
Content-Type: text/event-stream

data: {"content": "单词1"}
data: {"content": "单词2"}
data: [DONE]
```

---

## 8. 知识库 API

基础路径：`/api/v1/workspaces/{workspaceId}/knowledge-bases`

### 知识库管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `.../knowledge-bases` | 查询知识库列表 |
| POST | `.../knowledge-bases` | 创建知识库 |
| GET | `.../knowledge-bases/{kbId}` | 获取知识库详情 |
| PATCH | `.../knowledge-bases/{kbId}` | 更新知识库 |
| DELETE | `.../knowledge-bases/{kbId}` | 删除知识库 |

### 文档管理

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `.../knowledge-bases/{kbId}/documents` | 上传文档（multipart/form-data） |
| GET | `.../knowledge-bases/{kbId}/documents` | 查询文档列表 |
| PUT | `.../knowledge-bases/{kbId}/documents/{docId}` | 重新向量化文档 |
| DELETE | `.../knowledge-bases/{kbId}/documents/{docId}` | 删除文档 |
| GET | `.../knowledge-bases/ingestion-jobs/{jobId}` | 查询入库任务状态 |

### 检索

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `.../knowledge-bases/{kbId}/retrieve` | 执行快速检索 |
| GET | `.../knowledge-bases/{kbId}/retrieve/stream` | 流式检索（SSE） |
| POST | `.../knowledge-bases/{kbId}/search` | 高级检索（含查询改写、重排） |

### POST .../knowledge-bases/{kbId}/documents

上传文档（支持单文件或多文件）。

```
Content-Type: multipart/form-data

file: @文档.pdf
或
files: @文档1.pdf, @文档2.docx
```

**响应**：
```json
{
  "jobId": "string",
  "status": "PROCESSING",
  "documentCount": 2,
  "createdAt": "2026-01-01T00:00:00Z"
}
```

### POST .../knowledge-bases/{kbId}/search

高级检索（检索流水线：查询改写 → 双路检索 → 合并 → 重排 → 过滤 → 引用生成）。

**请求体**：
```json
{
  "query": "搜索关键词",
  "topK": 10,
  "scoreThreshold": 0.75,
  "enableRerank": true,
  "enableQueryRewrite": true
}
```

**响应**：
```json
{
  "originalQuery": "搜索关键词",
  "rewrittenQuery": "改写后的查询",
  "results": [
    {
      "chunkId": "string",
      "content": "文档片段内容",
      "score": 0.95,
      "source": "文件名.pdf",
      "pageNumber": 1,
      "metadata": {}
    }
  ]
}
```

---

## 9. 工作流 API

基础路径：`/api/v1/workspaces/{workspaceId}/dag-workflows`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `.../dag-workflows` | 查询工作流列表 |
| POST | `.../dag-workflows` | 创建工作流 |
| GET | `.../dag-workflows/{workflowId}` | 获取工作流详情 |
| PUT | `.../dag-workflows/{workflowId}` | 更新工作流 |
| DELETE | `.../dag-workflows/{workflowId}` | 删除工作流 |
| POST | `.../dag-workflows/{workflowId}/publish` | 发布工作流 |
| POST | `.../dag-workflows/{workflowId}/unpublish` | 取消发布工作流 |

### POST /api/v1/workspaces/{workspaceId}/dag-workflows

创建工作流。

**请求体**：
```json
{
  "workflowCode": "string",
  "name": "string",
  "description": "string",
  "graphDefinition": "{\"nodes\":[],\"edges\":[]}"
}
```

---

## 10. 模型配置 API

基础路径：`/api/v1/workspaces/{workspaceId}/model-configs`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `.../model-configs` | 查询模型配置列表 |
| POST | `.../model-configs` | 创建模型配置 |
| GET | `.../model-configs/{configId}` | 获取模型配置详情 |
| PUT | `.../model-configs/{configId}` | 更新模型配置 |
| PATCH | `.../model-configs/{configId}` | 部分更新模型配置 |
| DELETE | `.../model-configs/{configId}` | 删除模型配置 |

支持模型供应商：
- OPENAI
- OLLAMA
- ALIBABA_CLOUD
- AZURE_OPENAI

---

## 11. 模型策略 API

基础路径：`/api/v1/workspaces/{workspaceId}/model-strategies`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `.../model-strategies` | 查询模型策略列表 |
| POST | `.../model-strategies` | 创建模型策略 |
| GET | `.../model-strategies/{strategyId}` | 获取模型策略详情 |
| PUT | `.../model-strategies/{strategyId}` | 更新模型策略 |
| DELETE | `.../model-strategies/{strategyId}` | 删除模型策略 |

---

## 12. 向量存储 API

基础路径：`/api/v1/workspaces/{workspaceId}/vector-store-configs`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `.../vector-store-configs` | 查询向量存储配置列表 |
| POST | `.../vector-store-configs` | 创建向量存储配置 |
| GET | `.../vector-store-configs/{configId}` | 获取配置详情 |
| PUT | `.../vector-store-configs/{configId}` | 更新配置 |
| DELETE | `.../vector-store-configs/{configId}` | 删除配置 |

支持向量数据库类型：
- QDRANT
- MILVUS
- WEAVIATE
- CHROMA
- PGVECTOR

---

## 13. MCP 工具 API

基础路径：`/api/v1/workspaces/{workspaceId}/mcp-tools`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `.../mcp-tools` | 查询 MCP 工具列表 |
| POST | `.../mcp-tools` | 注册 MCP 工具 |
| PATCH | `.../mcp-tools/{toolId}` | 更新 MCP 工具 |
| DELETE | `.../mcp-tools/{toolId}` | 删除 MCP 工具 |

---

## 14. HTTP 工具 API

基础路径：`/api/v1/workspaces/{workspaceId}/http-tools`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `.../http-tools` | 查询 HTTP 工具列表 |
| POST | `.../http-tools` | 创建 HTTP 工具 |
| GET | `.../http-tools/{toolId}` | 获取工具详情 |
| PUT | `.../http-tools/{toolId}` | 更新工具 |
| DELETE | `.../http-tools/{toolId}` | 删除工具 |
| POST | `.../http-tools/{toolId}/invoke` | 调用 HTTP 工具 |

---

## 15. 系统工具 API

基础路径：`/api/v1/workspaces/{workspaceId}/system-tools`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `.../system-tools` | 查询系统工具列表 |
| POST | `.../system-tools` | 注册系统工具 |
| GET | `.../system-tools/{toolId}` | 获取工具详情 |
| PUT | `.../system-tools/{toolId}` | 更新工具 |
| DELETE | `.../system-tools/{toolId}` | 删除工具 |
| POST | `.../system-tools/{toolId}/invoke` | 调用系统工具 |

---

## 16. 技能管理 API

基础路径：`/api/v1/workspaces/{workspaceId}/skills`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `.../skills` | 查询技能列表 |
| POST | `.../skills` | 创建技能 |
| GET | `.../skills/{skillId}` | 获取技能详情 |
| PATCH | `.../skills/{skillId}` | 更新技能 |
| DELETE | `.../skills/{skillId}` | 删除技能 |
| POST | `.../skills/{skillId}/enable` | 启用技能 |
| POST | `.../skills/{skillId}/disable` | 禁用技能 |

---

## 17. 提示词模板 API

基础路径：`/api/v1/workspaces/{workspaceId}/prompt-templates`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `.../prompt-templates` | 查询提示词模板列表 |
| POST | `.../prompt-templates` | 创建提示词模板 |
| GET | `.../prompt-templates/{templateId}` | 获取模板详情 |
| PUT | `.../prompt-templates/{templateId}` | 更新模板 |
| DELETE | `.../prompt-templates/{templateId}` | 删除模板 |

---

## 18. 检索策略 API

基础路径：`/api/v1/workspaces/{workspaceId}/retrieval-strategies`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `.../retrieval-strategies` | 查询检索策略列表 |
| POST | `.../retrieval-strategies` | 创建检索策略 |
| GET | `.../retrieval-strategies/{strategyId}` | 获取策略详情 |
| PUT | `.../retrieval-strategies/{strategyId}` | 更新策略 |
| DELETE | `.../retrieval-strategies/{strategyId}` | 删除策略 |

检索策略配置参数：
- `retrievalType`: HYBRID | SEMANTIC | KEYWORD
- `topK`: 返回结果数量
- `scoreThreshold`: 分数阈值
- `enableQueryRewrite`: 是否启用查询改写
- `enableTextSearch`: 是否启用全文检索
- `enableVectorSearch`: 是否启用向量检索
- `enableRerank`: 是否启用重排
- `vectorWeight`: 向量检索权重
- `keywordWeight`: 关键词检索权重

---

## 19. 工具策略 API

基础路径：`/api/v1/workspaces/{workspaceId}/tool-strategies`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `.../tool-strategies` | 查询工具策略列表 |
| POST | `.../tool-strategies` | 创建工具策略 |
| GET | `.../tool-strategies/{strategyId}` | 获取策略详情 |
| PUT | `.../tool-strategies/{strategyId}` | 更新策略 |
| DELETE | `.../tool-strategies/{strategyId}` | 删除策略 |

---

## 20. 护栏策略 API

基础路径：`/api/v1/workspaces/{workspaceId}/guardrail-strategies`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `.../guardrail-strategies` | 查询护栏策略列表 |
| POST | `.../guardrail-strategies` | 创建护栏策略 |
| GET | `.../guardrail-strategies/{strategyId}` | 获取策略详情 |
| PUT | `.../guardrail-strategies/{strategyId}` | 更新策略 |
| DELETE | `.../guardrail-strategies/{strategyId}` | 删除策略 |

护栏策略配置参数：
- `inputValidationEnabled`: 输入验证
- `outputValidationEnabled`: 输出验证
- `piiDetectionEnabled`: PII 检测
- `piiMaskingEnabled`: PII 脱敏
- `promptInjectionDetection`: 提示注入检测
- `maxInputLength`: 最大输入长度
- `maxOutputLength`: 最大输出长度

---

## 21. 记忆 API

基础路径：`/api/v1/workspaces/{workspaceId}/agents/{agentId}/memories`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `.../memories` | 查询记忆列表 |
| POST | `.../memories` | 创建记忆 |
| GET | `.../memories/{memoryId}` | 获取记忆详情 |
| PUT | `.../memories/{memoryId}` | 更新记忆 |
| DELETE | `.../memories/{memoryId}` | 删除记忆 |

### POST /api/v1/workspaces/{workspaceId}/agents/{agentId}/memories

创建记忆。

**请求体**：
```json
{
  "memoryType": "SHORT_TERM|LONG_TERM|EPISODIC|SEMANTIC",
  "content": "记忆内容",
  "metadata": "{}",
  "importance": 0.8,
  "expiresAt": "2026-12-31T00:00:00Z"
}
```

---

## 22. 定时任务 API

基础路径：`/api/v1/workspaces/{workspaceId}/scheduled-tasks`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `.../scheduled-tasks` | 查询定时任务列表 |
| POST | `.../scheduled-tasks` | 创建定时任务 |
| GET | `.../scheduled-tasks/{taskId}` | 获取任务详情 |
| PUT | `.../scheduled-tasks/{taskId}` | 更新任务 |
| DELETE | `.../scheduled-tasks/{taskId}` | 删除任务 |
| POST | `.../scheduled-tasks/{taskId}/execute` | 立即执行任务 |

---

## 23. 配置类型 API

基础路径：`/api/v1/workspaces/{workspaceId}/agent-config-types`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `.../agent-config-types` | 查询配置类型列表 |

---

## 24. AgentHub 聚合 API

基础路径：`/api/v1/workspaces/{workspaceId}/agenthub`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `.../agenthub/overview` | 获取工作空间概览数据 |

---

## 通用错误码

| HTTP 状态码 | 说明 |
|------------|------|
| 200 | 请求成功 |
| 201 | 创建成功 |
| 204 | 删除成功（无内容） |
| 400 | 请求参数错误 |
| 401 | 未认证或 Token 失效 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 409 | 资源冲突 |
| 422 | 请求体验证失败 |
| 500 | 服务器内部错误 |
