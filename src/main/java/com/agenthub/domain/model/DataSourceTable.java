package com.agenthub.domain.model;

import com.agenthub.domain.enums.TableOperation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

/**
 * 数据源表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceTable {
    private String id;
    private String schemaId;
    private String name;
    private String displayName;
    private String description;
    private Set<TableOperation> allowedOperations;
    private String sampleDataJson;
    private int tableOrder;
    private List<DataSourceColumn> columns;
    private List<TableRelationship> relationships;
}
