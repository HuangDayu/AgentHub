package com.agenthub.domain.model.data_source;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据源字段
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceColumn {
    private String id;
    private String tableId;
    private String name;
    private String type;
    private boolean nullable;
    private boolean isPrimary;
    private String defaultValue;
    private String description;
    private boolean isPii;
    private String piiType;
    private int columnOrder;
}
