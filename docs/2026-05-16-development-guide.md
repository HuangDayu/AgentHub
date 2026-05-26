# 开发指南

> 本文档帮助开发者快速上手 AgentHub 项目开发，涵盖环境搭建、编码规范、测试流程等。

---

## 目录

- [开发环境搭建](#1-开发环境搭建)
- [项目结构](#2-项目结构)
- [编码规范](#3-编码规范)
- [测试规范](#4-测试规范)
- [开发工作流](#5-开发工作流)
- [IDE 配置](#6-ide-配置)
- [常见任务指南](#7-常见任务指南)

---

## 1. 开发环境搭建

### 1.1 前置条件

| 工具 | 版本 | 用途 |
|------|------|------|
| JDK | 21+ | 编译运行 |
| Gradle | 8.0+ | 构建工具（可使用 wrapper） |
| Node.js | 18+ | 前端开发 |
| npm | 9+ | 前端包管理 |
| Docker | 最新 | 运行基础设施（推荐） |
| IntelliJ IDEA | 2024+ | 推荐 IDE |

### 1.2 快速开始

```bash
# 1. 克隆项目
git clone https://github.com/HuangDayu/AgentHub.git
cd AgentHub

# 2. 启动基础设施（Docker）
docker run -d --name postgres -e POSTGRES_USER=things -e POSTGRES_PASSWORD=things123 -e POSTGRES_DB=agenthub -p 5432:5432 postgres:16
docker run -d --name redis -p 6379:6379 redis:7
docker run -d --name qdrant -p 6333:6333 -p 6334:6334 qdrant/qdrant

# 3. 构建项目
./gradlew clean build

# 4. 运行测试
./gradlew test

# 5. 启动应用
./gradlew bootRun
```

### 1.3 前端开发环境

```bash
# 进入前端目录
cd src/main/web

# 安装依赖
npm install

# 启动开发服务器（热重载）
npm run dev
```

---

## 2. 项目结构

### 2.1 代码分层

```
src/main/java/com/agenthub/
├── api/                    # 接口适配层
│   ├── controller/         # REST 控制器
│   ├── dto/                # 请求/响应 DTO
│   ├── mapper/             # DTO 映射器
│   └── exception/          # 异常处理
├── application/            # 应用层
│   ├── usecase/            # 用例实现
│   ├── command/            # 命令对象
│   ├── dto/                # 应用层 DTO
│   ├── executor/           # 执行器
│   ├── service/            # 应用服务
│   └── port/               # 端口接口
│       ├── in/             # 输入端口
│       └── out/            # 输出端口
├── domain/                 # 领域层
│   ├── model/              # 领域模型
│   ├── event/              # 领域事件
│   └── exception/          # 领域异常
├── infrastructure/         # 基础设施层
│   ├── agents/             # Agent 运行时
│   ├── auth/               # 认证授权
│   ├── context/            # 上下文管理
│   ├── etl/                # ETL 管道
│   ├── rag/                # RAG 流水线
│   ├── store/              # 数据存储
│   ├── tools/              # 工具实现
│   ├── vector/             # 向量存储
│   ├── web/                # Web 配置
│   └── wiki/               # Wiki 集成
└── common/                 # 公共组件
    ├── annotations/        # 自定义注解
    ├── constants/          # 常量
    └── utils/              # 工具类
```

### 2.2 模块内标准结构

每个功能模块（如 Agent、知识库、工作流等）遵循如下结构：

```
模块/
├── api/
│   ├── controller/ModuleController.java      # REST 接口
│   ├── dto/ModuleRequest.java               # 请求 DTO
│   ├── dto/ModuleResponse.java              # 响应 DTO
│   └── mapper/ModuleResponseMapper.java     # DTO 映射
├── application/
│   ├── usecase/ModuleUseCase.java           # 用例实现
│   ├── command/ModuleCommand.java           # 命令对象
│   ├── dto/ModuleOutput.java               # 应用 DTO
│   └── port/out/repositories/              # 仓储接口
├── domain/
│   └── model/Module.java                    # 领域模型
└── infrastructure/
    └── store/db/
        ├── entity/ModuleEntity.java         # 数据库实体
        ├── mapper/ModuleMapper.java         # MyBatis Mapper
        └── repository/MybatisModuleRepository.java  # 仓储实现
```

---

## 3. 编码规范

### 3.1 总体要求

| 规范项 | 要求 |
|-------|------|
| 架构模式 | 整洁架构（Clean Architecture） |
| 设计原则 | SOLID |
| 开发流程 | TDD（Red → Green → Refactor） |
| 编码风格 | Alibaba P3C 规范 |
| 注解工具 | 提倡 Lombok，禁止使用 record 类 |
| 方法长度 | 不超过 10 行 |
| 方法注释 | 必须有 Javadoc 注释 |
| 参数数量 | 不超过 3 个 |
| 禁止项 | 禁止写伪代码、Thread.sleep() 等 |

### 3.2 命名规范

| 类型 | 命名规则 | 示例 |
|------|---------|------|
| Controller | `*Controller` | `AgentController` |
| UseCase | `*UseCase` | `AgentUseCase` |
| Repository 接口 | `*Repository` | `AgentRepository` |
| Repository 实现 | `Mybatis*Repository` | `MybatisAgentRepository` |
| Entity | `*Entity` | `AgentEntity` |
| Mapper | `*Mapper` | `AgentMapper` |
| Request DTO | `*Request` | `CreateAgentRequest` |
| Response DTO | `*Response` | `AgentResponse` |
| Command | `*Command` | `CreateAgentCommand` |
| Port | `*Port` | `AgentPort` |

### 3.3 架构约束（ArchUnit）

ArchUnit 规则在测试阶段自动校验以下约束：

```java
// API 层只能依赖 Application 层
layeredArchitecture()
    .layer("API").definedBy("..api..")
    .layer("APPLICATION").definedBy("..application..")
    .whereLayer("API").mayOnlyBeAccessedByLayers("API")

// Domain 层不能依赖外部框架
noClasses()
    .that().resideInAPackage("..domain..")
    .should().dependOnClassesThat().resideInAnyPackage(
        "..spring..", "..jakarta..", "..kafka.."
    )

// Application 层不能依赖 Infrastructure 层
noClasses()
    .that().resideInAPackage("..application..")
    .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
```

---

## 4. 测试规范

### 4.1 测试分类

| 测试类型 | 说明 | 框架 |
|---------|------|------|
| 单元测试 | 测试领域模型和工具类 | JUnit 6 |
| 集成测试 | 测试 Controller 完整请求链路 | Spring Boot Test |
| 架构测试 | 校验分层依赖和命名约定 | ArchUnit |

### 4.2 测试要求

1. **Controller 集成测试必须覆盖 100%**
2. **所有测试必须通过 Gradle Test**
3. **遵循 TDD 流程：先写测试，再写实现**
4. **测试方法命名清晰，表明测试场景**

### 4.3 运行测试

```bash
# 运行所有测试
./gradlew test

# 运行架构测试
./gradlew test --tests "*ArchTest*"

# 运行特定集成测试
./gradlew test --tests "*AgentConfigControllerIntegrationTest*"

# 运行测试并生成报告
./gradlew test jacocoTestReport
```

### 4.4 集成测试示例

```java
@SpringBootTest
@AutoConfigureMockMvc
class AgentConfigControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateConfig() throws Exception {
        mockMvc.perform(post("/api/v1/workspaces/{wsId}/agents/{id}/configs")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isCreated());
    }
}
```

---

## 5. 开发工作流

### 5.1 TDD 流程

```
1. Red:   编写失败的测试用例
2. Green: 编写最小实现代码使测试通过
3. Refactor: 重构代码，确保测试依然通过
```

### 5.2 添加新功能步骤

1. **理解需求**：明确功能范围和边界
2. **定义领域模型**：在 `domain/model/` 中创建或修改领域模型
3. **定义端口接口**：在 `application/port/` 中定义输入/输出端口
4. **实现用例**：在 `application/usecase/` 中创建 UseCase
5. **编写仓储实现**：在 `infrastructure/store/` 中实现仓储接口
6. **创建 API 端点**：在 `api/controller/` 中添加 REST 接口
7. **编写集成测试**：验证完整请求链路
8. **编写前端页面**（如需）：在 `src/main/web/` 中添加视图

### 5.3 代码审查清单

- [ ] 遵循整洁架构分层规则
- [ ] 方法不超过 10 行
- [ ] 方法有 Javadoc 注释
- [ ] 参数不超过 3 个
- [ ] 使用了 Lombok 注解
- [ ] 集成测试已覆盖
- [ ] ArchUnit 规则未违反
- [ ] Gradle Test 全部通过

---

## 6. IDE 配置

### 6.1 IntelliJ IDEA 配置

**推荐插件**：
- Lombok
- MyBatisX
- ArchUnit
- Alibaba Java Coding Guidelines
- Gradle

**代码风格**：

1. 导入 Alibaba P3C 代码风格模板
2. 设置文件编码为 UTF-8
3. 启用注解处理（Lombok）

### 6.2 VS Code 配置

推荐扩展：
- Extension Pack for Java
- Lombok Annotations Support
- Gradle Extension Pack
- Vue Language Features (Volar)

---

## 7. 常见任务指南

### 7.1 添加新的领域模型

1. 在 `domain/model/` 中创建模型类
2. 在 `application/command/` 中创建对应 Command
3. 在 `application/dto/` 中创建应用层 DTO
4. 在 `infrastructure/store/db/entity/` 中创建 Entity
5. 在 `infrastructure/store/db/mapper/` 中创建 Mapper
6. 在 `application/port/out/repositories/` 中创建 Repository 接口
7. 在 `infrastructure/store/db/repository/` 中创建 MyBatis 实现

### 7.2 添加新的 API 端点

1. 在 `api/controller/` 中添加（或修改）Controller
2. 在 `api/dto/` 中创建请求/响应 DTO
3. 在 `api/mapper/` 中添加 DTO 映射
4. 编写集成测试
5. 自动生成前端 API 调用

### 7.3 添加新的策略类型

1. 在 `domain/model/` 中定义策略模型
2. 在 `application/port/out/repositories/` 中定义仓储接口
3. 实现存储和业务逻辑
4. 在 `api/controller/` 中创建管理端点
