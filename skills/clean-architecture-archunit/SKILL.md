---
name: clean-architecture-archunit
description: AgentHub 完整 TDD 开发流程。实现任何功能必须遵循：理解架构 → 理解需求 → 制定方案保存到docs → 编写Controller集成测试(Red) → 按方案开发(Green) → 代码自检 → ArchUnit校验 → 集成测试通过 → 总结反思 → 等待审核迭代。Use when creating features, adding APIs, fixing bugs, or any backend code changes.
---

# AgentHub TDD 开发流程

**核心原则：先理解 → 先计划 → 先写测试 → 按层开发 → 自检 → 双重验证 → 总结反思 → 等审核。**

---

## 第 1 步：理解架构

在写任何代码前，确认你理解了四层依赖方向：

```
api (Controller/DTO) → application (UseCase/Port) → domain (Model)
                                                          ↑
infrastructure (agents/auth/store/etl/rag/tools) ────────┘
```

核心约束速查：

| 规则 | 含义 |
|------|------|
| `domain` 禁依赖 Spring/JPA/Kafka | 纯 POJO |
| `application` 禁依赖 `infrastructure` | 通过 Port 反转 |
| `api.controller` 禁直接依赖 `domain` | 经 UseCase |
| 方法 ≤10 行、≤3 参数 | ArchUnit 强制执行 |
| 禁止 `record`/`@Builder`/`@Autowired` | 项目铁律 |

命名和位置见 [REFERENCE.md](REFERENCE.md)。

---

## 第 2 步：理解需求

仔细分析用户的需求，明确：

- **功能边界**：做什么、不做什么
- **输入输出**：API 路径、请求参数、响应格式
- **涉及层级**：需要新建哪些文件？修改哪些已有文件？
- **依赖关系**：是否依赖已有 Repository/Service？是否需要新建表？
- **边界情况**：空结果、权限不足、参数校验失败等

需求不清晰时，**必须先向用户提问澄清**，不要假设。

---

## 第 3 步：制定方案，保存到 docs

**在写任何代码之前**，将实现方案写入 `docs/` 目录，命名格式：`YYYY-MM-DD-feature-name-plan.md`。

方案文档必须包含：

```markdown
# [功能名称] 实现方案

> 日期：YYYY-MM-DD · 状态：待审核

## 1. 需求概述

[一句话描述 + 关键输入输出]

## 2. 架构设计

| 层 | 文件 | 操作 |
|----|------|------|
| domain | Xxx.java | 新建 |
| application/port | XxxRepository.java | 新建 |
| application/usecase | XxxUseCase.java | 新建 |
| infrastructure | MybatisXxxRepository.java | 新建 |
| api/controller | XxxController.java | 修改 |
| test | XxxIntegrationTest.java | 新建 |

## 3. API 设计

- Method: GET/POST/PUT/DELETE
- Path: /api/v1/workspaces/{wsId}/xxx
- Request/Response 示例

## 4. 边界情况

- [ ] 空数据 → 404
- [ ] 参数非法 → 400
- [ ] ...

## 5. 检查清单

- [ ] 所有方法 ≤10 行、≤3 参数
- [ ] 分层依赖正确
- [ ] 命名符合约定
```

---

## 第 4 步：编写 Controller 集成测试（Red）

**按方案文档中的 API 设计**，先创建集成测试。

文件位置：`src/test/java/com/agenthub/test/integration/`

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class XxxControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @Order(1)
    void shouldCreateXxx() { /* Given/When/Then */ }

    @Test
    @Order(2)
    void shouldReturn404ForMissing() { /* Given/When/Then */ }
}
```

运行确认失败：
```bash
gradle test --tests "*XxxControllerIntegrationTest*"
```

---

## 第 5 步：按方案执行代码开发（Green）

**严格按第 3 步的方案文档 + 以下顺序创建文件，不可跨层直调：**

```
5.1 domain/model/Xxx.java                ← 纯 POJO，零框架依赖
5.2 application/command/XxxCommand.java   ← 命令对象（如需）
5.3 application/dto/XxxOutput.java        ← 应用层输出
5.4 application/port/out/.../XxxRepository.java  ← 端口接口
5.5 application/usecase/XxxUseCase.java   ← 用例编排（≤10行，≤3参数）
5.6 infrastructure/store/db/entity/XxxEntity.java
5.7 infrastructure/store/db/mapper/XxxMapper.java
5.8 infrastructure/store/db/repository/MybatisXxxRepository.java
5.9 api/dto/XxxRequest.java / XxxResponse.java
5.10 api/controller/XxxController.java    ← 薄 Controller
```

每完成一个文件，确保它只依赖同层或内层的代码。

---

## 第 6 步：代码自检

**在运行测试之前**，逐项自检：

- [ ] 每个方法 ≤10 行
- [ ] 每个方法 ≤3 参数
- [ ] 每个方法有中文 Javadoc
- [ ] domain 层无 Spring/JPA/Kafka 等框架依赖
- [ ] application 层无 infrastructure 引用
- [ ] Controller 不直接依赖 domain 模型
- [ ] 文件名和包路径符合命名约定
- [ ] 无 `record` / `@Builder` / `@Autowired`
- [ ] 无通配符 import（`domain.exception.*` 除外）
- [ ] 无循环依赖

**发现问题立即修复，不等到测试阶段。**

---

## 第 7 步：ArchUnit 校验

```bash
gradle test --tests "com.agenthub.test.architecture.AgentHubCleanArchitectureTest"
```

要求：**17/17 全部通过**。

如有违规：
1. 阅读报错信息，定位违规文件和行号
2. 根据 [REFERENCE.md](REFERENCE.md) 中的修复指引处理
3. 重新运行直到全部通过

---

## 第 8 步：集成测试验证

```bash
gradle test --tests "*XxxControllerIntegrationTest*"
```

要求：**全部通过**。如失败，根据断言信息修复业务逻辑后重新运行，然后回到第 7 步再次跑架构测试。

---

## 第 9 步：总结方案完成情况和反思

更新方案文档（第 3 步创建的 `docs/YYYY-MM-DD-feature-name-plan.md`），在末尾追加：

```markdown
## 完成情况

- ArchUnit: 17/17 ✅
- 集成测试: N/N ✅
- 创建文件: X 个
- 修改文件: Y 个

## 反思

- 遇到的问题：[描述]
- 解决方案：[描述]
- 改进建议：[如有]
```

---

## 第 10 步：等待管理员审核，持续迭代

完成后**不要主动提交代码**，告知用户：

```
方案文档: docs/YYYY-MM-DD-feature-name-plan.md
ArchUnit: 17/17 ✅
集成测试: N/N ✅
请审核代码和方案文档，如有修改意见我将立即调整。
```

收到审核意见后，回到对应步骤迭代修改，重新跑第 7、8 步验证。

---

## 修复违规快速参考

| 违规 | 修复 |
|------|------|
| 方法 >10 行 | 按逻辑段落拆分为独立私有方法 |
| 参数 >3 个 | 创建 `*Command` 封装参数 |
| domain 依赖框架 | 移除框架注解，逻辑上移到 application |
| 命名不符约定 | 重命名类，移动到正确包 |
| Controller 直调 domain | 经 UseCase + Output/Response 转换 |

详细规则和示例见 [REFERENCE.md](REFERENCE.md) 和 [EXAMPLES.md](EXAMPLES.md)。
