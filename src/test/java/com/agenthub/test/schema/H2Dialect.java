package com.agenthub.test.schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * H2 方言：双引号标识符、PG 模式兼容（timestamptz、ON CONFLICT）。
 */
public class H2Dialect implements SchemaDialect {

    @Override
    public String name() {
        return "h2";
    }

    @Override
    public String quoteIdentifier(String name) {
        return "\"" + name + "\"";
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
            return "TIMESTAMP WITH TIME ZONE";
        }
        if (javaType == LocalDate.class) {
            return "date";
        }
        if (javaType == byte[].class) {
            return "blob";
        }
        return "text";
    }

    @Override
    public String autoIncrementType() {
        return "BIGINT AUTO_INCREMENT";
    }

    @Override
    public String insertOrIgnore(SqlInsert insert) {
        StringBuilder sb = new StringBuilder();
        for (String row : insert.values()) {
            sb.append("INSERT INTO ").append(insert.table()).append(" (")
              .append(String.join(", ", insert.columns())).append(") SELECT ")
              .append(row).append(" FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM ")
              .append(insert.table()).append(" WHERE id = '")
              .append(extractIdFromValues(row)).append("');\n");
        }
        return sb.toString();
    }

    @Override
    public String insertOrUpdate(SqlInsert insert) {
        String setClause = insert.updateColumns().stream()
            .map(c -> "t." + c + " = s." + c)
            .collect(Collectors.joining(", "));
        return "MERGE INTO " + insert.table() + " t USING (VALUES " + joinValues(insert.values()) + ") s("
            + String.join(", ", insert.columns()) + ") ON t.id = s.id "
            + "WHEN MATCHED THEN UPDATE SET " + setClause + " "
            + "WHEN NOT MATCHED THEN INSERT (" + String.join(", ", insert.columns()) + ") "
            + "VALUES (" + insert.columns().stream().map(c -> "s." + c).collect(Collectors.joining(", ")) + ");";
    }

    @Override
    public String uuidFunction() {
        return "RANDOM_UUID()";
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
        return "-- H2 PG 模式由 JDBC URL 启用；不需要 CREATE SCHEMA / EXTENSION";
    }

    private String mapStringType(String columnName) {
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

    private String extractIdFromValues(String valuesRow) {
        int comma = valuesRow.indexOf(',');
        String first = comma > 0 ? valuesRow.substring(0, comma).trim() : valuesRow.trim();
        if (first.startsWith("'") && first.endsWith("'")) {
            return first.substring(1, first.length() - 1);
        }
        return first;
    }
}
