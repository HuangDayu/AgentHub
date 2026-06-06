package com.agenthub.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceSchemaResponse {
    private String id;
    private String dataSourceId;
    private String displayName;
    private String description;
    private boolean introspected;
    private java.time.Instant lastIntrospectedAt;
    private List<DataSourceTableResponse> tables;
}
