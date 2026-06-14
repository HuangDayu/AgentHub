# AgentDataModel 元数据驱动通用 CRUD 方案

> 日期：2026-06-13 · 状态：待审核

## 1. 需求概述

**问题**：当前工具系统是手动编码驱动的，每个 API 需要单独编写工具类，会导致：
- 工具数量爆炸（40+ 个工具）
- Agent 上下文窗口被工具定义占满
- 新增业务需要手动编写工具类

**目标**：实现元数据驱动的通用 CRUD，固定 5-6 个工具，Agent 通过"先认识数据，再操作数据"的方式完成业务。

**核心价值**：
- 工具数量固定，不随业务增长
- Agent 学习成本低，只需理解数据模型
- 新增业务实体只需加注解，无需写工具类

## 2. 架构设计

| 层 | 文件 | 操作 | 说明 |
|----|------|------|------|
| domain | `AgentDataModel.java` | 新建 | 注解定义，标记可操作的实体 |
| domain | `DataModelMetadata.java` | 新建 | 元数据模型，描述实体结构 |
| domain | `DataFieldMetadata.java` | 新建 | 字段元数据，描述字段信息 |
| infrastructure | `DataModelScanner.java` | 新建 | 启动时扫描注解，生成元数据 |
| infrastructure | `DataModelTools.java` | 新建 | 通用 CRUD 工具类 |
| infrastructure | `DataModelInvoker.java` | 新建 | 直接调用 Mapper 的通用执行器 |
| entity | 多个 Entity 类 | 修改 | 添加 `@AgentDataModel` 注解 |

## 3. 注解规范

### 3.1 `@AgentDataModel` 注解

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AgentDataModel {
    /** 数据模型名称（Agent 看到的名称） */
    String name();
    
    /** 数据模型描述（Agent 看到的描述） */
    String description();
    
    /** 所属领域（用于分组） */
    String domain() default "通用";
    
    /** 图标名称（可选） */
    String icon() default "table";
    
    /** 是否支持创建 */
    boolean creatable() default true;
    
    /** 是否支持更新 */
    boolean updatable() default true;
    
    /** 是否支持删除 */
    boolean deletable() default true;
    
    /** 关联的 Mapper 类（直接操作数据库） */
    Class<?> mapper();
    
    /** 租户字段名（用于租户隔离） */
    String tenantField() default "tenantId";
    
    /** 工作空间字段名（用于工作空间隔离） */
    String workspaceField() default "workspaceId";
}
```

### 3.2 `@AgentDataField` 注解（可选，用于字段级控制）

```java
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AgentDataField {
    /** 字段描述 */
    String description() default "";
    
    /** 是否在列表中显示 */
    boolean listVisible() default true;
    
    /** 是否在详情中显示 */
    boolean detailVisible() default true;
    
    /** 是否可作为过滤条件 */
    boolean filterable() default false;
    
    /** 是否可排序 */
    boolean sortable() default false;
    
    /** 是否必填（创建时） */
    boolean required() default false;
    
    /** 是否隐藏（从 Agent 视图中隐藏） */
    boolean hidden() default false;
}
```

### 3.3 使用示例

```java
@AgentDataModel(
    name = "知识库",
    description = "用于存储和检索文档的知识库",
    domain = "知识管理",
    mapper = KnowledgeBaseMapper.class
)
@Data
@TableName("knowledge_base")
public class KnowledgeBase {
    private String id;
    
    @AgentDataField(description = "知识库名称", required = true, filterable = true)
    private String name;
    
    @AgentDataField(description = "知识库描述")
    private String description;
    
    @AgentDataField(hidden = true)  // 从上下文自动填充
    private String workspaceId;
    
    @AgentDataField(hidden = true)
    private String tenantId;
    
    private Instant createdAt;
    private Instant updatedAt;
}
```

## 4. 工具设计

### 4.1 工具列表

| 工具名 | 功能 | 输入 | 输出 |
|--------|------|------|------|
| `listModels` | 列出所有可操作的数据模型 | 无 | 模型列表（名称、描述、领域） |
| `describeModel` | 查看模型结构 | model: 模型名称 | 字段列表（名称、类型、描述） |
| `query` | 查询数据 | model, filters, page, size | 数据列表 + 分页信息 |
| `create` | 创建数据 | model, data | 创建结果 |
| `update` | 更新数据 | model, id, data | 更新结果 |
| `delete` | 删除数据 | model, id | 删除结果 |

### 4.2 Agent 调用示例

```json
// 1. 探索阶段
{"tool": "listModels"}
{"tool": "describeModel", "arguments": {"model": "知识库"}}

// 2. 操作阶段
{"tool": "query", "arguments": {"model": "知识库", "filters": {"name": "Java"}}}
{"tool": "create", "arguments": {"model": "知识库", "data": {"name": "新知识库", "description": "..."}}}
```

## 5. 实现步骤

### 步骤 1：创建注解（domain 层）

- [ ] 创建 `@AgentDataModel` 注解
- [ ] 创建 `@AgentDataField` 注解
- [ ] 创建 `DataModelMetadata` 模型类
- [ ] 创建 `DataFieldMetadata` 模型类

### 步骤 2：创建元数据扫描器（infrastructure 层）

- [ ] 创建 `DataModelScanner` 类
- [ ] 实现 `@PostConstruct` 扫描逻辑
- [ ] 解析注解生成元数据
- [ ] 缓存元数据到内存

### 步骤 3：创建通用工具（infrastructure 层）

- [ ] 创建 `DataModelTools` 类
- [ ] 实现 `listModels()` 工具方法
- [ ] 实现 `describeModel()` 工具方法
- [ ] 实现 `query()` 工具方法
- [ ] 实现 `create()` 工具方法
- [ ] 实现 `update()` 工具方法
- [ ] 实现 `delete()` 工具方法

### 步骤 4：创建通用执行器（infrastructure 层）

- [ ] 创建 `DataModelInvoker` 类
- [ ] 实现直接调用 Mapper 的通用方法
- [ ] 使用 MyBatis-Plus 的 `LambdaQueryWrapper` 进行条件查询
- [ ] 处理参数映射和类型转换
- [ ] 处理异常和错误

### 步骤 5：给 Entity 添加注解

- [ ] KnowledgeBase（知识库）
- [ ] Agent（Agent）
- [ ] Workspace（工作空间）
- [ ] ModelConfig（模型配置）
- [ ] Skill（技能）
- [ ] AgentTeam（Agent 团队）
- [ ] McpTool（MCP 工具）
- [ ] PromptTemplateInfo（提示词模板）
- [ ] ScheduledTask（定时任务）
- [ ] 其他需要的 Entity...

### 步骤 6：测试验证

- [ ] 单元测试：元数据扫描
- [ ] 单元测试：通用 CRUD
- [ ] 集成测试：完整流程
- [ ] ArchUnit 测试：架构合规

## 6. 边界情况

| 场景 | 处理方式 |
|------|---------|
| Mapper 没有 `selectByTenantId` 方法 | 使用 `LambdaQueryWrapper` 构建查询条件 |
| Entity 没有 `tenantId` 字段 | 不做租户过滤 |
| 字段类型不支持 | 跳过该字段，日志警告 |
| Mapper 方法不存在 | 返回错误提示 |
| 并发创建导致 ID 冲突 | 重试 3 次 |
| 大数据量查询 | 强制分页，最大 100 条/页 |

## 7. 检查清单

- [ ] 注解定义符合 Java 规范
- [ ] 元数据模型不可变（使用 `@Data` + `final` 字段）
- [ ] 扫描器只在启动时执行一次
- [ ] 工具方法都有 Javadoc 注释
- [ ] 反射调用有异常处理
- [ ] 租户隔离正确实现
- [ ] 分页查询支持
- [ ] 字段过滤正确
- [ ] 创建/更新参数校验
- [ ] 删除操作有确认机制

## 8. 技术约束

- **Mapper 直接操作**：使用 MyBatis-Plus 的 `BaseMapper` 接口，直接操作数据库
- **条件查询**：使用 `LambdaQueryWrapper` 构建类型安全的查询条件
- **类型转换**：使用 Jackson 进行 JSON 和对象转换
- **异常处理**：所有异常都需要捕获并转换为友好错误
- **性能考虑**：元数据只在启动时扫描一次，运行时使用缓存

## 9. 与其他方案的对比

| 方案 | 工具数量 | 扩展性 | 实现复杂度 | Agent 学习成本 |
|------|---------|--------|-----------|---------------|
| 手动工具类 | N 个（N=API 数量） | 低 | 低 | 高 |
| OpenAPI 自动生成 | N 个 | 中 | 高 | 高 |
| **元数据驱动 CRUD** | **固定 6 个** | **高** | **中** | **低** |

## 10. 完成情况

- [x] 创建 `@AgentDataModel` 注解
- [x] 创建 `@AgentDataField` 注解
- [x] 创建 `DataModelMetadata` 模型类
- [x] 创建 `DataFieldMetadata` 模型类
- [x] 创建 `DataModelScanner` 类
- [x] 创建 `DataModelInvoker` 类
- [x] 创建 `DataModelTools` 类
- [x] 创建 DTO 类（QueryParams、QueryInput、UpdateInput、FilterContext）
- [x] 为现有实体添加 `@AgentDataModel` 注解
- [x] ArchUnit 测试全部通过（17/17）

## 11. 后续扩展

- **批量操作**：支持 `batchCreate`、`batchUpdate`、`batchDelete`
- **关联查询**：支持 `include` 参数加载关联数据
- **聚合查询**：支持 `count`、`sum`、`avg` 等聚合操作
- **导出功能**：支持导出为 CSV/JSON
- **审计日志**：记录所有数据操作
