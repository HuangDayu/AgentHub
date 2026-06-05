# VibeCoding 架构防腐实践

> 基于 AgentHub 项目的整洁架构、ArchUnit 规则引擎、TDD 开发流程与 AI Agent 协作契约的防腐经验总结。

---

## 1. 根本原因：VibeCoding 为何导致架构腐败

"VibeCoding" 是指借助 AI 编程助手（如 Claude Code、Copilot、Cursor 等）以自然语言描述意图、由 AI 直接生成代码的开发模式。这种模式极大提升了开发效率，但也埋下了架构腐败的隐患。

### 1.1 AI 的"局部最优"倾向

AI 编程助手在处理代码任务时有几个天然倾向：

- **局部视角**：AI 只能看到当前打开的文件或有限的上下文窗口，天然缺乏对全局架构的认知。当用户说"帮我加一个查询接口"，AI 可能直接在 Controller 里写 SQL —— 因为它不知道这个项目有 Repository 层。
- **模式记忆偏差**：AI 训练数据中充斥着大量"快速原型"风格的代码—— Controller 直接调 Service，Service 直接调 DAO，没有分层、没有接口抽象。当缺乏明确约束时，AI 会自然滑向这些常见但不规范的写法。
- **路径依赖**：一旦项目中某处出现了架构违规（比如 domain 层依赖了 Spring 注解），AI 会把这种违规当作"项目惯例"，在后续代码中继续复制 —— 导致腐败加速扩散。

### 1.2 架构腐败的典型路径

在没有防腐机制的项目中，VibeCoding 导致的架构腐败通常遵循以下路径：

```
第一次：AI 在 Controller 里直接写了业务逻辑 → 没人注意到
     ↓
第二次：AI 在 domain 模型上加了 @TableName → 编译通过了
     ↓
第三次：Infrastructure 的类被 API 层直接引用 → 反正能跑
     ↓
第 N 次：分层边界完全模糊，依赖方向混乱，新功能不知道写在哪层
     ↓
最终：架构名存实亡，代码库变成"大泥球"（Big Ball of Mud）
```

### 1.3 AgentHub 项目的现实教训

AgentHub 项目在发展过程中也经历过类似的挑战。在引入 `MethodViolationDumper` 全量扫描之前，项目中有 1000+ 的伪违规（70% 是扫描器 bug），真实的违规也高达 324 条 —— 包括 `api.controller` 直接依赖 `domain.model.*`、领域模型内嵌 `*Request` 类等典型问题。这些问题的共同根源是：**AI 生成代码时缺乏架构边界意识，而缺乏自动化检查让这些问题得以累积。**

本文后续章节将逐一阐述 AgentHub 如何通过四层防线——**架构设计、自动化检验、编码规范、开发流程**——构建一套完整的架构防腐体系，并在第六节专门讨论如何让 AI Agent 本身成为遵守契约的协作者。

---

## 2. 防腐架构：整洁架构和 SOLID 设计原则

### 2.1 整洁架构：用分层划定边界

整洁架构（Clean Architecture）是抵御架构腐败的第一道防线。它通过**明确的层级边界**和**单向依赖规则**，在物理层面约束了代码的组织方式。

AgentHub 采用严格的四层架构：

```
┌─────────────────────────────────────────────────────────┐
│                     api (接口适配层)                      │
│    Controller · Request/Response DTO · Exception Handler │
│                     ↑ 依赖方向                           │
├─────────────────────────────────────────────────────────┤
│                 application (应用层)                      │
│    UseCase · Command · Port(Interface) · Output          │
│                     ↑ 依赖方向                           │
├─────────────────────────────────────────────────────────┤
│                  domain (领域层)                          │
│    Model · Event · Exception · 核心业务规则              │
│                     ↑ 依赖方向                           │
├─────────────────────────────────────────────────────────┤
│              infrastructure (基础设施层)                   │
│    agents · auth · etl · rag · store · tools · vector    │
│    (实现 application/port 接口)                           │
└─────────────────────────────────────────────────────────┘
```

**依赖规则**（单向，不可逆）：

| 规则 | 说明 |
|------|------|
| `api` → `application` | API 层调用应用层 UseCase |
| `application` → `domain` | 应用层依赖领域模型 |
| `infrastructure` → `application/port` + `domain` | 基础设施层实现端口接口 |
| ❌ `domain` 禁止依赖任何框架 | 纯 POJO，禁止 Spring/JPA/Kafka 等 |
| ❌ `application` 禁止依赖 `infrastructure` | 通过 Port 接口反转依赖 |
| ❌ `api.controller` 禁止直接依赖 `domain` | 必须通过 application 层访问 |

### 2.2 SOLID 原则：让设计自带免疫力

仅靠分层是不够的 —— 如果每一层内部的代码组织混乱，架构同样会腐化。SOLID 原则为每一层内部的代码组织提供了指南：

| 原则 | 在 AgentHub 中的实践 |
|------|---------------------|
| **单一职责 (SRP)** | 每个 UseCase 只处理一个业务用例；Controller 只做参数适配和委托 |
| **开闭原则 (OCP)** | 通过 `application/port/out` 定义扩展点，infrastructure 实现新适配器即可扩展 |
| **里氏替换 (LSP)** | Port 接口约定行为契约，所有实现类必须满足 |
| **接口隔离 (ISP)** | `application/port/out/` 下按领域拆分 25 个精细的 Repository 接口 |
| **依赖倒置 (DIP)** | application 定义 Port 接口，infrastructure 实现 —— 高层不依赖底层 |

### 2.3 关键设计决策：Port & Adapter 模式

AgentHub 最核心的防腐机制是 **Port & Adapter** 模式（又称六边形架构）：

```java
// application/port/out/repositories/AgentRepository.java — 端口接口（在应用层定义）
public interface AgentRepository {
    Optional<Agent> findById(String id);
    Agent save(Agent agent);
    void deleteById(String id);
}

// infrastructure/store/db/repository/MybatisAgentRepository.java — 适配器实现（在基础设施层）
@Component
@Primary
public class MybatisAgentRepository implements AgentRepository {
    private final AgentMapper agentMapper;
    // 实现细节...
}
```

**为什么这能防腐？** 当 AI 被告知"实现 AgentRepository"时，它必须实现已定义的接口 —— 接口的方法签名、参数类型、返回类型都已经规定好了。AI 的自由度被限定在"如何实现"而非"如何设计"，从根本上减少了架构违规的可能性。

---

## 3. 架构检验：ArchUnit 规则检验保证整洁架构持续迭代

设计文档写得再好、设计原则理解得再透彻，如果没有**自动化检验**，一切都会在持续的 VibeCoding 中逐渐走样。AgentHub 使用 ArchUnit 将架构规则编写成**可执行的测试用例**，每次构建都会校验。

### 3.1 分层依赖检验

`AgentHubCleanArchitectureTest` 中定义了 6 个核心依赖规则测试：

```java
// 1. 领域层不应该依赖任何外层
@Test
void domain_should_not_depend_on_outer_layers() {
    noClasses().that().resideInAPackage("..domain..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("..application..", "..infrastructure..", "..api..")
        .because("领域层应该是核心层，不应该依赖应用层、基础设施层或API层")
        .check(classes);
}

// 2. 应用层不应该依赖基础设施层或 API 层
@Test
void application_should_not_depend_on_infrastructure_or_api() { ... }

// 3. API 层不应该依赖基础设施层
@Test
void api_should_not_depend_on_infrastructure() { ... }

// 4. 基础设施层不应该依赖 API 层
@Test
void infrastructure_should_not_depend_on_api() { ... }

// 5. API 控制器不应该直接依赖领域层
@Test
void api_should_not_directly_depend_on_domain() { ... }

// 6. 领域模型应该为纯 POJO（禁止依赖 Spring/JPA 等框架）
@Test
void domain_models_should_be_pojos() { ... }
```

这些测试在 `gradle test` 时自动运行。**任何一个违规都会导致构建失败** —— 这意味着没有任何架构腐败能"悄悄溜进"代码库。

### 3.2 命名约定与位置检验

除了依赖方向，ArchUnit 还检验代码的"位置"是否正确：

| 测试 | 规则 |
|------|------|
| `application_use_case_naming_convention` | 应用层用例类必须以 `UseCase` 结尾 |
| `repository_interfaces_should_be_in_application` | `*Repository` 接口必须在 `application.port.out` 包中 |
| `repository_implementations_should_be_in_infrastructure` | `Mybatis*Repository` 实现必须在 `infrastructure.store.db.repository` 包中 |
| `controller_naming_convention` | 控制器类必须以 `Controller` 结尾 |
| `dtos_should_be_in_api_dto_package` | `*Request`/`*Response`/`*DTO` 必须在 `api.dto` 或 `infrastructure..dto` 包中 |
| `domain_events_should_be_in_domain_event_package` | `*Event` 类必须在 `domain.event` 包中 |

这些规则确保 AI 生成的新文件即使逻辑正确，只要放错了包位置就会被拦截 —— 避免了"所有的类都堆在同一个包下"这种典型的 VibeCoding 后遗症。

### 3.3 循环依赖检验

循环依赖是架构腐化的隐秘杀手 —— 它让模块之间耦合到无法单独理解和测试。ArchUnit 通过 slice 规则检测循环：

```java
@Test
void no_cycles_in_application_layer() {
    slices().matching("..application.(*)..")
        .should().beFreeOfCycles()
        .because("应用层不应该有循环依赖")
        .check(classes);
}

@Test
void no_cycles_in_domain_layer() {
    slices().matching("..domain.(*)..")
        .should().beFreeOfCycles()
        .because("领域层不应该有循环依赖")
        .check(classes);
}
```

### 3.4 方法复杂度检验：≤10 行 & ≤3 参数

这是 AgentHub 最具特色的防腐规则。`AGENTS.md` 明确规定：

> 每个方法不超过 **10 行**，方法参数不超过 **3 个**

这两条规则同样由 ArchUnit 强制执行：

```java
@Test
void methods_should_not_exceed_ten_lines() {
    methods()
        .that().areDeclaredInClassesThat().areNotInterfaces()
        .and().areDeclaredInClassesThat().resideInAPackage("com.agenthub..")
        .should(new ArchCondition<JavaMethod>("不超过10行代码") {
            public void check(JavaMethod method, ConditionEvents events) {
                if (MethodComplexityRules.shouldSkip(method) || isExempted(method)) return;
                int lineCount = MethodLineAnalyzer.countLines(method);
                if (lineCount > MAX_METHOD_LINES) { /* 报告违规 */ }
            }
        }).check(classes);
}
```

**为什么 ≤10 行如此重要？** 在 VibeCoding 中，AI 倾向于生成"大方法"—— 把所有逻辑塞进一个方法里，因为对 AI 来说这是最简单的思维方式。但大方法是代码腐化的温床：难以理解、难以测试、难以复用。强制 ≤10 行迫使开发者（和 AI）将逻辑拆分为有明确语义的小方法，间接提升了代码质量。

### 3.5 豁免机制：务实的设计

严格的规则必须有务实的例外处理。AgentHub 提供了完善的豁免体系：

- **自动排除**：getter/setter、toString、equals、hashCode、Lombok 生成方法、枚举方法等
- **人工审查豁免**：`architecture-exemptions.json` 配置文件，对已通过审查但暂不适合重构的方法进行豁免
- **分级治理**：`MethodViolationDumper` 将违规按严重程度分为 HEAVY/MEDIUM/LIGHT 三级，优先处理 HEAVY 违规

这种机制保证了规则不会因为"一刀切"而被放弃 —— 团队可以渐进式地消除违规，而不是被规则压垮。

---

## 4. 编码规范：强制性编码规范保证代码整洁

ArchUnit 防守的是**架构边界**，但边界的内部同样需要规范来保证**代码整洁**。AgentHub 的编码规范覆盖了从命名到注解、从 import 到 DTO 转换的方方面面。

### 4.1 命名约定：见名知位

AgentHub 定义了严格的命名约定表，每种代码元素都有固定的后缀和位置：

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
| Command 对象 | `*Command` | `application/command/` |
| 领域模型 | 领域名词 | `domain/model/` |

**为什么这很重要？** 对于 AI 来说，命名约定是一种**隐式指令**。当 AI 被告知"创建一个 UseCase"时，如果项目中有 33 个 `*UseCase` 类且都在 `application/usecase/` 包下，AI 会自然模仿这种模式。反之，如果命名混乱，AI 也会混乱。

### 4.2 类注解规范

每层有固定的注解模式，减少 AI 的"创造性发挥"：

```java
// Controller — 始终 @RestController + @RequestMapping
@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/agents")
@RequiredArgsConstructor
public class AgentHubController { ... }

// UseCase — @Component + @RequiredArgsConstructor（构造器注入）
@Component
@RequiredArgsConstructor
public class AgentUseCase { ... }

// Domain 模型 — 纯 POJO，@Data + @NoArgsConstructor + @AllArgsConstructor
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Agent { ... }

// Infrastructure Repository — @Component + @Primary
@Component
@Primary
public class MybatisAgentRepository implements AgentRepository { ... }
```

### 4.3 明确的禁令

AgentHub 不仅规定了"应该怎么做"，还明确列出了"禁止怎么做"：

| 禁止项 | 原因 |
|--------|------|
| 禁止使用 Java `record` 类 | 与 Lombok 体系不一致，序列化兼容性问题 |
| 禁止使用 `@Builder` | 避免 Builder 模式的滥用，保持代码风格统一 |
| 禁止使用 `@Autowired` 字段注入 | 必须使用 `@RequiredArgsConstructor` 构造器注入 |
| 禁止通配符 import（`import xxx.*`） | 保持依赖关系透明（领域异常包除外） |
| 禁止方法超过 10 行 | 强制小方法、单一职责 |
| 禁止方法参数超过 3 个 | 强制使用 Command/DTO 封装参数 |

### 4.4 DTO 转换链

明确的数据转换链路：`Request → Command → Domain Model → Output → Response`

```java
// 统一使用 hutool BeanUtil 进行对象转换
private SomeResponse toResponse(SomeOutput output) {
    SomeResponse response = new SomeResponse();
    BeanUtil.copyProperties(output, response);
    return response;
}
```

固定的转换链和统一的工具类避免了 AI 在每一层"即兴发挥"不同的转换方式。

---

## 5. 开发模式：测试驱动开发保证代码正确

架构规则和编码规范解决了"代码长什么样"的问题，但**代码对不对**，需要测试来保证。AgentHub 严格遵循 TDD 流程：**Red → Green → Refactor**。

### 5.1 TDD 的防腐价值

TDD 对 VibeCoding 的防腐作用体现在三个层面：

1. **意图锁（Specification Lock）**：测试用例是需求的**可执行规格书**。当 AI 后续修改代码时，测试会立刻捕获行为变化，而非在几周后的集成阶段才发现。
2. **设计约束（Design Constraint）**：写测试的过程迫使开发者思考接口设计。一个难以测试的类通常也设计得不好 —— 这种"可测试性反馈"在 VibeCoding 中尤其珍贵。
3. **重构安全网（Safety Net）**：当 AI 进行重构时（如按 ArchUnit 要求拆分大方法），通过的测试保证了行为不变。

### 5.2 三层测试金字塔

| 测试类型 | 范围 | 框架 | 覆盖率要求 |
|---------|------|------|-----------|
| 架构测试 | 全局架构规则 | ArchUnit | 17/17 规则通过 |
| 集成测试 | Controller 完整链路 | Spring Boot Test | **100%** |
| 单元测试 | 领域模型和工具类 | JUnit 6 | 核心逻辑覆盖 |

**Controller 集成测试 100% 覆盖**是 AgentHub 最硬性的要求。这意味着每个 API 端点的完整请求-响应链路都有测试保护。当 AI 修改 Controller、UseCase 或 Repository 实现时，这些测试会立刻发现回归问题。

### 5.3 测试即文档

测试代码本身也是给 AI 看的"活文档"：

```java
@Test
void shouldCreateAgent() throws Exception {
    mockMvc.perform(post("/api/v1/workspaces/{wsId}/agents", workspaceId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"name": "test-agent"}"""))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("test-agent"));
}
```

当 AI 需要理解"如何创建 Agent"时，这段测试代码比任何文档都更精确地描述了 API 行为。

---

## 6. 人机协作：如何让 Coding Agent 遵守契约

以上四道防线——架构、检验、规范、测试——构成了自动化防腐体系。但最关键的环节始终是**人**：如何让 AI Agent 在生成代码时**主动遵守**这些规则，而不是每次都由 ArchUnit 事后拦截？

AgentHub 项目探索出一套"AI Agent 契约"机制，核心文件是 `AGENTS.md`。

### 6.1 AGENTS.md：AI Agent 的"宪法"

`AGENTS.md` 是专门写给 AI 编程助手看的项目指令文件。它不是给人看的文档，而是 AI 的**行为契约**。它包含三部分关键内容：

**(1) 架构地图**

```
api (Controller/DTO) → application (UseCase/Port) → domain (Model)
                                                          ↑
infrastructure (agents/auth/store/etl/rag/tools/vector) ──┘
```

AI 在处理任何代码任务前，首先要理解自己处于哪个层级以及该层级的职责边界。

**(2) 命名约定表**

| 类型 | 命名规则 | 位置 |
|------|---------|------|
| Controller | `*Controller` | `api/controller/` |
| UseCase | `*UseCase` | `application/usecase/` |
| ... | ... | ... |

这张表告诉 AI："当你创建 X 类型时，你应该命名它 Y，放在目录 Z"。消除了 AI 的"命名自由裁量权"。

**(3) 行为约束清单**

```markdown
### 方法约束
- 每个方法不超过 **10 行**
- 方法参数不超过 **3 个**
- 每个方法必须有 Javadoc 注释（中文）
- 布尔字段使用 `isEnabled()` 而非 `getEnabled()`

### 禁止项
- 禁止使用 `record` 类和 `@Builder`
- 禁止使用 `@Autowired`
- 禁止通配符 import
```

### 6.2 CLAUDE.md：Claude Code 的专项强化

除了通用的 `AGENTS.md`，AgentHub 还为 Claude Code 提供了 `CLAUDE.md`，增加了四条 AI 行为准则：

1. **编码前先思考**："不要假设。不要隐藏困惑。明确权衡。"
2. **简洁优先**："不添加超出要求的功能。不为单次使用的代码创建抽象。"
3. **精准修改**："不要'改进'相邻的代码、注释或格式。不要重构没有问题的部分。"
4. **目标驱动执行**："定义成功标准。循环直到验证通过。"

这四条准则直接针对 VibeCoding 中 AI 最常见的反模式：过度设计（over-engineering）、随意重构（gratuitous refactoring）、模糊理解（hallucination）。

### 6.3 人机协作的反馈闭环

完整的协防体系是一个**自动反馈闭环**：

```
开发者/AI 编写代码
        ↓
ArchUnit 架构测试  ←  违规  →  定位并修复
        ↓ 通过
方法复杂度检测    ←  违规  →  拆分或豁免
        ↓ 通过
命名/位置约定    ←  违规  →  重命名或移动
        ↓ 通过
集成测试         ←  失败  →  修复业务逻辑
        ↓ 通过
代码合入
```

AI 在这个过程中扮演双重角色：既是代码的生产者，也是**被规则约束的对象**。当 AI 提交的代码违反架构规则时，它会从失败的测试中获得反馈，并在后续生成中自我纠正。

### 6.4 实践心得：契约式协作的三个关键

1. **规则必须可自动执行**：写在文档里的规则不如写在测试里的规则。AgentHub 将 10 行/3 参数等规则写入了 ArchUnit 测试，而非仅写在 README 中。
2. **指令要精确到消除歧义**：对 AI 说"保持代码整洁"是无效的；说"每个方法 ≤10 行，≤3 参数，需 Javadoc，禁止 record 和 @Builder"才是有效的。
3. **豁免机制保护规则的严肃性**：面对"紧急需求"时，如果规则被频繁绕过，规则就会形同虚设。`architecture-exemptions.json` 提供了"审查后豁免"的正式通道，避免了"暂时跳过测试"的滑坡。

---

## 7. 总结

VibeCoding 不是洪水猛兽，但它确实会加速架构腐败 —— 就像水流加速堤坝侵蚀一样。对抗这种趋势，需要的不是放弃 AI，而是建立一套**多层防腐体系**：

| 防线 | 机制 | 核心工具 | 保护对象 |
|------|------|---------|---------|
| 第一层 | 架构设计 | 整洁架构 + SOLID + Port/Adapter | 模块边界 |
| 第二层 | 架构检验 | ArchUnit 规则（依赖/命名/位置/循环/复杂度） | 分层约束 |
| 第三层 | 编码规范 | AGENTS.md 契约 + Alibaba P3C | 代码整洁度 |
| 第四层 | 开发流程 | TDD（Red → Green → Refactor） | 行为正确性 |
| 第五层 | 人机协作 | AGENTS.md + CLAUDE.md + 反馈闭环 | AI 生成质量 |

每一层都是一道防线，每一道防线都在自动运行。当 AI 生成的代码通过所有五道防线时，它不仅是"能运行"的代码，更是"符合架构契约、经过测试验证、可以被团队成员理解"的代码。

**架构防腐的本质，不是约束 AI，而是用自动化规则表达团队的设计共识，让 AI 成为共识的执行者而非破坏者。**

---

> **参考文档**
> - [架构设计文档](2026-05-16-architecture-design.md)
> - [开发指南](2026-05-16-development-guide.md)
> - [方法复杂度治理方案](2026-06-04-method-complexity-governance.md)
> - [AGENTS.md](../AGENTS.md) — AI Agent 行为契约
> - [CLAUDE.md](../CLAUDE.md) — Claude Code 专项指令
> - [AgentHubCleanArchitectureTest.java](../src/test/java/com/agenthub/test/architecture/AgentHubCleanArchitectureTest.java) — ArchUnit 规则实现
