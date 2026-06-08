package com.agenthub.test.schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MySQL 8.0+ 方言：反引号标识符、datetime(6)、INSERT IGNORE / ON DUPLICATE KEY UPDATE。
 * MySQL 没有 schema 概念，schemaPrefix() 返回空串。
 */
public class MysqlDialect implements SchemaDialect {

    @Override
    public String name() {
        return "mysql";
    }

    @Override
    public String quoteIdentifier(String name) {
        return "`" + name + "`";
    }

    @Override
    public String mapType(Class<?> javaType, String colName) {
        if ("id".equals(colName)) {
            return "varchar(64)";
        }
        if (javaType == String.class) {
            return mapStringType(colName);
        }
        if (javaType == Long.class || javaType == long.class) {
            return "BIGINT";
        }
        if (javaType == Integer.class || javaType == int.class) {
            return "INT";
        }
        if (javaType == Short.class || javaType == short.class) {
            return "SMALLINT";
        }
        if (javaType == Boolean.class || javaType == boolean.class) {
            return "TINYINT(1)";
        }
        if (javaType == Double.class || javaType == double.class) {
            return "DOUBLE";
        }
        if (javaType == Float.class || javaType == float.class) {
            return "FLOAT";
        }
        if (javaType == BigDecimal.class) {
            return "DECIMAL(19,2)";
        }
        if (javaType == Instant.class || javaType == LocalDateTime.class) {
            return "DATETIME(6)";
        }
        if (javaType == LocalDate.class) {
            return "DATE";
        }
        if (javaType == byte[].class) {
            return "BLOB";
        }
        return "TEXT";
    }

    @Override
    public String autoIncrementType() {
        return "BIGINT NOT NULL AUTO_INCREMENT";
    }

    @Override
    public String insertOrIgnore(SqlInsert insert) {
        return "INSERT IGNORE INTO " + insert.table() + " (" + String.join(", ", insert.columns()) + ") VALUES "
            + joinValues(insert.values()) + ";";
    }

    @Override
    public String insertOrUpdate(SqlInsert insert) {
        String setClause = insert.updateColumns().stream()
            .map(c -> c + " = VALUES(" + c + ")")
            .collect(Collectors.joining(", "));
        return "INSERT INTO " + insert.table() + " (" + String.join(", ", insert.columns()) + ") VALUES "
            + joinValues(insert.values()) + " ON DUPLICATE KEY UPDATE " + setClause + ";";
    }

    @Override
    public String uuidFunction() {
        return "UUID()";
    }

    @Override
    public String concat(List<String> parts) {
        return "CONCAT(" + String.join(", ", parts) + ")";
    }

    @Override
    public String currentTimestampLiteral() {
        return "'2026-01-01 00:00:00'";
    }

    @Override
    public String schemaPrefix() {
        return "";
    }

    @Override
    public String schemaHeader() {
        return "SET NAMES utf8mb4;\nSET FOREIGN_KEY_CHECKS = 0;\n";
    }

    private String mapStringType(String columnName) {
        if (columnName.contains("url") || columnName.contains("uri") || columnName.contains("payload")) {
            return "LONGTEXT";
        }
        if (columnName.contains("description") || columnName.contains("content")) {
            return "TEXT";
        }
        return "VARCHAR(255)";
    }

    private String joinValues(List<String> values) {
        if (values.size() == 1) {
            return "(" + values.get(0) + ")";
        }
        return String.join(", ", values.stream().map(v -> "(" + v + ")").collect(Collectors.toList()));
    }
}
