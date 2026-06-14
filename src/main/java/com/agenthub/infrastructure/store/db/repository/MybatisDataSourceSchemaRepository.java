package com.agenthub.infrastructure.store.db.repository;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.application.port.out.repositories.DataSourceSchemaRepository;
import com.agenthub.domain.model.data_source.DataSourceColumn;
import com.agenthub.domain.model.data_source.DataSourceSchema;
import com.agenthub.domain.model.data_source.DataSourceTable;
import com.agenthub.infrastructure.store.db.entity.DataSourceColumnEntity;
import com.agenthub.infrastructure.store.db.entity.DataSourceSchemaEntity;
import com.agenthub.infrastructure.store.db.entity.DataSourceTableEntity;
import com.agenthub.infrastructure.store.db.mapper.DataSourceColumnMybatisMapper;
import com.agenthub.infrastructure.store.db.mapper.DataSourceSchemaMybatisMapper;
import com.agenthub.infrastructure.store.db.mapper.DataSourceTableMybatisMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 数据源 Schema MyBatis 仓储（包含表/字段）
 */
@Repository
@Primary
public class MybatisDataSourceSchemaRepository implements DataSourceSchemaRepository {
    private final DataSourceSchemaMybatisMapper schemaMapper;
    private final DataSourceTableMybatisMapper tableMapper;
    private final DataSourceColumnMybatisMapper columnMapper;

    public MybatisDataSourceSchemaRepository(DataSourceSchemaMybatisMapper schemaMapper,
                                             DataSourceTableMybatisMapper tableMapper,
                                             DataSourceColumnMybatisMapper columnMapper) {
        this.schemaMapper = schemaMapper;
        this.tableMapper = tableMapper;
        this.columnMapper = columnMapper;
    }

    @Override
    public DataSourceSchema save(DataSourceSchema schema) {
        upsertSchemaRow(schema);
        persistTables(schema);
        return findById(schema.getId()).orElseThrow();
    }

    @Override
    public Optional<DataSourceSchema> findById(String id) {
        return Optional.ofNullable(schemaMapper.selectById(id)).map(this::toDomainWithRelations);
    }

    @Override
    public Optional<DataSourceSchema> findByDataSourceId(String dataSourceId) {
        LambdaQueryWrapper<DataSourceSchemaEntity> q = new LambdaQueryWrapper<>();
        q.eq(DataSourceSchemaEntity::getDataSourceId, dataSourceId);
        return Optional.ofNullable(schemaMapper.selectOne(q)).map(this::toDomainWithRelations);
    }

    @Override
    public void deleteById(String id) {
        deleteColumnsBySchema(id);
        tableMapper.delete(buildTablesQueryBySchema(id));
        schemaMapper.deleteById(id);
    }

    private void upsertSchemaRow(DataSourceSchema schema) {
        DataSourceSchemaEntity e = toSchemaEntity(schema);
        if (recordMissing(e.getId(), schemaMapper.selectById(e.getId()))) {
            schemaMapper.insert(e);
        } else {
            schemaMapper.updateById(e);
        }
    }

    private void persistTables(DataSourceSchema schema) {
        if (schema.getTables() == null) return;
        for (DataSourceTable table : schema.getTables()) {
            saveTable(table);
        }
    }

    private void saveTable(DataSourceTable table) {
        DataSourceTableEntity te = toTableEntity(table);
        if (recordMissing(te.getId(), tableMapper.selectById(te.getId()))) {
            tableMapper.insert(te);
        } else {
            tableMapper.updateById(te);
        }
        persistColumns(table);
    }

    private void persistColumns(DataSourceTable table) {
        if (table.getColumns() == null) return;
        for (DataSourceColumn col : table.getColumns()) {
            saveColumn(col, table.getId());
        }
    }

    private void saveColumn(DataSourceColumn col, String tableId) {
        DataSourceColumnEntity ce = toColumnEntity(col);
        ce.setTableId(tableId);
        if (recordMissing(ce.getId(), columnMapper.selectById(ce.getId()))) {
            columnMapper.insert(ce);
        } else {
            columnMapper.updateById(ce);
        }
    }

    private boolean recordMissing(String id, Object existing) {
        return id == null || id.isBlank() || existing == null;
    }

    private void deleteColumnsBySchema(String schemaId) {
        for (DataSourceTableEntity t : selectTablesBySchema(schemaId)) {
            LambdaQueryWrapper<DataSourceColumnEntity> qc = new LambdaQueryWrapper<>();
            qc.eq(DataSourceColumnEntity::getTableId, t.getId());
            columnMapper.delete(qc);
        }
    }

    private List<DataSourceTableEntity> selectTablesBySchema(String schemaId) {
        return tableMapper.selectList(buildTablesQueryBySchema(schemaId));
    }

    private LambdaQueryWrapper<DataSourceTableEntity> buildTablesQueryBySchema(String schemaId) {
        LambdaQueryWrapper<DataSourceTableEntity> qt = new LambdaQueryWrapper<>();
        qt.eq(DataSourceTableEntity::getSchemaId, schemaId);
        return qt;
    }

    private DataSourceSchema toDomainWithRelations(DataSourceSchemaEntity e) {
        DataSourceSchema s = new DataSourceSchema();
        BeanUtil.copyProperties(e, s);
        s.setTables(loadTables(e.getId()));
        return s;
    }

    private List<DataSourceTable> loadTables(String schemaId) {
        List<DataSourceTable> tables = new ArrayList<>();
        for (DataSourceTableEntity te : tableMapper.selectList(buildTablesQueryBySchema(schemaId))) {
            tables.add(toTableWithColumns(te));
        }
        return tables;
    }

    private DataSourceTable toTableWithColumns(DataSourceTableEntity te) {
        DataSourceTable t = new DataSourceTable();
        BeanUtil.copyProperties(te, t);
        t.setColumns(loadColumns(t.getId()));
        return t;
    }

    private List<DataSourceColumn> loadColumns(String tableId) {
        LambdaQueryWrapper<DataSourceColumnEntity> qc = new LambdaQueryWrapper<>();
        qc.eq(DataSourceColumnEntity::getTableId, tableId);
        List<DataSourceColumn> cols = new ArrayList<>();
        for (DataSourceColumnEntity ce : columnMapper.selectList(qc)) {
            cols.add(toColumn(ce));
        }
        return cols;
    }

    private DataSourceColumn toColumn(DataSourceColumnEntity ce) {
        DataSourceColumn c = new DataSourceColumn();
        BeanUtil.copyProperties(ce, c);
        return c;
    }

    private DataSourceSchemaEntity toSchemaEntity(DataSourceSchema s) {
        DataSourceSchemaEntity e = new DataSourceSchemaEntity();
        BeanUtil.copyProperties(s, e);
        return e;
    }

    private DataSourceTableEntity toTableEntity(DataSourceTable t) {
        DataSourceTableEntity e = new DataSourceTableEntity();
        BeanUtil.copyProperties(t, e);
        return e;
    }

    private DataSourceColumnEntity toColumnEntity(DataSourceColumn c) {
        DataSourceColumnEntity e = new DataSourceColumnEntity();
        BeanUtil.copyProperties(c, e);
        return e;
    }
}
