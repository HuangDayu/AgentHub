package com.agenthub.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据源列请求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceColumnRequest {
    private String name;
    private String type;
    private String description;
    private Boolean nullable;
    private Boolean primaryKey;
    private String defaultValue;
}
