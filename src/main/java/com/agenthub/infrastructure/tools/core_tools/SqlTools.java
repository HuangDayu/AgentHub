package com.agenthub.infrastructure.tools.core_tools;

import com.agenthub.infrastructure.tools.annotations.AgentTools;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * SQL 工具，提供数据库表查询和只读 SQL 执行能力。
 */
@AgentTools(name = "SqlTools", description = "SQL 工具：查询表结构、执行只读SQL查询", defaultEnable = false)
public class SqlTools {

    private final JdbcTemplate jdbcTemplate;
    private static final Pattern SAFE_TABLE_NAME = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");
    private static final Set<String> WRITE_KEYWORDS = Set.of(
            "INSERT", "UPDATE", "DELETE", "DROP", "ALTER", "CREATE",
            "TRUNCATE", "GRANT", "REVOKE", "EXEC", "EXECUTE"
    );

    public SqlTools(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Tool(
            name = "sql_db_list_tables",
            description = "列出数据库中所有表名")
    public String listTables(String ignored) {
        List<String> tables = jdbcTemplate.queryForList(
                "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC' AND TABLE_TYPE = 'BASE TABLE'",
                String.class);
        return String.join(", ", tables);
    }

    @Tool(
            name = "sql_db_schema",
            description = "获取指定表的结构和示例数据")
    public String getSchema(String tableNames) {
        StringBuilder sb = new StringBuilder();
        for (String table : tableNames.split(",")) {
            String t = table.trim();
            if (t.isEmpty() || !isSafeTableName(t)) continue;
            appendTableSchema(sb, t);
        }
        return sb.toString();
    }

    @Tool(
            name = "sql_db_query",
            description = "执行只读SQL查询（仅允许SELECT）")
    public String runQuery(String query) {
        if (isWriteQuery(query)) return "Error: 只允许 SELECT 查询";
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(query);
            return rows.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private boolean isSafeTableName(String name) {
        return SAFE_TABLE_NAME.matcher(name).matches();
    }

    private boolean isWriteQuery(String query) {
        String upper = query.toUpperCase();
        return WRITE_KEYWORDS.stream().anyMatch(upper::contains);
    }

    private void appendTableSchema(StringBuilder sb, String tableName) {
        try {
            List<Map<String, Object>> rows = fetchSampleRows(tableName);
            List<Map<String, Object>> columns = fetchColumns(tableName);
            appendCreateTableDdl(sb, tableName, columns);
            appendSampleRows(sb, rows);
        } catch (Exception e) {
            sb.append("Error for table ").append(tableName).append(": ").append(e.getMessage()).append("\n");
        }
    }

    private List<Map<String, Object>> fetchSampleRows(String tableName) {
        return jdbcTemplate.queryForList("SELECT * FROM " + tableName + " LIMIT 3");
    }

    private List<Map<String, Object>> fetchColumns(String tableName) {
        return jdbcTemplate.queryForList(
                "SELECT COLUMN_NAME, TYPE_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ?",
                tableName.toUpperCase());
    }

    private void appendCreateTableDdl(StringBuilder sb, String tableName, List<Map<String, Object>> columns) {
        sb.append("CREATE TABLE \"").append(tableName).append("\" (");
        sb.append(columns.stream()
                .map(c -> "\"" + c.get("COLUMN_NAME") + "\" " + c.get("TYPE_NAME"))
                .collect(Collectors.joining(", ")));
        sb.append(")\n\n");
    }

    private void appendSampleRows(StringBuilder sb, List<Map<String, Object>> rows) {
        if (rows.isEmpty()) {
            sb.append("\n");
            return;
        }
        sb.append("Sample rows:\n");
        rows.forEach(r -> sb.append(r.toString()).append("\n"));
    }
}
