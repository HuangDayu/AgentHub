package com.agenthub.test.schema;

import java.util.List;

/**
 * 方言契约：抽象不同数据库的语法差异（DDL 标识符引用、类型映射、INSERT 冲突策略）。
 */
public interface SchemaDialect {

    /**
     * 方言名：postgres / h2 / mysql
     */
    String name();

    /**
     * 引用标识符：PG/H2 用双引号，MySQL 用反引号
     */
    String quoteIdentifier(String name);

    /**
     * Java 类型到 SQL 类型的映射（colName 用于"id"特殊处理）
     */
    String mapType(EntitySchemaScanner.ColumnDefinition  col);

    /**
     * 自增主键类型（PG BIGSERIAL / H2 BIGINT AUTO_INCREMENT / MySQL BIGINT NOT NULL AUTO_INCREMENT）
     */
    String autoIncrementType();

    /**
     * 生成 INSERT ... ON CONFLICT (id) DO NOTHING 风格语句
     */
    String insertOrIgnore(SqlInsert insert);

    /**
     * 生成 INSERT ... ON CONFLICT (id) DO UPDATE SET ... 风格语句
     */
    String insertOrUpdate(SqlInsert insert);

    /**
     * 生成主键生成函数（用于 role_binding.id 这种 CONCAT 拼接的 id 字段）
     */
    String uuidFunction();

    /**
     * SELECT CONCAT(...) 的等价物（H2/MySQL 也支持 CONCAT；仅当不兼容时 override）
     */
    String concat(List<String> parts);

    /**
     * CURRENT_TIMESTAMP / now() / CURRENT_TIMESTAMP(6)
     */
    String currentTimestampLiteral();

    /**
     * 标识符大小写策略（PG/H2 是 unquoted lowercase；MySQL 依赖 lower_case_table_names）
     */
    String schemaPrefix();

    /**
     * schema.sql 文件头（CREATE SCHEMA、CREATE EXTENSION 等）
     */
    String schemaHeader();

    /**
     * INSERT 请求封装（避免方法参数超过 3 个）
     */
    record SqlInsert(String table, List<String> columns, List<String> values, List<String> updateColumns) {
        public static SqlInsert of(String table, List<String> columns, List<String> values) {
            return new SqlInsert(table, columns, values, List.of());
        }
    }
}

