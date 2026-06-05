# Architecture Rules & Conventions Reference

> 四层整洁架构的完整规则清单、命名约定、编码规范与测试模式。

---

## 1. 四层架构总览

### 1.1 层级定义

| 层 | 包路径 | 职责 | 可依赖 |
|----|--------|------|--------|
| **api** | `com.agenthub.api` | HTTP 适配、DTO 转换、异常处理 | application |
| **application** | `com.agenthub.application` | 用例编排、端口定义、事务 | domain |
| **domain** | `com.agenthub.domain` | 核心业务规则、领域模型 | Java 标准库 |
| **infrastructure** | `com.agenthub.infrastructure` | 实现端口接口、适配外部框架 | application/port + domain |

### 1.2 子包结构

```
api/
├── controller/     # *Controller — REST 端点
├── dto/            # *Request / *Response
├── mapper/         # DTO 映射器
└── exception/      # ApiExceptionHandler

application/
├── usecase/        # *UseCase — 用例编排
├── command/        # *Command — 命令对象
├── dto/            # *Output — 应用层输出
├── executor/       # 命令执行器
├── service/        # 跨用例服务
└── port/
    ├── in/         # 输入端口
    └── out/        # 输出端口（仓储/Agent/ETL/RAG）

domain/
├── model/          # 领域模型（聚合根/实体/值对象）
├── event/          # *Event — 领域事件
└── exception/      # *Exception — 领域异常

infrastructure/
├── agents/         # Agent 运行时
├── auth/           # JWT 认证
├── context/        # 多租户上下文
├── etl/            # 文档 ETL
├── rag/            # RAG 检索
├── store/db/       # 数据存储（entity/mapper/repository）
├── tools/          # 工具实现
├── vector/         # 向量存储
├── web/            # Web 配置
└── wiki/           # Wiki 集成
```

---

## 2. 命名约定表

| 类型 | Pattern | 包路径 |
|------|---------|--------|
| Controller | `*Controller` | `api.controller` |
| UseCase | `*UseCase` | `application.usecase` |
| Repository 接口 | `*Repository` | `application.port.out.repositories` |
| Repository 实现 | `Mybatis*Repository` | `infrastructure.store.db.repository` |
| MyBatis Entity | `*Entity` | `infrastructure.store.db.entity` |
| MyBatis Mapper | `*Mapper` | `infrastructure.store.db.mapper` |
| Request DTO | `*Request` | `api.dto` |
| Response DTO | `*Response` | `api.dto` |
| Application DTO | `*Output` | `application.dto` |
| Command | `*Command` | `application.command` |
| Domain Model | 领域名词 | `domain.model` |
| Domain Event | `*Event` | `domain.event` |
| Domain Exception | `*Exception` | `domain.exception` |
| Port 接口 | `*Port` / `*Repository` | `application.port` |

---

## 3. 17 条 ArchUnit 规则清单

### 3.1 架构依赖规则（6 条）

| # | 测试方法 | 规则 |
|---|---------|------|
| 1 | `domain_should_not_depend_on_outer_layers` | domain 包禁依赖 application/infrastructure/api |
| 2 | `application_should_not_depend_on_infrastructure_or_api` | application 包禁依赖 infrastructure/api |
| 3 | `api_should_not_depend_on_infrastructure` | api 包禁依赖 infrastructure |
| 4 | `infrastructure_should_not_depend_on_api` | infrastructure 包禁依赖 api |
| 5 | `api_should_not_directly_depend_on_domain` | api.controller 禁直接依赖 domain |
| 6 | `domain_models_should_be_pojos` | domain.model 禁依赖 Spring/JPA/Kafka |

### 3.2 命名与位置规则（9 条）

| # | 测试方法 | 规则 |
|---|---------|------|
| 7 | `application_use_case_naming_convention` | usecase 类以 `UseCase` 结尾 |
| 8 | `repository_interfaces_should_be_in_application` | `*Repository` 接口在 `application.port.out` |
| 9 | `repository_implementations_should_be_in_infrastructure` | `Mybatis*Repository` 在 `infrastructure.store.db.repository` |
| 10 | `controller_naming_convention` | controller 类以 `Controller` 结尾 |
| 11 | `dtos_should_be_in_api_dto_package` | `*Request`/`*Response`/`*DTO` 在 `api.dto` 或 `infrastructure..dto` |
| 12 | `domain_events_should_be_in_domain_event_package` | `*Event` 在 `domain.event` |
| 13 | `domain_use_case_should_only_depend_on_domain` | domain.usecase 仅依赖 domain |
| 14 | `no_cycles_in_application_layer` | 应用层无循环依赖 |
| 15 | `no_cycles_in_domain_layer` | 领域层无循环依赖 |

### 3.3 方法复杂度规则（2 条）

| # | 测试方法 | 规则 |
|---|---------|------|
| 16 | `methods_should_not_exceed_ten_lines` | 业务方法 ≤10 行（不含空行/注释） |
| 17 | `methods_should_not_have_more_than_three_parameters` | 业务方法 ≤3 参数 |

排除方法：getter/setter、toString/equals/hashCode、toXxx/fromXxx 转换、Lombok 生成、枚举方法。

---

## 4. 类注解模式

```java
// ✅ Controller
@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/xxx")
@RequiredArgsConstructor
public class XxxController { }

// ✅ UseCase
@Component
@RequiredArgsConstructor
public class XxxUseCase { }

// ✅ Domain Model — 零框架依赖
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Xxx { }

// ✅ Infrastructure Repository
@Component
@Primary
public class MybatisXxxRepository implements XxxRepository { }

// ✅ MyBatis Entity
@Data
@TableName("xxx")
public class XxxEntity { }

// ✅ MyBatis Mapper
@Mapper
public interface XxxMapper extends BaseMapper<XxxEntity> { }
```

---

## 5. 编码禁止项

| 禁止 | 替代 |
|------|------|
| ❌ `record` 类 | `@Data` + `@NoArgsConstructor` + `@AllArgsConstructor` |
| ❌ `@Builder` | 普通构造 + setter |
| ❌ `@Autowired` 字段注入 | `@RequiredArgsConstructor` + `private final` |
| ❌ 通配符 import | 显式 import（`domain.exception.*` 除外） |
| ❌ 方法 >10 行 | 拆分为多个 ≤10 行的方法 |
| ❌ 方法 >3 参数 | 创建 `*Command` 封装 |

---

## 6. DTO 转换链

```
Request (api.dto) → Command (application.command)
    → Domain Model (domain.model)
    → Output (application.dto) → Response (api.dto)
```

统一使用 `BeanUtil.copyProperties()` 进行对象转换。

---

## 7. 集成测试模板

```java
package com.agenthub.test.integration;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class XxxControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private static final String BASE_URL = "/api/v1/workspaces/test-ws/xxx";

    @Test
    @Order(1)
    void shouldCreateXxx() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Tenant-Id", "test-tenant");
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = """
            {"name": "test-xxx"}
            """;

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                BASE_URL, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }
}
```

---

## 8. 常用命令

```bash
# 跑所有测试
gradle test

# 只跑架构测试
gradle test --tests "com.agenthub.test.architecture.AgentHubCleanArchitectureTest"

# 只跑特定集成测试
gradle test --tests "*XxxControllerIntegrationTest*"

# 只跑单个测试方法
gradle test --tests "*XxxControllerIntegrationTest#shouldCreateXxx*"

# 启动应用
gradle bootRun
```

---

## 9. 方案文档模板

每个功能开发前必须创建方案文档，保存在 `docs/` 目录，命名格式：`YYYY-MM-DD-feature-name-plan.md`。

```markdown
# [功能名称] 实现方案

> 日期：YYYY-MM-DD · 状态：待审核

## 1. 需求概述

[一句话描述功能 + 关键输入输出]

## 2. 架构设计

| 层 | 文件 | 操作 |
|----|------|------|
| domain | Xxx.java | 新建 |
| application/port | XxxRepository.java | 新建 |
| application/usecase | XxxUseCase.java | 新建 |
| application/command | XxxCommand.java | 新建 |
| application/dto | XxxOutput.java | 新建 |
| infrastructure/entity | XxxEntity.java | 新建 |
| infrastructure/mapper | XxxMapper.java | 新建 |
| infrastructure/repository | MybatisXxxRepository.java | 新建 |
| api/dto | XxxRequest.java / XxxResponse.java | 新建 |
| api/controller | XxxController.java | 修改 |
| test | XxxControllerIntegrationTest.java | 新建 |

## 3. API 设计

- Method: GET
- Path: /api/v1/workspaces/{workspaceId}/xxx/{id}
- Request: 无
- Response: { "id": "...", "name": "..." }

## 4. 边界情况

- [ ] 资源不存在 → 404 NotFoundException
- [ ] 参数非法 → 400 ValidationException
- [ ] 权限不足 → 401

## 5. 检查清单

- [ ] 所有方法 ≤10 行、≤3 参数
- [ ] 分层依赖正确
- [ ] 命名符合约定
- [ ] 禁止项无一违反

## 完成情况

- ArchUnit: 17/17 ✅
- 集成测试: N/N ✅
- 创建文件: X 个
- 修改文件: Y 个

## 反思

- 遇到的问题：
- 解决方案：
- 改进建议：
```
