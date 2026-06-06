package com.agenthub.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableRelationshipResponse {
    private String id;
    private String sourceTableId;
    private String targetTableId;
    private String name;
    private String type;
    private String sourceColumn;
    private String targetColumn;
    private String description;
}
