package com.agenthub.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 数据源表请求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceTableRequest {
    private String id;
    private String name;
    private String displayName;
    private String description;
    private List<DataSourceColumnRequest> columns;
}
