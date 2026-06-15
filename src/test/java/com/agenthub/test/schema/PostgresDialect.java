package com.agenthub.test.schema;

import org.apache.ibatis.type.JdbcType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.agenthub.test.schema.SchemaFileGenerator.mapJdbcType;

/**
 * PostgreSQL 方言：双引号标识符、timestamptz、ON CONFLICT (id) DO NOTHING/UPDATE。
 */
public class PostgresDialect implements SchemaDialect {

    @Override
    public String name() {
        return "postgres";
    }

    @Override
    public String quoteIdentifier(String name) {
        return "\"" + name + "\"";
    }

    @Override
    public String mapType(EntitySchemaScanner.ColumnDefinition col) {
        Class<?> javaType = col.javaType;
        String colName = col.columnName;
        if (col.tableField != null && !col.tableField.jdbcType().equals(JdbcType.UNDEFINED)) {
            return mapJdbcType(col.tableField.jdbcType()).toUpperCase();
        }
        if ("id".equals(colName)) {
            return "varchar(64)";
        }
        if (javaType == String.class) {
            return mapStringType(col);
        }
        if (javaType == Long.class || javaType == long.class) {
            return "bigint";
        }
        if (javaType == Integer.class || javaType == int.class) {
            return "integer";
        }
        if (javaType == Short.class || javaType == short.class) {
            return "smallint";
        }
        if (javaType == Boolean.class || javaType == boolean.class) {
            return "boolean";
        }
        if (javaType == Double.class || javaType == double.class) {
            return "double precision";
        }
        if (javaType == Float.class || javaType == float.class) {
            return "real";
        }
        if (javaType == BigDecimal.class) {
            return "numeric(19,2)";
        }
        if (javaType == Instant.class || javaType == LocalDateTime.class) {
            return "timestamptz";
        }
        if (javaType == LocalDate.class) {
            return "date";
        }
        if (javaType == byte[].class) {
            return "bytea";
        }
        return "text";
    }

    @Override
    public String autoIncrementType() {
        return "bigserial";
    }

    @Override
    public String insertOrIgnore(SqlInsert insert) {
        return "INSERT INTO " + insert.table() + " (" + String.join(", ", insert.columns()) + ") VALUES "
                + joinValues(insert.values()) + " ON CONFLICT (id) DO NOTHING;";
    }

    @Override
    public String insertOrUpdate(SqlInsert insert) {
        String setClause = insert.updateColumns().stream()
                .map(c -> c + " = EXCLUDED." + c)
                .collect(Collectors.joining(", "));
        return "INSERT INTO " + insert.table() + " (" + String.join(", ", insert.columns()) + ") VALUES "
                + joinValues(insert.values()) + " ON CONFLICT (id) DO UPDATE SET " + setClause + ";";
    }

    @Override
    public String uuidFunction() {
        return "gen_random_uuid()";
    }

    @Override
    public String concat(List<String> parts) {
        return "CONCAT(" + String.join(", ", parts) + ")";
    }

    @Override
    public String currentTimestampLiteral() {
        return "'2026-01-01T00:00:00Z'";
    }

    @Override
    public String schemaPrefix() {
        return "app.";
    }

    @Override
    public String schemaHeader() {
        return "CREATE SCHEMA IF NOT EXISTS app;\n"
                + "CREATE EXTENSION IF NOT EXISTS pgcrypto;\n"
                + "SET search_path TO app, public;\n";
    }

    private String mapStringType(EntitySchemaScanner.ColumnDefinition col) {
        String columnName = col.columnName;
        if (columnName.contains("url") || columnName.contains("uri") || columnName.contains("payload")) {
            return "text";
        }
        if (columnName.contains("description") || columnName.contains("content")) {
            return "text";
        }
        return "varchar(255)";
    }

    private String joinValues(List<String> values) {
        if (values.size() == 1) {
            return "(" + values.get(0) + ")";
        }
        return String.join(", ", values.stream().map(v -> "(" + v + ")").collect(Collectors.toList()));
    }
}
