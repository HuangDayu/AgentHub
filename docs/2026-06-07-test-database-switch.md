# AgentHub 测试数据库切换指南

> 日期：2026-06-07 · 状态：基础设施就绪，完整 H2 切换待迁移

## 1. 两种数据库模式

AgentHub 测试现在支持**配置驱动**的数据库切换：

| Profile | 数据库 | 启动要求 | 适用场景 |
|---------|--------|----------|----------|
| `test-pg`（默认） | PostgreSQL 15+ | 需本地启动 PG（端口 5432，DB=agenthub，user=things） | 完整集成测试、CI |
| `test-h2` | H2 2.2（嵌入式内存） | 无（自动启动） | 快速开发、无 PG 环境 |

## 2. 使用方式

### 2.1 命令行激活

```bash
# 默认（PG）
gradle test

# 显式指定 PG
gradle test -Dspring.profiles.active=test-pg

# 切换到 H2
gradle '-Dspring.profiles.active=test-h2' test --tests "*XxxTest*"
```

> ⚠️ PowerShell 下 `=` 会被解析为任务分隔符，必须用 `'-Dspring.profiles.active=test-h2'`（整段加单引号）。

### 2.2 在 IDE 中切换

Run/Debug Configuration → VM Options → `-Dspring.profiles.active=test-h2`

### 2.3 在测试类中固定 Profile

```java
@SpringBootTest(...)
@ActiveProfiles("test-h2")  // 该类永远跑 H2
class MyH2OnlyTest { }
```

## 3. 配置文件结构

```
src/test/resources/
├── application-test.yml        # 基础配置（无 datasource）
├── application-test-pg.yml     # PG datasource + 完整 schema.sql 加载
└── application-test-h2.yml     # H2 嵌入式 datasource（无 schema.sql 加载）
```

- `application-test.yml` 提供 server、multipart、redis、kafka、mybatis-plus、JWT 等**通用**配置。
- `application-test-pg.yml` 覆盖 datasource + `spring.sql.init.*`（加载 `sql/schema.sql` + `init.sql`）。
- `application-test-h2.yml` 覆盖 datasource + 关闭 SQL init（由测试 `@BeforeAll` 自管 schema）。

## 4. H2 关键配置

```yaml
url: jdbc:h2:mem:agenthub;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_DELAY=-1;NON_KEYWORDS=USER;INIT=CREATE SCHEMA IF NOT EXISTS app
```

| 参数 | 作用 |
|------|------|
| `MODE=PostgreSQL` | 兼容 PG 语法（`now()`、`timestamptz`、`SERIAL`） |
| `DATABASE_TO_LOWER=TRUE` | 表/字段名小写（与 PG 行为一致） |
| `CASE_INSENSITIVE_IDENTIFIERS=TRUE` | 标识符大小写不敏感 |
| `DB_CLOSE_DELAY=-1` | 内存 DB 在 JVM 生命周期内保持 |
| `NON_KEYWORDS=USER` | 防止 `USER` 被识别为保留字 |
| `INIT=CREATE SCHEMA IF NOT EXISTS app` | 连接建立时自动建 `app` schema |

## 5. 测试 Helper：`TestDatabaseSupport`

位于 `src/test/java/com/agenthub/test/common/TestDatabaseSupport.java`，提供：

| 方法 | 作用 |
|------|------|
| `detectDriver(Connection)` | 返回驱动名（小写），含 `h2` 或 `postgres` 关键字 |
| `isH2(conn)` / `isPostgres(conn)` | 布尔判断 |
| `dropTables(ds, tableNames)` | 按依赖反序逐个 DROP（H2 不支持 CASCADE） |
| `applyMigrations(ds, fileNames)` | 读 `sql/V*.sql`，H2 模式自动剥离 `COMMENT ON`、`timestamptz` → `TIMESTAMP WITH TIME ZONE` |
| `qualify(name, h2)` | 表名加 schema 前缀（PG: `app.X` / H2: `X`） |
| `adaptForH2(sql)` | H2 SQL 预处理 |
| `splitSqlStatements(sql)` | 多语句拆分（H2 默认不允许多语句） |

## 6. 实体类自动建表（H2 推荐方案）⭐

### 6.1 方案原理

不再依赖 `sql/schema.sql`，改为**运行时从 MyBatis-Plus Entity 类自动生成 H2 DDL**：

```
@DatabaseTable  ──反射──▶  EntitySchemaInitializer.generateH2CreateTables()
   @TableName              │
   @TableField             ▼
   @TableId            List<String> CREATE TABLE ... (H2 语法)
   Java 字段类型  ──▶  mapJavaType() → varchar / bigint / boolean / TIMESTAMP WITH TIME ZONE
```

**好处**：
- ✅ 不维护 `schema.sql`，**Entity 是唯一真源**
- ✅ 改 Entity 自动同步表结构
- ✅ H2 无需 SQL 预处理
- ✅ 60+ 张表在 1 秒内建好

### 6.2 Helper 类

`src/test/java/com/agenthub/test/common/EntitySchemaInitializer.java`：

| 方法 | 作用 |
|------|------|
| `generateH2CreateTables()` | 返回 `List<String>`，每条是一个 H2 兼容的 CREATE TABLE |
| `camelToSnake("userName")` | Java 字段名 → 列名（`user_name`） |
| `mapJavaType(Class)` | Java 类型 → H2 SQL 类型（`String`→`varchar(255)`, `Instant`→`TIMESTAMP WITH TIME ZONE`） |
| `resolveColumnName(Field)` | 优先用 `@TableField(value)`，否则 camel→snake |

**支持的 Java 类型**：`String`/`Long`/`Integer`/`Short`/`Byte`/`Boolean`/`Double`/`Float`/`BigDecimal`/`Instant`/`LocalDateTime`/`LocalDate`/`LocalTime`/`byte[]`，其余 → `text`。

### 6.3 H2 集成测试模板

```java
@SpringBootTest(...)
@ActiveProfiles("test-h2")
class MyH2Test {

    private static final File SCHEMA = writeRuntimeSchema();

    @DynamicPropertySource
    static void override(DynamicPropertyRegistry r) {
        r.add("spring.sql.init.schema-locations", () -> SCHEMA.toURI().toString());
    }

    private static File writeRuntimeSchema() {
        File tmp = Files.createTempFile("schema-", ".sql").toFile();
        tmp.deleteOnExit();
        try (PrintWriter w = new PrintWriter(tmp, "UTF-8")) {
            w.println("CREATE SCHEMA IF NOT EXISTS app;");
            for (String ddl : EntitySchemaInitializer.generateH2CreateTables()) {
                w.println(ddl);
            }
        }
        return tmp;
    }

    @Test
    void test() { /* DataSource is ready, tables exist */ }
}
```

### 6.4 已验证

`H2EntitySchemaIntegrationTest` 2/2 通过：
- `shouldCreateTablesFromEntitiesOnH2`：60+ 张表自动建好
- `shouldInsertAndSelectRow`：插入 + 查询往返成功

## 7. 迁移现有测试到 H2

### 7.1 现状

`@BeforeAll` 硬编码 `app.X CASCADE` 的测试类**当前仅支持 PG**：

- `AgentDataSourceControllerIntegrationTest`
- `AgentDataSourceComponentControllerIntegrationTest`
- `DataSourceSchemaControllerIntegrationTest`
- `PermissionStrategyControllerIntegrationTest`
- `AuditLogControllerIntegrationTest`
- `CamelDataSourcePgIntegrationTest`
- `AgentDataSourceToolCallbackProviderIntegrationTest`

### 7.2 迁移步骤（推荐）

**方案 A**（推荐）：让测试用 `EntitySchemaInitializer`，并把 PG 的 `app.` 改成默认 schema：

```java
@BeforeAll
void applyMigrations() throws Exception {
    TestDatabaseSupport.initializeFromEntities(dataSource);  // 自动建全部表
    // 特定测试数据准备 ...
}
```

**方案 B**（保持 PG 路径）：测试加 `@ActiveProfiles("test-pg")` 固定走 PG。

## 8. 已验证

| 验证项 | 结果 |
|--------|------|
| `TestDatabaseSupportTest` 9 个单元测试 | ✅ |
| `EntitySchemaInitializerTest` 6 个单元测试 | ✅ |
| `AgentHubCleanArchitectureTest` 19 条 ArchUnit 规则 | ✅ |
| `H2EntitySchemaIntegrationTest` 2 个端到端测试（H2 模式） | ✅ |
| `application-test-pg.yml`（默认）加载 | ✅ |
| `application-test-h2.yml` H2 连接 + 60+ 表自动建 | ✅ |

## 9. 待办（管理员决策）

- [x] ~~创建 H2 兼容版 `sql/h2/schema.sql`~~ **改用 Entity 驱动方案，无需 SQL 文件**
- [ ] 迁移 7 个集成测试类的 `@BeforeAll` 使用 `TestDatabaseSupport.initializeFromEntities`（约 1 小时）
- [ ] 验证主代码 MyBatis 在 H2 上的兼容性（驼峰转下划线、`@TableField` 大小写适配）
- [ ] 跑业务逻辑集成测试 on H2（验证 Camel JDBC、审计、限流等）
- [ ] CI 矩阵：PG + H2 双跑
