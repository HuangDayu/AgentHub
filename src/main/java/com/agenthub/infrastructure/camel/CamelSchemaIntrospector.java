package com.agenthub.infrastructure.camel;

import com.agenthub.domain.model.AgentDataSource;
import com.agenthub.domain.model.DataSourceColumn;
import com.agenthub.domain.model.DataSourceSchema;
import com.agenthub.domain.model.DataSourceTable;
import com.agenthub.domain.model.TableRelationship;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * JDBC Schema 自动发现器
 * <p>通过 DatabaseMetaData 查询数据源的表结构元数据。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CamelSchemaIntrospector {
    private final CamelAgentDataSourceAdapter adapter;

    /**
     * 自动发现数据源的表结构
     */
    public DataSourceSchema introspect(AgentDataSource source) {
        DataSource ds = adapter.getRegisteredDataSource(source.getId());
        if (ds == null) {
            throw new IllegalStateException("数据源未注册，请先启用");
        }
        return doIntrospect(ds, source);
    }

    private DataSourceSchema doIntrospect(DataSource ds, AgentDataSource source) {
        try (Connection conn = ds.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            String schema = conn.getSchema();
            List<DataSourceTable> tables = discoverTables(meta, schema);
            return buildSchema(source, tables);
        } catch (SQLException e) {
            throw new RuntimeException("Schema 自动发现失败: " + e.getMessage(), e);
        }
    }

    private List<DataSourceTable> discoverTables(DatabaseMetaData meta, String schema)
            throws SQLException {
        List<DataSourceTable> tables = new ArrayList<>();
        try (ResultSet rs = meta.getTables(null, schema, "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                tables.add(buildTable(meta, schema, rs.getString("TABLE_NAME")));
            }
        }
        return tables;
    }

    private DataSourceTable buildTable(DatabaseMetaData meta, String schema, String tableName)
            throws SQLException {
        DataSourceTable table = new DataSourceTable();
        table.setId(UUID.randomUUID().toString());
        table.setName(tableName);
        table.setDisplayName(tableName);
        table.setColumns(discoverColumns(meta, schema, tableName));
        table.setRelationships(discoverRelationships(meta, schema, tableName));
        return table;
    }

    private List<DataSourceColumn> discoverColumns(DatabaseMetaData meta, String schema,
                                                    String tableName) throws SQLException {
        Set<String> pks = discoverPrimaryKeys(meta, schema, tableName);
        List<DataSourceColumn> columns = new ArrayList<>();
        int order = 0;
        try (ResultSet rs = meta.getColumns(null, schema, tableName, "%")) {
            while (rs.next()) {
                columns.add(buildColumn(rs, pks, order++));
            }
        }
        return columns;
    }

    private DataSourceColumn buildColumn(ResultSet rs, Set<String> pks, int order)
            throws SQLException {
        DataSourceColumn col = new DataSourceColumn();
        col.setId(UUID.randomUUID().toString());
        populateColumn(col, rs, pks);
        col.setColumnOrder(order);
        return col;
    }

    /** 填充列属性。 */
    private void populateColumn(DataSourceColumn col, ResultSet rs, Set<String> pks) throws SQLException {
        col.setName(rs.getString("COLUMN_NAME"));
        col.setType(rs.getString("TYPE_NAME"));
        col.setNullable("YES".equals(rs.getString("IS_NULLABLE")));
        col.setDefaultValue(rs.getString("COLUMN_DEF"));
        col.setPrimary(pks.contains(col.getName()));
    }

    private Set<String> discoverPrimaryKeys(DatabaseMetaData meta, String schema,
                                             String tableName) throws SQLException {
        Set<String> keys = new java.util.HashSet<>();
        try (ResultSet rs = meta.getPrimaryKeys(null, schema, tableName)) {
            while (rs.next()) {
                keys.add(rs.getString("COLUMN_NAME"));
            }
        }
        return keys;
    }

    private List<TableRelationship> discoverRelationships(DatabaseMetaData meta,
                                                           String schema, String tableName) {
        List<TableRelationship> rels = new ArrayList<>();
        try (ResultSet rs = meta.getImportedKeys(null, schema, tableName)) {
            collectRelationships(rs, rels);
        } catch (SQLException e) {
            log.warn("failed to discover FKs for {}.{}", schema, tableName);
        }
        return rels;
    }

    private void collectRelationships(ResultSet rs, List<TableRelationship> rels)
            throws SQLException {
        while (rs.next()) {
            rels.add(buildRelationship(rs));
        }
    }

    private TableRelationship buildRelationship(ResultSet rs) throws SQLException {
        TableRelationship rel = new TableRelationship();
        rel.setId(UUID.randomUUID().toString());
        rel.setName(rs.getString("FK_NAME"));
        rel.setSourceColumn(rs.getString("FKCOLUMN_NAME"));
        rel.setTargetTableId(rs.getString("PKTABLE_NAME"));
        rel.setTargetColumn(rs.getString("PKCOLUMN_NAME"));
        rel.setType("MANY_TO_ONE");
        return rel;
    }

    private DataSourceSchema buildSchema(AgentDataSource source, List<DataSourceTable> tables) {
        DataSourceSchema schema = new DataSourceSchema();
        schema.setId(UUID.randomUUID().toString());
        populateSchema(source, tables, schema);
        return schema;
    }

    /** 填充 schema 属性。 */
    private void populateSchema(AgentDataSource source, List<DataSourceTable> tables, DataSourceSchema schema) {
        schema.setTenantId(source.getTenantId());
        schema.setWorkspaceId(source.getWorkspaceId());
        schema.setDataSourceId(source.getId());
        schema.setDisplayName("Auto-discovered from " + source.getName());
        schema.setIntrospected(true);
        schema.setLastIntrospectedAt(java.time.Instant.now());
        schema.setTables(tables);
    }
}
