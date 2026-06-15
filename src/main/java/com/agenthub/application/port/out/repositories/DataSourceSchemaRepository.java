package com.agenthub.application.port.out.repositories;

import com.agenthub.domain.model.datasource.DataSourceSchema;

import java.util.Optional;

/**
 * 数据源 Schema 仓储端口
 */
public interface DataSourceSchemaRepository {
    DataSourceSchema save(DataSourceSchema schema);
    Optional<DataSourceSchema> findById(String id);
    Optional<DataSourceSchema> findByDataSourceId(String dataSourceId);
    void deleteById(String id);
}
