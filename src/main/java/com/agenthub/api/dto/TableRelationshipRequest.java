package com.agenthub.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 表关系请求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableRelationshipRequest {
    private String name;
    private String sourceTableId;
    private String targetTableId;
    private String type;
    private String sourceColumn;
    private String targetColumn;
}
