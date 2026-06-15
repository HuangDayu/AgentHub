package com.agenthub.application.dto;

import com.agenthub.domain.model.datasource.TableRelationship;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 表关联输出
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableRelationshipOutput {
    private String id;
    private String sourceTableId;
    private String targetTableId;
    private String name;
    private String type;
    private String sourceColumn;
    private String targetColumn;
    private String description;

    public static TableRelationshipOutput from(TableRelationship r) {
        if (r == null) return null;
        return new TableRelationshipOutput(
            r.getId(), r.getSourceTableId(), r.getTargetTableId(),
            r.getName(), r.getType(), r.getSourceColumn(), r.getTargetColumn(),
            r.getDescription()
        );
    }
}
