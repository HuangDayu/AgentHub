package com.agenthub.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 表关联关系
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableRelationship {
    private String id;
    private String sourceTableId;
    private String targetTableId;
    private String name;
    private String type;
    private String sourceColumn;
    private String targetColumn;
    private String description;
}
