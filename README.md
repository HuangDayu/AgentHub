# AgentHub

<div align="center">

**企业级 AI Agent 管理平台**

一个用于管理 AI Agent 和 AI Agent Team 全生命周期的智能化平台

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0--M2-brightgreen)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-2.0.0--M4-blue)](https://spring.io/projects/spring-ai)
[![Java](https://img.shields.io/badge/Java-21-orange)](https://openjdk.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

</div>

---

## 📋 项目简介

AgentHub 是一个基于整洁架构和 TDD 开发流程构建的企业级 AI Agent 管理平台。它提供了完整的 AI Agent 生命周期管理能力，包括创建、配置、运行、监控和协作等功能。

### 核心功能

- 🤖 **AI Agent 管理**：创建、配置、运行和监控 AI Agent
- 👥 **Agent Team 协作**：支持多 Agent 协作和团队管理
- 📚 **RAG（检索增强生成）**：集成向量数据库和知识库管理
- 🔧 **工具集成**：支持 HTTP 工具、MCP 工具、系统工具等
- 🎯 **模型配置**：支持多种 AI 模型配置和策略管理
- 🔄 **工作流管理**：支持复杂的工作流编排
- 📖 **知识库管理**：文档摄取、向量化、检索
- 💬 **会话管理**：聊天会话和记忆管理

---

## 🛠️ 技术栈

### 核心技术

| 类别 | 技术选型 |
|------|---------|
| **架构模式** | 整洁架构（Clean Architecture） |
| **设计原则** | SOLID |
| **开发流程** | TDD（Red → Green → Refactor） |
| **后端框架** | Spring Boot 4.1.0-M2 / Java 21 |
| **AI 框架** | Spring AI 2.0.0-M4 |
| **ORM** | MyBatis-Plus 3.5.16 |
| **服务间调用** | OpenFeign 13.6 |
| **向量数据库** | Qdrant、Milvus、Weaviate、Chroma、PGVector |
| **消息队列** | Kafka |
| **缓存** | Redis |
| **数据库** | PostgreSQL |
| **对象存储** | MinIO |
| **搜索引擎** | Elasticsearch |
| **前端框架** | Vue 3.5 + Vue Router 4.5 + Pinia 2.3 |
| **构建工具** | Gradle |
| **测试框架** | JUnit 6、ArchUnit |

### 主要依赖

- **Spring AI**：spring-ai-alibaba、spring-ai-mcp、spring-ai-ollama、spring-ai-openai、spring-ai-qdrant-store 等
- **安全**：Spring Security Crypto、JWT (jjwt)
- **工具类**：Hutool、Jsoup、Jackson
- **测试**：ArchUnit（架构测试）

### 开发规范

- **开发模式**： 集成测试驱动开发 (TDD)
- **架构模式**： 整洁架构 (Clean Architecture) 
- **设计思想**： 符合 SOLID 设计原则
- **编码规范**： Alibaba P3C 规范
- **其他要求**： 
  - 提倡使用lombok，禁止使用record类
  - 所有的方法必须不能超过10行
  - 每个方法必须有注释说明
  - 方法的参数不得超过3个
  - 禁止写伪代码，sleep等伪代码
  - Controller集成测试要覆盖100%
  - 不能违反ArchUnit架构规则校验
  - 必须用Gradle Test进行测试并确保全部通过

---

## 🏗️ 项目结构

```
AgentHub/
├── src/
│   ├── main/
│   │   ├── java/com/agenthub/          # 后端源代码
│   │   │   ├── api/                     # API层（Controller、DTO）
│   │   │   ├── application/             # 应用层（UseCase、Service、Port）
│   │   │   ├── domain/                  # 领域层（Model、Event）
│   │   │   ├── infrastructure/          # 基础设施层
│   │   │   │   ├── agents/              # Agent实现
│   │   │   │   ├── auth/                # 认证授权
│   │   │   │   ├── context/             # 上下文管理
│   │   │   │   ├── etl/                 # ETL管道
│   │   │   │   ├── rag/                 # RAG实现
│   │   │   │   ├── store/               # 数据存储
│   │   │   │   ├── tools/               # 工具实现
│   │   │   │   ├── vector/              # 向量存储
│   │   │   │   └── web/                 # Web相关
│   │   │   └── common/                  # 公共组件
│   │   ├── web/                         # 前端源代码（Vue 3）
│   │   └── resources/                   # 配置文件
│   └── test/                            # 测试代码
├── docs/                                # 文档
├── sql/                                 # 数据库脚本
│   ├── schema.sql                       # 数据库模式
│   ├── init.sql                         # 初始化数据
│   └── checklist.sql                    # 检查清单
├── build.gradle                         # Gradle配置
├── settings.gradle                      # Gradle设置
├── README.md                            # 项目说明
├── CLAUDE.md                            # Claude AI 指导文档
└── CLAUDE_ZH.md                         # Claude AI 指导文档（中文）
```

---

## 🏛️ 架构设计

### 分层定义

```
api/              → Controller、请求/响应 DTO、协议适配
application/      → Use Case、命令与查询编排、事务边界
domain/           → 核心业务规则、业务对象、不变式、领域事件
infrastructure/   → 数据库、缓存、消息、外部 API、模型 SDK 适配
```

### 领域模块标准结构

```
src/main/java/com/agenthub/
├── api/
│   ├── controller/         # REST控制器
│   ├── dto/                # 请求/响应DTO
│   ├── mapper/             # DTO映射器
│   └── exception/          # API异常处理
├── application/
│   ├── usecase/            # 用例
│   ├── command/            # 命令对象
│   ├── dto/                # 应用层DTO
│   ├── executor/           # 执行器
│   ├── service/            # 应用服务
│   └── port/               # 端口接口
│       ├── in/             # 输入端口
│       └── out/            # 输出端口
│           ├── agent/      # Agent端口
│           ├── etl/        # ETL端口
│           ├── rag/        # RAG端口
│           ├── repositories/ # 仓储端口
│           └── tools/      # 工具端口
├── domain/
│   ├── model/              # 领域模型
│   ├── event/              # 领域事件
│   └── exception/          # 领域异常
├── infrastructure/
│   ├── agents/             # Agent实现
│   │   └── alibaba/        # Spring-ai-alibaba Agent实现
│   ├── auth/               # 认证授权实现
│   ├── context/            # 上下文管理
│   ├── etl/                # ETL管道实现
│   ├── factory/            # 工厂类
│   ├── model/              # 模型配置实现
│   ├── rag/                # RAG实现
│   ├── store/              # 数据存储
│   │   ├── cache/          # 缓存实现
│   │   ├── db/             # 数据库实现
│   │   │   ├── entity/     # 数据库实体
│   │   │   ├── mapper/     # MyBatis Mapper
│   │   │   └── repository/ # 仓储实现
│   │   └── files/          # 文件存储
│   ├── tools/              # 工具实现
│   │   ├── http_tools/     # HTTP工具
│   │   ├── mcp_tools/      # MCP工具
│   │   ├── skills_tools/   # 技能工具
│   │   └── system_tools/   # 系统工具
│   ├── vector/             # 向量存储实现
│   ├── web/                # Web相关
│   └── wiki/               # Wiki相关
└── common/                 # 公共组件
    ├── annotations/        # 注解
    ├── constants/          # 常量
    └── utils/              # 工具类
```

### 分层依赖规则（强制）

```
api → application → domain ← infrastructure
```

**规则说明**：

1. `api` 可依赖 `application`
2. `application` 可依赖 `domain`
3. `infrastructure` 可依赖 `domain` 和 `application`（实现 Port）
4. **禁止**：`domain` 依赖 Spring/JPA/HTTP/Kafka/SDK 类型
5. **禁止**：跨层直接依赖（domain -> infrastructure）
6. **禁止**：`application` 依赖 `infrastructure` 或 `api`

### 命名约定

| 类型 | 命名规则 | 位置 |
|------|---------|------|
| Controller | `*Controller` | `api/controller/` |
| UseCase | `*UseCase` | `application/usecase/` |
| Repository接口 | `*Repository` | `application/port/out/repositories/` |
| Repository实现 | `Mybatis*Repository` | `infrastructure/store/db/repository/` |
| Entity | `*Entity` | `infrastructure/store/db/entity/` |
| Mapper | `*Mapper` | `infrastructure/store/db/mapper/` |
| Request DTO | `*Request` | `api/dto/` |
| Response DTO | `*Response` | `api/dto/` |
| 领域模型 | 领域名词 | `domain/model/` |
| 命令对象 | `*Command` | `application/command/` |
| 端口接口 | `*Port` | `application/port/` |

---

## 🚀 快速开始

### 环境要求

- JDK 21+
- Gradle 8.0+
- PostgreSQL 14+
- Redis 7+
- Qdrant 1.7+
- MinIO (可选)
- Kafka (可选)

### 安装步骤

1. **克隆项目**
```bash
git clone https://github.com/HuangDayu/AgentHub.git
cd AgentHub
```

2. **配置数据库**
```bash
# 创建数据库
psql -U postgres -c "CREATE DATABASE agenthub;"

# 执行数据库脚本
psql -U postgres -d agenthub -f sql/schema.sql
psql -U postgres -d agenthub -f sql/init.sql
```

3. **配置应用**
```bash
# 修改 src/main/resources/application.yml
# 配置数据库、Redis、Qdrant 等连接信息
```

4. **构建项目**
```bash
gradle clean build
```

5. **运行应用**
```bash
gradle bootRun
```

应用将在 `http://localhost:8080` 启动。

---

## 📖 文档

- [Agent 功能实现计划](docs/agent_function_implementation_plan.md)
- [Agent 运行时实现计划](docs/agent_runtime_implementation_plan.md)
- [前端美化指南](docs/frontend-beautification-guide.md)
- [Harness Engineering AI Agent 设计](docs/harness-engineering-ai-agent-design.md)
- [Spring AI ChatClient vs Agent 对比](docs/spring-ai-chatclient-vs-agent.md)
- [技术设计文档](docs/spring-ai-harness-engineering-agent-technical-design.md)

---

## 🧪 测试

项目采用 TDD 开发流程，包含完整的单元测试和架构测试。

```bash
# 运行所有测试
gradle test

# 运行架构测试
gradle test --tests "*ArchTest*"
```

---


<div align="center">

**⭐ 如果这个项目对你有帮助，请给一个 Star ⭐**

</div>
