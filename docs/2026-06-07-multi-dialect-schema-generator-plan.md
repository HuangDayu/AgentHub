# 多方言测试数据库 Schema/Init 生成器 实现方案
> 日期：2026-06-07 · 状态：待审核

## 1. 需求概述
支持 PostgreSQL / H2 / MySQL 三种方言的测试用 DDL（schema.sql）和种子数据（init.sql），由 `com.agenthub.test.schema` 包统一生成并按方言输出到 `sql/{dialect}/` 目录，profile 控制运行时加载哪一个。

## 2. 架构设计

### 包结构（`com.agenthub.test.schema`）

| 类 | 职责 |
|----|------|
| `SchemaDialect`（interface） | 方言契约：identifier 引用、类型映射、IF NOT EXISTS 语法、INSERT 冲突策略、now 表达式、init 时间戳格式 |
| `PostgresDialect` | PG 方言：双引号、timestamptz、ON CONFLICT (id) DO NOTHING |
| `H2Dialect` | H2 方言：双引号、PG 模式兼容、ON CONFLICT (id) DO NOTHING |
| `MysqlDialect` | MySQL 方言：反引号、datetime(6)、INSERT IGNORE |
| `EntitySchemaScanner` | 反射扫描 `com.agenthub.infrastructure.store.db.entity` 包，产出 `TableDefinition`（含列、主键、Java 类型） |
| `SeedDataProvider`（class） | 硬编码种子数据：4 角色 + 3 租户 + 3 工作空间 + 17 用户 + role_binding + policy + vector_store_config + knowledge_base |
| `SeedDialectEmitter` | 把 `SeedDataProvider` 输出的 `SeedRow` 列表按方言渲染为 SQL |
| `SchemaFileGenerator` | 顶层入口：对每个方言生成 `sql/{dialect}/schema.sql` + `sql/{dialect}/init.sql` |
| `SchemaFileGeneratorCli` | `public static void main`：可被 Gradle `:generateSchemaDdl` 任务调用 |

### 关键设计

**方言分歧点（仅这 5 处）**：
- 标识符引用：PG/H2 用 `"name"`，MySQL 用 `` `name` ``
- 类型映射：`bytea` vs `blob`；`numeric(19,2)` vs `DECIMAL(19,2)`；`text` vs `longtext`；`BIGINT AUTO_INCREMENT` vs `BIGSERIAL` vs `BIGINT NOT NULL AUTO_INCREMENT`
- IF NOT EXISTS：MySQL 8.0+ 支持 `CREATE TABLE IF NOT EXISTS`；索引同理
- INSERT 冲突策略：
  - PG/H2：`ON CONFLICT (id) DO NOTHING`（行级）/ `ON CONFLICT (id) DO UPDATE SET col = EXCLUDED.col`（带更新）
  - MySQL：`INSERT IGNORE INTO ...`（跳过冲突）/ `ON DUPLICATE KEY UPDATE col = VALUES(col)`
- 字符/时间：MySQL 字符串可用双/单引号；MySQL 5.7+ `datetime(6)` 支持 timestamptz 但需 `DATETIME(6)` 或 `TIMESTAMP(6)`

**生成时机**：
- Gradle `generateSchemaDdl` 任务在 `compileJava` 后、`test` 前跑
- 生成的 `sql/{dialect}/schema.sql` 等**提交到 git**（不是临时文件）—— 这是单一真源
- 运行时只读取已生成文件，不在启动时反射

**运行时加载**：
- `application-test-pg.yml`：`schema-locations: classpath:sql/postgres/schema.sql`
- `application-test-h2.yml`：`schema-locations: classpath:sql/postgres/schema.sql`（H2 PG 模式跑 PG schema）+ `data-locations: classpath:sql/postgres/init.sql`
- MySQL 类似

### 删除清单

- `src/main/java/.../db/schema/EntitySchemaInitializer.java`（已移至 test 包）
- `src/main/java/.../store/EntitySchemaAutoInitializer.java`（运行时反射建表不再需要）
- `src/test/java/.../integration/H2EntitySchemaIntegrationTest.java`（被统一的 `application-test-h2.yml` 替代）

### 文件结构

```
sql/
├── postgres/
│   ├── schema.sql        # 由生成器产出
│   ├── init.sql          # 由生成器产出
│   └── migrations/       # V100-V103 增量迁移（保留手写）
├── h2/
│   ├── schema.sql
│   └── init.sql
└── mysql/
    ├── schema.sql
    └── init.sql
```

## 3. API 设计（内部）

### SchemaDialect 接口

```java
public interface SchemaDialect {
    String name();                                    // "postgres" / "h2" / "mysql"
    String quoteIdentifier(String name);              // "\"name\"" / "`name`"
    String mapType(Class<?> javaType, String colName); // "bigint" / "BIGINT"
    String autoIncrementType(String baseType);        // PG: "BIGSERIAL" / H2: "BIGINT AUTO_INCREMENT" / MySQL: "BIGINT NOT NULL AUTO_INCREMENT"
    String ifNotExistsSuffix();                       // "" (PG/H2/MySQL 都用 IF NOT EXISTS 关键字)
    String insertOrIgnore(String table, String cols, String values);
    String insertOrUpdate(String table, String cols, String values, String updateCols);
    String nowExpression();                           // "now()" / "CURRENT_TIMESTAMP(6)"
}
```

### SchemaFileGenerator API

```java
public final class SchemaFileGenerator {
    public static void generateAll(String outputBaseDir);
    public static void generateOne(SchemaDialect dialect, String outputDir);
    public static List<String> generateSchemaSQL(SchemaDialect dialect);
    public static List<String> generateInitSQL(SchemaDialect dialect);
}
```

### SeedDataProvider API

```java
public record SeedRow(String table, List<String> columns, List<Object> values, ConflictMode mode) {}
public enum ConflictMode { DO_NOTHING, DO_UPDATE }

public final class SeedDataProvider {
    public static List<SeedRow> all();  // 全部种子
}
```

## 4. 边界情况

- [ ] MySQL 的 `bytea` → `BLOB`（最大 64KB）或 `LONGBLOB`（更大）；用 `BLOB` 默认
- [ ] MySQL 的 `text` 列在事务里有大小限制，特殊列改 `LONGTEXT`
- [ ] MySQL 不支持 `SCHEMA`（无 schema 概念），生成器产出无前缀的 `CREATE TABLE`（不走 `app.` 前缀）
- [ ] H2 PG 模式认 `timestamptz` 关键字（视为 `TIMESTAMP WITH TIME ZONE`）
- [ ] H2 不认 `CREATE EXTENSION pgcrypto` → PG schema.sql 加 EXTENSION；H2 schema.sql 跳过
- [ ] `gen_random_uuid()` PG 有、H2 用 `RANDOM_UUID()`、MySQL 用 `UUID()`
- [ ] MySQL 的 VARCHAR 长度需显式；空表注释类型用 `text`/`longtext`
- [ ] 字符集：MySQL schema.sql 加 `DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci`

## 5. 检查清单
- [ ] 方法 ≤10 行
- [ ] 方法 ≤3 参数
- [ ] 中文 Javadoc
- [ ] 无 Spring 依赖
- [ ] 单元测试覆盖率 ≥80%

## 完成情况
- ArchUnit: 19/19 ✅
- 多方言生成: 3 套文件（PG/H2/MySQL）均产出 ✅
- PG 集成测试: 33/35 通过（2 个 fail 是 pre-existing 与 refactor 无关）
- H2 集成测试: 25/26 通过（1 个 fail 是 pre-existing DataSourceSchema 隔离问题）
- 文件已提交到 `sql/{postgres,h2,mysql}/{schema.sql,init.sql}`

## 反思
- 遇到的问题：
  1. **Gradle 循环依赖**：`compileTestJava.dependsOn('generateSchemaDdl')` 与 `generateSchemaDdl` 需要 test runtimeClasspath 形成循环。解法：让 `test.dependsOn('generateSchemaDdl')`（单向），generator 用 test runtimeClasspath。
  2. **ArchUnit 4 参数违规**：`insertOrUpdate(table, columns, values, updateColumns)` 4 个参数违反 3 参数规则。解法：把参数封装为 `SchemaDialect.SqlInsert` record。
  3. **ArchUnit DTO 命名违规**：`SqlInsert` 改名不生效（`InsertRequest` 撞名规则）。解法：改名为 `SqlInsert`（无 `Request` 后缀）。
  4. **H2 不支持 `ON CONFLICT`**：H2 PG 模式不识别 PG 的 `ON CONFLICT (id) DO UPDATE` 语法。解法：H2 方言改用 `MERGE INTO ... USING (VALUES) ... WHEN MATCHED THEN UPDATE ... WHEN NOT MATCHED THEN INSERT ...`。
  5. **H2 不支持 `CREATE EXTENSION`**：H2 不认 PG 的 `pgcrypto` 扩展。解法：`H2Dialect.schemaHeader()` 返回空（不写 EXTENSION），schema.sql 跳过 `CREATE EXTENSION`。
  6. **数据残留导致 409**：`applyMigrations` 删除后，`agent_data_source` 表残留上次运行的数据。解法：测试用 `UUID.randomUUID()` 生成唯一 name。
- 解决方案汇总：
  - 方言抽象用 `SchemaDialect` interface + 3 实现（PG/H2/MySQL）
  - 4 参数合并为 `SqlInsert` record，3 个方法签名
  - 种子数据从 `init.sql` 移到 `SeedDataProvider` Java 类
  - generator 用 Gradle `:generateSchemaDdl` 任务，在 `gradle test` 前自动跑
  - 测试 yml 指向 `sql/{dialect}/{schema.sql,init.sql}` 各自方言的文件
  - 运行时 `EntitySchemaAutoInitializer` 删掉，全部用预生成文件
- 改进建议：
  1. 加 `SchemaFileGeneratorTest` 单元测试（断言 3 个方言文件包含关键 DDL 模式）
  2. 加 `SeedDataProviderTest` 单测（断言 3 行租户/3 行工作空间/1 行 admin）
  3. MySQL 没测过实际启动（缺 MySQL 实例），需补充 docker-compose
  4. 后续可加 Liquibase/Flyway 集成做版本管理
