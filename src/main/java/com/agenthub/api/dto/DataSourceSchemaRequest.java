package com.agenthub.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 数据源 schema 请求 DTO（用于 PUT /schema、POST /introspect 等接口）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceSchemaRequest {
    private String version;
    private List<DataSourceTableRequest> tables;
    private List<TableRelationshipRequest> relationships;
}
