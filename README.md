<div align="center">

<h1 align="center">AgentHub</h1>

<h3 align="center">企业级 AI Agent 全生命周期管理平台</h3>

<h5 align="center">一站式管理 AI Agent 和 Agent Team 的创建、配置、运行、监控与协作</h5>

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0--M2-brightgreen)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-2.0.0--M4-blue)](https://spring.io/projects/spring-ai)
[![Java](https://img.shields.io/badge/Java-21-orange)](https://openjdk.org/)
[![Vue](https://img.shields.io/badge/Vue-3.5-brightgreen)](https://vuejs.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Build](https://img.shields.io/badge/build-Gradle-blue)](https://gradle.org/)


</div>

<p align="center">
  <a href="#项目简介">项目简介</a> •
  <a href="#核心功能">核心功能</a> •
  <a href="#技术栈">技术栈</a> •
  <a href="#架构设计">架构设计</a> •
  <a href="#快速开始">快速开始</a> •
  <a href="#api-overview">API 概览</a> •
  <a href="#文档">文档</a> •
  <a href="#测试">测试</a>
</p>

---

## 项目简介

AgentHub 是一个基于 **整洁架构（Clean Architecture）** 和 **TDD（测试驱动开发）** 流程构建的企业级 AI Agent 管理平台。它提供了完整的 AI Agent 生命周期管理能力，涵盖了从单个 Agent 的创建配置到多 Agent 团队协作的全场景需求。

平台集成了 **RAG 检索增强生成**、**多向量数据库**、**MCP 工具协议**、**工作流编排**等前沿技术，致力于为企业提供开箱即用的 AI Agent 基础设施。

---

## 核心功能

| 功能模块 | 描述 |
|---------|------|
| 🤖 **AI Agent 管理** | 创建、配置、启动、暂停、停止和监控 AI Agent 的完整生命周期 |
| 👥 **Agent Team 协作** | 支持多 Agent 编排为团队，多种协作模式（顺序、广播、路由等） |
| 📚 **RAG 检索增强生成** | 集成知识库管理、文档摄取（ETL）、向量化存储、混合检索流水线 |
| 🔧 **多功能工具集成** | 支持 HTTP 工具、MCP (Model Context Protocol) 工具、系统工具、自定义技能 |
| 🎯 **模型策略管理** | 支持多种 AI 模型（OpenAI、Ollama、阿里云等）配置与策略切换 |
| 🔄 **工作流管理** | 基于 DAG 图定义的工作流编排，支持发布/取消发布 |
| 🛡️ **护栏策略** | 输入/输出验证、PII 检测与脱敏、提示注入检测 |
| 📖 **知识库管理** | 文档上传、解析、清洗、分块、向量化、检索全文流水线 |
| 💬 **会话与记忆** | 聊天会话管理、短期/长期记忆存储、对话历史检索 |
| 🏢 **多租户与工作空间** | 多租户隔离、工作空间管理、权限角色体系（Owner/Admin/Developer/Viewer） |
| ⏰ **定时任务** | Cron 表达式驱动的定时 Agent 任务调度 |
| 🧩 **提示词模板** | 系统提示词、助手提示词模板管理 |
| 📊 **监控与审计** | Actuator 健康检查、指标监控、操作审计 |

---

## 技术栈

### 后端核心技术

| 类别 | 技术选型 |
|------|---------|
| **架构模式** | 整洁架构（Clean Architecture）四层架构 |
| **设计原则** | SOLID、依赖倒置、接口隔离 |
| **开发流程** | TDD（Red → Green → Refactor）+ ArchUnit 架构守护 |
| **语言** | Java 21 |
| **主框架** | Spring Boot 4.1.0-M2 |
| **AI 框架** | Spring AI 2.0.0-M4 |
| **Agent 框架** | Spring AI Alibaba Agent Framework、AgentScope |
| **ORM** | MyBatis-Plus 3.5.16（Spring Boot 4.x 适配） |
| **服务调用** | OpenFeign 13.6 |
| **安全** | Spring Security Crypto、JWT (jjwt 0.12.6) |
| **数据库** | PostgreSQL 14+ |
| **缓存** | Redis 7+ |
| **消息队列** | Kafka |
| **对象存储** | MinIO |
| **搜索引擎** | Elasticsearch |
| **向量数据库** | Qdrant、Milvus、Weaviate、Chroma、PGVector |
| **构建工具** | Gradle |
| **测试** | JUnit 6、ArchUnit 1.3 |
| **工具库** | Hutool 5.8.31、Jsoup、Jackson、Lombok |

### 前端技术

| 类别 | 技术选型 |
|------|---------|
| **框架** | Vue 3.5 |
| **路由** | Vue Router 4.5 |
| **状态管理** | Pinia 2.3 |
| **构建** | Vite |
| **语言** | TypeScript |

---

## 架构设计

### 四层整洁架构

```
┌─────────────────────────────────────────────────────────┐
│                     api (接口适配层)                      │
│    Controller · Request/Response DTO · Mapper · Exception │
│                     ↑ 依赖方向 ↓                         │
├─────────────────────────────────────────────────────────┤
│                 application (应用层)                      │
│    UseCase · Command · Service · Port(In/Out) · Executor │
│                     ↑ 依赖方向 ↓                         │
├─────────────────────────────────────────────────────────┤
│                  domain (领域层)                          │
│    Model · Event · Exception · 核心业务规则               │
│                     ↑ 依赖方向 ↓                         │
├─────────────────────────────────────────────────────────┤
│              infrastructure (基础设施层)                   │
│    agents · auth · etl · rag · store · tools · vector    │
│    (实现 application/port 定义的接口)                     │
└─────────────────────────────────────────────────────────┘
```

### 分层依赖规则（ArchUnit 强制校验）

```
api → application → domain ← infrastructure
```

| 规则 | 说明 |
|------|------|
| `api` → `application` | API 层可调用应用层的 UseCase |
| `application` → `domain` | 应用层依赖领域层核心模型 |
| `infrastructure` → `domain` | 基础设施层实现领域层定义的端口接口 |
| `infrastructure` → `application` | 基础设施层可实现应用层的 Port 接口 |
| ❌ `domain` 依赖外部框架 | 领域层禁止依赖 Spring/JPA/HTTP/Kafka 等外部框架 |
| ❌ 跨层直接依赖 | 禁止 `application` 直接依赖 `infrastructure` 或 `api` |

### 请求处理流程

```
Client → Controller → UseCase → Port(Interface)
                                     ↓
                              Infrastructure Implementation
                                     ↓
                                 Database / AI / External
```

### 命名约定

| 类型 | 命名规则 | 位置 |
|------|---------|------|
| Controller | `*Controller` | `api/controller/` |
| UseCase | `*UseCase` | `application/usecase/` |
| Repository 接口 | `*Repository` | `application/port/out/repositories/` |
| Repository 实现 | `Mybatis*Repository` | `infrastructure/store/db/repository/` |
| Entity | `*Entity` | `infrastructure/store/db/entity/` |
| Mapper | `*Mapper` | `infrastructure/store/db/mapper/` |
| Request DTO | `*Request` | `api/dto/` |
| Response DTO | `*Response` | `api/dto/` |
| 领域模型 | 领域名词 | `domain/model/` |
| 命令对象 | `*Command` | `application/command/` |
| 端口接口 | `*Port` | `application/port/` |

> 更详细的架构文档请参考 [架构设计文档](docs/architecture-design.md)

---

## 快速开始

### 环境要求

| 依赖 | 版本要求 | 说明 |
|------|---------|------|
| JDK | 21+ | 必需 |
| PostgreSQL | 14+ | 主数据库 |
| Redis | 7+ | 缓存/会话存储 |
| Qdrant | 1.7+ | 向量数据库（可选，可切换其他） |
| MinIO | 最新版 | 对象存储（可选） |
| Kafka | 最新版 | 消息队列（可选） |
| Elasticsearch | 8.x | 全文检索（可选） |

### 安装步骤

#### 1. 克隆项目

```bash
git clone https://github.com/HuangDayu/AgentHub.git
cd AgentHub
```

#### 2. 配置数据库

```bash
# 创建数据库
psql -U postgres -c "CREATE DATABASE agenthub;"

# 执行数据库脚本（应用启动时会自动执行）
psql -U postgres -d agenthub -f sql/schema.sql
psql -U postgres -d agenthub -f sql/init.sql
```

#### 3. 配置应用

编辑 `src/main/resources/application.yml`，配置以下关键项：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/agenthub?schema=app
    username: your_username
    password: your_password
  data:
    redis:
      host: localhost
      port: 6379
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      base-url: https://api.openai.com
    vectorstore:
      qdrant:
        host: localhost
        port: 6334
```

#### 4. 构建并运行

```bash
# 构建项目
gradle clean build

# 运行应用
gradle bootRun
```

应用将在 `http://localhost:8080` 启动。

前端页面在 `http://localhost:5173`（Vite 开发服务器）或通过后端静态资源访问。

> 更详细的部署说明请参考 [部署指南](docs/deployment-guide.md)

---

## API 概览

AgentHub 提供 RESTful API，统一前缀为 `/api/v1`，按功能模块划分：

| 模块 | 基础路径 | 说明 |
|------|---------|------|
| Agent 管理 | `/api/v1/workspaces/{wsId}/agents` | Agent 的 CRUD 与生命周期管理 |
| Agent 配置 | `/api/v1/workspaces/{wsId}/agents/{id}/configs` | Agent 的配置管理（按分类） |
| Agent 团队 | `/api/v1/workspaces/{wsId}/teams` | 多 Agent 团队管理 |
| 会话 | `/api/v1/workspaces/{wsId}/agents/{id}/sessions` | 会话创建、消息发送（含 SSE 流式） |
| 知识库 | `/api/v1/workspaces/{wsId}/knowledge-bases` | 知识库 CRUD、文档管理、检索 |
| 工作流 | `/api/v1/workspaces/{wsId}/dag-workflows` | 工作流定义与编排 |
| 模型配置 | `/api/v1/workspaces/{wsId}/model-configs` | AI 模型配置管理 |
| 记忆 | `/api/v1/workspaces/{wsId}/agents/{id}/memories` | Agent 记忆管理 |
| 向量存储 | `/api/v1/workspaces/{wsId}/vector-store-configs` | 向量数据库配置 |
| MCP 工具 | `/api/v1/workspaces/{wsId}/mcp-tools` | MCP 协议工具管理 |
| 技能 | `/api/v1/workspaces/{wsId}/skills` | Agent 技能管理 |
| 策略 | `/api/v1/workspaces/{wsId}/strategies` | 多种策略配置（检索、工具、模型、护栏） |
| 提示词 | `/api/v1/workspaces/{wsId}/prompt-templates` | 提示词模板管理 |
| 系统工具 | `/api/v1/workspaces/{wsId}/system-tools` | 系统内置工具 |
| 定时任务 | `/api/v1/workspaces/{wsId}/scheduled-tasks` | Cron 定时任务管理 |
| 认证 | `/api/v1/auth` | 登录、Token 刷新 |
| 租户 | `/api/v1/tenants` | 多租户管理 |
| 工作空间 | `/api/v1/workspaces` | 工作空间管理 |

> 完整的 API 端点参考请查阅 [API 参考文档](docs/api-reference.md)

---

## 文档

### 用户文档

| 文档 | 说明 |
|------|------|
| [架构设计文档](docs/2026-05-16-architecture-design.md) | 系统架构、分层设计、组件交互 |
| [API 参考文档](docs/2026-05-16-api-reference.md) | 完整 REST API 端点参考 |
| [部署指南](docs/2026-05-16-deployment-guide.md) | 环境配置与部署步骤 |
| [配置参考](docs/2026-05-16-configuration-reference.md) | 配置属性完整参考 |
| [开发指南](docs/2026-05-16-development-guide.md) | 开发者环境搭建与规范 |
| [数据模型](docs/2026-05-16-data-model.md) | 领域模型与数据实体定义 |

---

## 测试

项目采用 **TDD（测试驱动开发）** 流程，包含完整的单元测试、集成测试和架构测试。

```bash
# 运行所有测试
gradle test

# 运行架构测试
gradle test --tests "*ArchTest*"

# 运行特定模块测试
gradle test --tests "*AgentConfigControllerIntegrationTest*"
```

### 测试覆盖要求

- Controller 集成测试覆盖率达到 100%
- ArchUnit 架构规则约束所有分层依赖
- Gradle Test 确保全部通过方可提交

---

## 项目结构总览

```
AgentHub/
├── src/
│   ├── main/
│   │   ├── java/com/agenthub/          # 后端源代码
│   │   │   ├── api/                     # API层（Controller、DTO、Mapper、Exception）
│   │   │   │   ├── controller/          # 22个 REST 控制器
│   │   │   │   ├── dto/                 # 93个请求/响应 DTO
│   │   │   │   ├── mapper/              # DTO映射器
│   │   │   │   └── exception/           # API异常统一处理
│   │   │   ├── application/             # 应用层（UseCase、Command、Service、Port）
│   │   │   │   ├── usecase/             # 33个用例
│   │   │   │   ├── command/             # 命令对象
│   │   │   │   ├── dto/                 # 应用层DTO
│   │   │   │   ├── executor/            # 执行器
│   │   │   │   ├── service/             # 应用服务
│   │   │   │   └── port/                # 输入/输出端口接口
│   │   │   ├── domain/                  # 领域层（Model、Event、Exception）
│   │   │   │   ├── model/               # 57个领域模型
│   │   │   │   ├── event/               # 领域事件
│   │   │   │   └── exception/           # 17个领域异常
│   │   │   ├── infrastructure/          # 基础设施层
│   │   │   │   ├── agents/              # Agent实现（含Spring AI Alibaba）
│   │   │   │   ├── auth/                # JWT认证、凭证验证
│   │   │   │   ├── context/             # 上下文管理
│   │   │   │   ├── etl/                 # ETL管道（解析→清洗→分块→向量化）
│   │   │   │   ├── rag/                 # RAG流水线（查询改写→双路检索→重排）
│   │   │   │   ├── store/               # 数据存储（DB/缓存/文件）
│   │   │   │   ├── tools/               # 工具实现（HTTP/MCP/技能/系统）
│   │   │   │   ├── vector/              # 向量存储实现
│   │   │   │   ├── web/                 # Web相关
│   │   │   │   └── wiki/                # Wiki相关
│   │   │   └── common/                  # 公共组件
│   │   ├── web/                         # Vue 3 前端
│   │   │   └── src/
│   │   │       ├── api/                 # 28个API服务
│   │   │       ├── components/          # 18个通用组件
│   │   │       ├── views/               # 20个视图页面
│   │   │       ├── router/              # 路由配置
│   │   │       ├── store/               # Pinia状态管理
│   │   │       └── types/               # TypeScript类型定义
│   │   └── resources/                   # 配置文件
│   └── test/                            # 测试代码
│       └── java/com/agenthub/test/
│           ├── integration/             # 22个集成测试
│           └── architecture/            # 架构测试
├── docs/                                # 文档
├── sql/                                 # 数据库脚本
├── build.gradle                         # Gradle构建配置
└── settings.gradle                      # Gradle设置
```

---

## 开发规范

| 规范 | 要求 |
|------|------|
| **开发模式** | TDD - 测试驱动开发 |
| **架构模式** | 整洁架构（Clean Architecture） |
| **设计思想** | SOLID 原则 |
| **编码规范** | Alibaba P3C 规范 |
| **代码风格** | 提倡 Lombok，禁止使用 record 类 |
| **方法长度** | 每个方法不超过 10 行 |
| **方法注释** | 每个方法必须有 Javadoc 注释 |
| **参数数量** | 方法参数不超过 3 个 |
| **测试覆盖** | Controller 集成测试 100% 覆盖 |
| **架构守护** | ArchUnit 规则自动校验 |
| **构建验证** | Gradle Test 全部通过 |

---

## 贡献指南

欢迎贡献代码！请遵循以下流程：

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/amazing-feature`)
3. 编写测试并确保通过
4. 提交代码 (`git commit -m 'feat: add some amazing feature'`)
5. 推送到分支 (`git push origin feature/amazing-feature`)
6. 创建 Pull Request

请确保您的代码符合 TDD 流程并遵守整洁架构的分层规则。

---

## 许可证

[MIT](https://opensource.org/licenses/MIT)

---

<div align="center">

**⭐ 如果这个项目对你有帮助，请给一个 Star ⭐**

[报告 Bug](https://github.com/HuangDayu/AgentHub/issues) · [请求功能](https://github.com/HuangDayu/AgentHub/issues)

</div>
