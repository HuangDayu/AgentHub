package com.agenthub.application.dto;

import com.agenthub.domain.model.DataSourceColumn;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据源字段输出
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceColumnOutput {
    private String id;
    private String name;
    private String type;
    private boolean nullable;
    private boolean isPrimary;
    private String defaultValue;
    private String description;
    private boolean isPii;
    private String piiType;
    private int columnOrder;

    public static DataSourceColumnOutput from(DataSourceColumn c) {
        if (c == null) return null;
        return new DataSourceColumnOutput(
            c.getId(), c.getName(), c.getType(), c.isNullable(),
            c.isPrimary(), c.getDefaultValue(), c.getDescription(),
            c.isPii(), c.getPiiType(), c.getColumnOrder()
        );
    }
}
