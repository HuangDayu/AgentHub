package com.agenthub.application.dto;

import com.agenthub.domain.enums.TableOperation;
import com.agenthub.domain.model.DataSourceTable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

/**
 * 数据源表输出
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceTableOutput {
    private String id;
    private String name;
    private String displayName;
    private String description;
    private Set<TableOperation> allowedOperations;
    private String sampleDataJson;
    private int tableOrder;
    private List<DataSourceColumnOutput> columns;
    private List<TableRelationshipOutput> relationships;

    public static DataSourceTableOutput from(DataSourceTable t) {
        if (t == null) return null;
        return new DataSourceTableOutput(
            t.getId(), t.getName(), t.getDisplayName(), t.getDescription(),
            t.getAllowedOperations(), t.getSampleDataJson(), t.getTableOrder(),
            null, null
        );
    }

    public static java.util.List<DataSourceTableOutput> fromList(java.util.List<DataSourceTable> tables) {
        if (tables == null) return null;
        return tables.stream().map(DataSourceTableOutput::from).toList();
    }
}
