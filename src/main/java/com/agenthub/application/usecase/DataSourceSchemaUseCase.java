package com.agenthub.application.usecase;

import com.agenthub.application.dto.DataSourceSchemaOutput;
import com.agenthub.application.port.out.AgentDataSourcePort;
import com.agenthub.application.port.out.repositories.AgentDataSourceRepository;
import com.agenthub.application.port.out.repositories.DataSourceSchemaRepository;
import com.agenthub.domain.enums.AgentDataSourceProtocol;
import com.agenthub.domain.exception.AgentDataSourceNotFoundException;
import com.agenthub.domain.exception.AgentDataSourceValidationException;
import com.agenthub.domain.model.AgentDataSource;
import com.agenthub.domain.model.DataSourceColumn;
import com.agenthub.domain.model.DataSourceSchema;
import com.agenthub.domain.model.DataSourceTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 数据源 Schema 用例 - 手动配置 + 自动发现
 */
@Component
@RequiredArgsConstructor
public class DataSourceSchemaUseCase {
    private final DataSourceSchemaRepository schemaRepository;
    private final AgentDataSourceRepository sourceRepository;
    private final AgentDataSourcePort port;

    /**
     * 获取数据源 Schema（含表/字段/关联）
     */
    public DataSourceSchemaOutput get(String dataSourceId) {
        DataSourceSchema schema = schemaRepository.findByDataSourceId(dataSourceId)
            .orElseGet(() -> createNewSchema(dataSourceId, requireSource(dataSourceId)));
        return DataSourceSchemaOutput.from(schema);
    }

    /**
     * 整体替换 Schema（手动配置）
     */
    public DataSourceSchemaOutput replace(String dataSourceId, DataSourceSchema schema) {
        AgentDataSource source = requireSource(dataSourceId);
        DataSourceSchema existing = schemaRepository.findByDataSourceId(dataSourceId)
            .orElseGet(() -> createNewSchema(dataSourceId, source));
        applyReplace(existing, schema);
        DataSourceSchema saved = schemaRepository.save(existing);
        source.setSchemaId(saved.getId());
        sourceRepository.save(source);
        return DataSourceSchemaOutput.from(saved);
    }

    /**
     * 自动发现（通过 Camel JDBC 查 INFORMATION_SCHEMA）
     */
    public DataSourceSchemaOutput introspect(String dataSourceId) {
        AgentDataSource source = requireSource(dataSourceId);
        requireJdbc(source);
        DataSourceSchema saved = saveIntrospected(source, port.introspect(source), dataSourceId);
        return DataSourceSchemaOutput.from(saved);
    }

    /**
     * 新增表
     */
    public DataSourceSchemaOutput addTable(String dataSourceId, DataSourceTable table) {
        DataSourceSchema schema = schemaRepository.findByDataSourceId(dataSourceId)
            .orElseGet(() -> createNewSchema(dataSourceId, requireSource(dataSourceId)));
        assignIds(table, schema);
        schema.getTables().add(table);
        schema.setUpdatedAt(Instant.now());
        return DataSourceSchemaOutput.from(schemaRepository.save(schema));
    }

    /**
     * 更新表
     */
    public DataSourceSchemaOutput updateTable(String dataSourceId, String tableId, DataSourceTable table) {
        DataSourceSchema schema = requireSchema(dataSourceId);
        int idx = findTableIndex(schema, tableId);
        if (idx < 0) throw new AgentDataSourceNotFoundException("table not found: " + tableId);
        table.setId(tableId);
        table.setSchemaId(schema.getId());
        schema.getTables().set(idx, table);
        schema.setUpdatedAt(Instant.now());
        return DataSourceSchemaOutput.from(schemaRepository.save(schema));
    }

    /**
     * 删除表
     */
    public void deleteTable(String dataSourceId, String tableId) {
        DataSourceSchema schema = requireSchema(dataSourceId);
        schema.getTables().removeIf(t -> t.getId().equals(tableId));
        schema.setUpdatedAt(Instant.now());
        schemaRepository.save(schema);
    }

    private void applyReplace(DataSourceSchema existing, DataSourceSchema schema) {
        existing.setDisplayName(schema.getDisplayName());
        existing.setDescription(schema.getDescription());
        existing.setIntrospected(false);
        existing.setTables(schema.getTables());
        existing.setUpdatedAt(Instant.now());
    }

    private void requireJdbc(AgentDataSource source) {
        if (source.getProtocol() != AgentDataSourceProtocol.JDBC
            && source.getProtocol() != AgentDataSourceProtocol.SQL) {
            throw new AgentDataSourceValidationException(
                "introspect only supports JDBC/SQL data sources, got: " + source.getProtocol());
        }
    }

    private DataSourceSchema saveIntrospected(AgentDataSource source, DataSourceSchema discovered, String dataSourceId) {
        discovered.setDataSourceId(dataSourceId);
        discovered.setIntrospected(true);
        discovered.setLastIntrospectedAt(Instant.now());
        DataSourceSchema saved = schemaRepository.save(discovered);
        source.setSchemaId(saved.getId());
        sourceRepository.save(source);
        return saved;
    }

    private void assignIds(DataSourceTable table, DataSourceSchema schema) {
        if (table.getId() == null || table.getId().isBlank()) {
            table.setId(UUID.randomUUID().toString());
        }
        table.setSchemaId(schema.getId());
        if (table.getColumns() == null) return;
        for (DataSourceColumn col : table.getColumns()) assignColumnId(col, table.getId());
    }

    private void assignColumnId(DataSourceColumn col, String tableId) {
        if (col.getId() == null || col.getId().isBlank()) {
            col.setId(UUID.randomUUID().toString());
        }
        col.setTableId(tableId);
    }

    private int findTableIndex(DataSourceSchema schema, String tableId) {
        List<DataSourceTable> tables = schema.getTables();
        for (int i = 0; i < tables.size(); i++) {
            if (tables.get(i).getId().equals(tableId)) return i;
        }
        return -1;
    }

    private DataSourceSchema createNewSchema(String dataSourceId, AgentDataSource source) {
        DataSourceSchema schema = new DataSourceSchema();
        schema.setId(UUID.randomUUID().toString());
        schema.setTenantId(source.getTenantId());
        schema.setWorkspaceId(source.getWorkspaceId());
        schema.setDataSourceId(dataSourceId);
        schema.setTables(new ArrayList<>());
        schema.setCreatedAt(Instant.now());
        return schema;
    }

    private DataSourceSchema requireSchema(String dataSourceId) {
        return schemaRepository.findByDataSourceId(dataSourceId)
            .orElseThrow(() -> new AgentDataSourceNotFoundException("schema not found: " + dataSourceId));
    }

    private AgentDataSource requireSource(String id) {
        return sourceRepository.findById(id)
            .orElseThrow(() -> new AgentDataSourceNotFoundException(id));
    }
}
