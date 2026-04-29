# AgentHub

这个一个 AI Agent Hub 项目，用于管理 AI Agent 和 AI Agent Team 的全生命周期。


## 技术栈

| 基线项 | 技术选型 |
|-------|---------|
| 架构模式 | 整洁架构 |
| 设计原则 | SOLID |
| 开发流程 | TDD（Red → Green → Refactor） |
| 后端框架 | Spring Boot 4.0+ / Java 21 |
| AI 框架 | Spring AI |
| ORM | MyBatis-Plus（强制） |
| 服务间调用 | OpenFeign（强制） |
| 向量数据库 | Qdrant |
| 消息队列 | Kafka |
| 缓存 | Redis |
| 关系数据库 | PostgreSQL |
| 构建工具 | Gradle（Groovy DSL） |
| 前端框架 | Vue 3 |
| 编码规范 | 阿里巴巴 P3C（强制） |
| 方法行数 | 单方法不超过 10 行 |

---

## 强制性开发要求

- 开发模式采用集成测试驱动开发 (TDD)
- 项目架构是整洁架构，符合 SOLID 设计原则，不能违反ArchUnit架构规则校验
- 新增的Controller集成测试要覆盖100%
- 所有的方法必须不能超过10行
- 每次开发完成，都必须用Gradle Test进行测试并确保ArchUnit测试和集成测试都全部通过
- 所需要的外部组件和中间件通过WSL Docker部署，通常情况下已经搭建部署好了

---

## 整洁架构分层规范

### 分层定义

```
api/              → Controller、请求/响应 DTO、协议适配
application/      → Use Case、命令与查询编排、事务边界
domain/           → 核心业务规则、业务对象、不变式、领域事件
infrastructure/   → 数据库、缓存、消息、外部 API、模型 SDK 适配
```

### 领域模块标准结构

```
domain/xxx-domain/src/main/java/com/thingsknowledge/xxx/
├── api/
│   ├── controller/         # REST控制器
│   ├── dto/                # 请求/响应DTO
│   ├── mapper/             # DTO映射器
│   └── exception/          # API异常处理
├── application/
│   ├── usecase/            # 用例
│   ├── command/            # 命令对象
│   ├── query/              # 查询对象
│   ├── service/            # 应用服务
│   └── port/               # 端口接口
│       ├── in/             # 输入端口
│       └── out/            # 输出端口
│           ├── rag/        # RAG端口
│           └── repositories/ # 仓储端口
├── domain/
│   ├── model/              # 领域模型
│   ├── service/            # 领域服务
│   ├── event/              # 领域事件
│   ├── port/               # 领域端口
│   └── exception/          # 领域异常
└── infrastructure/
    ├── persistence/        # 持久化
    │   ├── entity/         # 数据库实体
    │   ├── mapper/         # MyBatis Mapper
    │   └── repository/     # 仓储实现
    ├── messaging/          # 消息适配
    ├── external/           # 外部API适配
    └── config/             # 配置
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
|------|----------|------|
| Controller | `*Controller` | `api/controller/` |
| UseCase | `*UseCase` | `application/usecase/` |
| Repository接口 | `*Repository` | `domain/` 或 `application/port/out/repositories/` |
| Repository实现 | `Mybatis*Repository` | `infrastructure/persistence/repository/` |
| Entity | `*Entity` | `infrastructure/persistence/entity/` |
| Mapper | `*Mapper` | `infrastructure/persistence/mapper/` |
| Request DTO | `*Request` | `api/dto/` |
| Response DTO | `*Response` | `api/dto/` |
| 领域模型 | 领域名词 | `domain/model/` |
| 命令对象 | `*Command` | `application/command/` |
| 查询对象 | `*Query` | `application/query/` |