package com.agenthub.test.schema;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.apache.ibatis.type.JdbcType;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * MyBatis-Plus Entity到PostgreSQL Schema生成器。
 */
@Deprecated
public class SchemaGenerator {

    private static final Logger log = LoggerFactory.getLogger(SchemaGenerator.class);

    /**
     * Schema文件头部模板
     */
    private static final String SCHEMA_HEADER = """
            -- =========================================================
            -- AgentHub - Auto-generated Schema
            -- Generated: %s
            -- Source: MyBatis-Plus Entity Classes
            -- =========================================================
            
            CREATE SCHEMA IF NOT EXISTS app;
            CREATE EXTENSION IF NOT EXISTS pgcrypto;
            SET search_path TO app, public;
            
            """;

    /**
     * Checklist文件头部模板
     */
    private static final String CHECKLIST_HEADER = """
            -- =========================================================
            -- Schema Change Checklist
            -- Generated: %s
            -- Description: Incremental changes from previous schema
            -- =========================================================
            
            BEGIN;
            
            """;

    /**
     * 主入口方法。
     */
    public static void main(String[] args) throws Exception {
        String basePackage = args.length > 0 ? args[0] : "com.agenthub";
        String schemaOutputPath = args.length > 1 ? args[1] : "sql/schema.sql";
        String checklistOutputPath = args.length > 2 ? args[2] : "sql/checklist.sql";
        SchemaGenerator generator = new SchemaGenerator();
        generator.generate(basePackage, schemaOutputPath, checklistOutputPath);
    }

    /**
     * 生成schema和checklist。
     */
    public void generate(String basePackage, String schemaOutputPath, String checklistOutputPath) throws Exception {
        log.info("Starting schema generation...");
        logGenerationParams(basePackage, schemaOutputPath, checklistOutputPath);
        List<Class<?>> entityClasses = scanEntities(basePackage);
        log.info("Found {} entity classes", entityClasses.size());
        String oldSchema = readOldSchema(schemaOutputPath);
        String newSchema = generateFullSchema(entityClasses);
        writeToFile(schemaOutputPath, newSchema);
        log.info("Generated schema.sql: {}", schemaOutputPath);
        generateChecklistIfNeeded(oldSchema, newSchema, checklistOutputPath);
        log.info("Schema generation completed successfully!");
    }

    /**
     * 记录生成参数。
     */
    private void logGenerationParams(String basePackage, String schemaOutputPath, String checklistOutputPath) {
        log.info("Base package: {}", basePackage);
        log.info("Schema output: {}", schemaOutputPath);
        log.info("Checklist output: {}", checklistOutputPath);
    }

    /**
     * 如果需要则生成checklist。
     */
    private void generateChecklistIfNeeded(String oldSchema, String newSchema, String checklistOutputPath)
            throws IOException {
        if (oldSchema == null || oldSchema.isEmpty()) {
            log.info("No previous schema found, skipping checklist generation");
            return;
        }
        String checklist = generateChecklist(oldSchema, newSchema);
        if (!checklist.trim().isEmpty()) {
            writeToFile(checklistOutputPath, checklist);
            log.info("Generated checklist.sql: {}", checklistOutputPath);
        } else {
            log.info("No schema changes detected");
        }
    }

    /**
     * 扫描指定包下的所有Entity类。
     */
    private List<Class<?>> scanEntities(String basePackage) throws Exception {
        List<Class<?>> entities = new ArrayList<>();
        log.info("Scanning package: {}", basePackage);
        try {
            Reflections reflections = new Reflections(basePackage);
            Set<Class<?>> classes = reflections.getTypesAnnotatedWith(TableName.class);
            entities.addAll(classes);
            logScannedEntities(entities);
        } catch (Exception e) {
            log.error("Failed to scan entities", e);
            throw e;
        }
        return entities;
    }

    /**
     * 记录扫描到的Entity。
     */
    private void logScannedEntities(List<Class<?>> entities) {
        for (Class<?> clazz : entities) {
            log.debug("Found entity: {}", clazz.getSimpleName());
        }
        log.info("Found {} entity classes", entities.size());
    }

    /**
     * 读取旧的schema.sql。
     */
    private String readOldSchema(String schemaOutputPath) {
        try {
            Path path = Paths.get(schemaOutputPath);
            if (Files.exists(path)) {
                return Files.readString(path);
            }
        } catch (IOException e) {
            log.warn("Failed to read old schema: {}", schemaOutputPath, e);
        }
        return null;
    }

    /**
     * 生成完整的schema.sql。
     */
    private String generateFullSchema(List<Class<?>> entityClasses) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(SCHEMA_HEADER, Instant.now()));
        List<TableDefinition> tables = parseAndSortTables(entityClasses);
        appendDropTables(sb, tables);
        appendCreateTables(sb, tables);
        appendIndexes(sb, tables);
        return sb.toString();
    }

    /**
     * 解析并排序表定义。
     */
    private List<TableDefinition> parseAndSortTables(List<Class<?>> entityClasses) {
        return entityClasses.stream()
                .map(this::parseEntity)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(t -> t.tableName))
                .collect(Collectors.toList());
    }

    /**
     * 追加DROP TABLE语句。
     */
    private void appendDropTables(StringBuilder sb, List<TableDefinition> tables) {
        sb.append("-- =========================================================\n");
        sb.append("-- Drop existing tables (dependency order)\n");
        sb.append("-- =========================================================\n");
        for (int i = tables.size() - 1; i >= 0; i--) {
            TableDefinition table = tables.get(i);
            sb.append(String.format("DROP TABLE IF EXISTS %s CASCADE;\n", table.tableName));
        }
        sb.append("\n");
    }

    /**
     * 追加CREATE TABLE语句。
     */
    private void appendCreateTables(StringBuilder sb, List<TableDefinition> tables) {
        sb.append("-- =========================================================\n");
        sb.append("-- Create tables\n");
        sb.append("-- =========================================================\n\n");
        for (TableDefinition table : tables) {
            sb.append(generateCreateTableSQL(table));
            sb.append("\n");
        }
    }

    /**
     * 追加索引语句。
     */
    private void appendIndexes(StringBuilder sb, List<TableDefinition> tables) {
        sb.append("-- =========================================================\n");
        sb.append("-- Create indexes\n");
        sb.append("-- =========================================================\n\n");
        for (TableDefinition table : tables) {
            String indexes = generateIndexes(table);
            if (!indexes.isEmpty()) {
                sb.append(indexes);
                sb.append("\n");
            }
        }
    }

    /**
     * 解析Entity类为表定义。
     */
    private TableDefinition parseEntity(Class<?> entityClass) {
        TableName tableName = entityClass.getAnnotation(TableName.class);
        if (tableName == null) {
            return null;
        }
        TableDefinition tableDef = createTableDefinition(entityClass, tableName);
        parseEntityFields(entityClass, tableDef);
        return tableDef;
    }

    /**
     * 创建表定义对象。
     */
    private TableDefinition createTableDefinition(Class<?> entityClass, TableName tableName) {
        TableDefinition tableDef = new TableDefinition();
        tableDef.entityClass = entityClass;
        tableDef.tableName = normalizeTableName(tableName.value());
        tableDef.comment = tableName.schema() + "." + tableName.value();
        return tableDef;
    }

    /**
     * 规范化表名。
     */
    private String normalizeTableName(String tableName) {
        if (tableName.startsWith("app.")) {
            return tableName.replace("app.", "");
        }
        return tableName;
    }

    /**
     * 解析Entity字段。
     */
    private void parseEntityFields(Class<?> entityClass, TableDefinition tableDef) {
        for (Field field : entityClass.getDeclaredFields()) {
            ColumnDefinition column = parseColumn(field);
            if (column != null) {
                tableDef.columns.add(column);
            }
        }
    }

    /**
     * 解析字段为列定义。
     */
    private ColumnDefinition parseColumn(Field field) {
        if (Modifier.isStatic(field.getModifiers())) {
            return null;
        }
        ColumnDefinition column = new ColumnDefinition();
        column.fieldName = field.getName();
        column.javaType = field.getType().getSimpleName();
        TableField tableField = field.getAnnotation(TableField.class);
        if (tableField != null && !tableField.value().isEmpty()) {
            column.columnName = tableField.value();
        } else {
            column.columnName = camelToSnake(field.getName());
        }
        if (tableField != null && !JdbcType.UNDEFINED.equals(tableField.jdbcType())) {
            column.sqlType = mapJdbcType(tableField.jdbcType());
        } else {
            column.sqlType = mapJavaTypeToPostgreSQL(field.getType(), column.columnName);
        }
        column.isPrimaryKey = field.getAnnotation(TableId.class) != null;
        column.nullable = !column.isPrimaryKey;
        return column;
    }

    private String mapJdbcType(JdbcType jdbcType) {
        return switch (jdbcType) {
            case VARCHAR -> "varchar(255)";
            case INTEGER -> "integer";
            case BIGINT -> "bigint";
            case BOOLEAN -> "boolean";
            case REAL -> "text";
            case DOUBLE -> "double precision";
            case FLOAT -> "real";
            case TIMESTAMP -> "timestamptz";
            case DATE -> "date";
            case NUMERIC -> "numeric(19,2)";
            case BLOB -> "bytea";
            case CLOB -> "text";
            case BINARY -> "bytea";
            case VARBINARY -> "bytea";
            case LONGVARBINARY -> "bytea";
            case TIME -> "time";
            case NCHAR -> "text";
            case NVARCHAR -> "text";
            case NCLOB -> "text";
            case SQLXML -> "text";
            case OTHER -> "text";
            case ARRAY -> "text";
            case REF -> "text";
            case DATALINK -> "text";
            case ROWID -> "text";
            case STRUCT -> "text";
            case NULL -> "text";
            case UNDEFINED -> "text";
            case DISTINCT -> "text";
            case JAVA_OBJECT -> "text";
            case CURSOR -> "text";
            case DATETIMEOFFSET -> null;
            case TIME_WITH_TIMEZONE -> "timestamptz";
            case TIMESTAMP_WITH_TIMEZONE -> "timestamptz";
            case BIT -> "boolean";
            case TINYINT -> "smallint";
            case SMALLINT -> "smallint";
            case DECIMAL -> "numeric(19,2)";
            case CHAR -> "char(1)";
            case LONGNVARCHAR -> "text";
            case LONGVARCHAR -> "text";
        };
    }


    /**
     * 生成CREATE TABLE SQL。
     */
    private String generateCreateTableSQL(TableDefinition table) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("-- Table: %s\n", table.tableName));
        sb.append(String.format("CREATE TABLE IF NOT EXISTS %s (\n", table.tableName));
        List<String> columnDefs = buildColumnDefinitions(table);
        sb.append(String.join(",\n", columnDefs));
        sb.append("\n);\n");
        return sb.toString();
    }

    /**
     * 构建列定义列表。
     */
    private List<String> buildColumnDefinitions(TableDefinition table) {
        List<String> columnDefs = new ArrayList<>();
        List<String> primaryKeys = new ArrayList<>();
        for (ColumnDefinition column : table.columns) {
            String colDef = buildColumnDefinition(column, primaryKeys);
            columnDefs.add(colDef);
        }
        addPrimaryKeyConstraint(columnDefs, primaryKeys);
        return columnDefs;
    }

    /**
     * 构建列定义字符串。
     */
    private String buildColumnDefinition(ColumnDefinition column, List<String> primaryKeys) {
        StringBuilder colDef = new StringBuilder();
        colDef.append("  ").append(column.columnName).append(" ").append(column.sqlType);
        if (column.isPrimaryKey) {
            primaryKeys.add(column.columnName);
        }
        if (!column.nullable) {
            colDef.append(" NOT NULL");
        }
        return colDef.toString();
    }

    /**
     * 添加主键约束。
     */
    private void addPrimaryKeyConstraint(List<String> columnDefs, List<String> primaryKeys) {
        if (!primaryKeys.isEmpty()) {
            columnDefs.add("  PRIMARY KEY (" + String.join(", ", primaryKeys) + ")");
        }
    }

    /**
     * 生成索引。
     */
    private String generateIndexes(TableDefinition table) {
        StringBuilder sb = new StringBuilder();
        for (ColumnDefinition column : table.columns) {
            if (shouldCreateIndex(column)) {
                appendIndexSQL(sb, table, column);
            }
        }
        return sb.toString();
    }

    /**
     * 判断是否需要创建索引。
     */
    private boolean shouldCreateIndex(ColumnDefinition column) {
        return column.columnName.endsWith("_id") && !column.isPrimaryKey;
    }

    /**
     * 追加索引SQL。
     */
    private void appendIndexSQL(StringBuilder sb, TableDefinition table, ColumnDefinition column) {
        String indexName = "idx_" + table.tableName + "_" + column.columnName;
        sb.append(String.format("CREATE INDEX IF NOT EXISTS %s ON %s(%s);\n",
                indexName, table.tableName, column.columnName));
    }

    /**
     * 生成增量checklist。
     */
    private String generateChecklist(String oldSchema, String newSchema) {
        StringBuilder sb = new StringBuilder();
        Map<String, String> oldTables = extractTables(oldSchema);
        Map<String, String> newTables = extractTables(newSchema);
        sb.append(String.format(CHECKLIST_HEADER, Instant.now()));
        boolean hasChanges = checkSchemaChanges(sb, oldTables, newTables);
        sb.append("COMMIT;\n");
        return hasChanges ? sb.toString() : "";
    }

    /**
     * 检查schema变更。
     */
    private boolean checkSchemaChanges(StringBuilder sb, Map<String, String> oldTables, Map<String, String> newTables) {
        boolean hasChanges = false;
        hasChanges |= checkNewTables(sb, oldTables, newTables);
        hasChanges |= checkModifiedTables(sb, oldTables, newTables);
        hasChanges |= checkDroppedTables(sb, oldTables, newTables);
        return hasChanges;
    }

    /**
     * 检查新增的表。
     */
    private boolean checkNewTables(StringBuilder sb, Map<String, String> oldTables, Map<String, String> newTables) {
        boolean hasChanges = false;
        for (String tableName : newTables.keySet()) {
            if (!oldTables.containsKey(tableName)) {
                sb.append(String.format("-- New table: %s\n", tableName));
                sb.append(newTables.get(tableName));
                sb.append("\n");
                hasChanges = true;
            }
        }
        return hasChanges;
    }

    /**
     * 检查修改的表。
     */
    private boolean checkModifiedTables(StringBuilder sb, Map<String, String> oldTables, Map<String, String> newTables) {
        boolean hasChanges = false;
        for (String tableName : newTables.keySet()) {
            if (oldTables.containsKey(tableName)) {
                hasChanges |= checkTableDiff(sb, tableName, oldTables, newTables);
            }
        }
        return hasChanges;
    }

    /**
     * 检查表差异。
     */
    private boolean checkTableDiff(StringBuilder sb, String tableName, Map<String, String> oldTables, Map<String, String> newTables) {
        String oldTableDef = oldTables.get(tableName);
        String newTableDef = newTables.get(tableName);
        if (!oldTableDef.equals(newTableDef)) {
            appendModifiedTableDiff(sb, tableName, oldTableDef, newTableDef);
            return true;
        }
        return false;
    }

    /**
     * 追加修改表的差异。
     */
    private void appendModifiedTableDiff(StringBuilder sb, String tableName, String oldTableDef, String newTableDef) {
        sb.append(String.format("-- Modified table: %s\n", tableName));
        sb.append("-- TODO: Review and apply necessary ALTER statements\n");
        sb.append("-- Old definition:\n");
        sb.append("-- ").append(oldTableDef.replace("\n", "\n-- ")).append("\n");
        sb.append("-- New definition:\n");
        sb.append(newTableDef).append("\n");
    }

    /**
     * 检查删除的表。
     */
    private boolean checkDroppedTables(StringBuilder sb, Map<String, String> oldTables, Map<String, String> newTables) {
        boolean hasChanges = false;
        for (String tableName : oldTables.keySet()) {
            if (!newTables.containsKey(tableName)) {
                sb.append(String.format("-- Dropped table: %s\n", tableName));
                sb.append(String.format("-- DROP TABLE IF EXISTS %s CASCADE;\n\n", tableName));
                hasChanges = true;
            }
        }
        return hasChanges;
    }

    /**
     * 从schema.sql中提取表定义。
     */
    private Map<String, String> extractTables(String schema) {
        Map<String, String> tables = new HashMap<>();
        Pattern pattern = Pattern.compile("CREATE TABLE IF NOT EXISTS (\\S+) \\((.*?)\\);", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(schema);
        while (matcher.find()) {
            String tableName = matcher.group(1);
            String tableDef = matcher.group(0);
            tables.put(tableName, tableDef);
        }
        return tables;
    }

    /**
     * Java类型映射到PostgreSQL类型。
     */
    private String mapJavaTypeToPostgreSQL(Class<?> javaType, String columnName) {
        String typeName = javaType.getSimpleName();
        if ("id".equals(columnName)) {
            return "varchar(64)";
        }
        return switch (typeName) {
            case "String" -> mapStringType(columnName);
            case "Integer", "int" -> "integer";
            case "Long", "long" -> "bigint";
            case "Boolean", "boolean" -> "boolean";
            case "Double", "double" -> "double precision";
            case "Float", "float" -> "real";
            case "Instant", "LocalDateTime", "ZonedDateTime" -> "timestamptz";
            case "LocalDate" -> "date";
            case "BigDecimal" -> "numeric(19,2)";
            case "UUID" -> "uuid";
            default -> mapDefaultType(typeName);
        };
    }

    /**
     * 映射String类型。
     */
    private String mapStringType(String columnName) {
        if (columnName.contains("url") || columnName.contains("uri")) {
            return "text";
        } else if (columnName.contains("description") || columnName.contains("content")) {
            return "text";
        }
        return "varchar(255)";
    }

    /**
     * 映射默认类型。
     */
    private String mapDefaultType(String typeName) {
        log.warn("Unknown Java type: {}, defaulting to text", typeName);
        return "text";
    }

    /**
     * 驼峰转下划线。
     */
    private String camelToSnake(String camelCase) {
        if (camelCase == null || camelCase.isEmpty()) {
            return camelCase;
        }
        StringBuilder result = new StringBuilder();
        result.append(Character.toLowerCase(camelCase.charAt(0)));
        appendSnakeCaseChars(camelCase, result);
        return result.toString();
    }

    /**
     * 追加下划线命名字符。
     */
    private void appendSnakeCaseChars(String camelCase, StringBuilder result) {
        for (int i = 1; i < camelCase.length(); i++) {
            char c = camelCase.charAt(i);
            if (Character.isUpperCase(c)) {
                result.append('_');
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
    }

    /**
     * 写入文件。
     */
    private void writeToFile(String filePath, String content) throws IOException {
        Path path = Paths.get(filePath);
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
        log.info("Written to: {}", filePath);
    }

    /**
     * 表定义。
     */
    static class TableDefinition {
        Class<?> entityClass;
        String tableName;
        String comment;
        List<ColumnDefinition> columns = new ArrayList<>();
    }

    /**
     * 列定义。
     */
    static class ColumnDefinition {
        String fieldName;
        String columnName;
        String javaType;
        String sqlType;
        boolean isPrimaryKey;
        boolean nullable;
    }
}
