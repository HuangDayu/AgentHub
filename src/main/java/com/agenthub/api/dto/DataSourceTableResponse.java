package com.agenthub.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceTableResponse {
    private String id;
    private String name;
    private String displayName;
    private String description;
    private Set<String> allowedOperations;
    private String sampleDataJson;
    private int tableOrder;
    private List<DataSourceColumnResponse> columns;
    private List<TableRelationshipResponse> relationships;
}
