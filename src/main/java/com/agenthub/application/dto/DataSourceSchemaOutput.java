package com.agenthub.application.dto;

import com.agenthub.domain.model.data_source.DataSourceSchema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * 数据源 Schema 输出
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceSchemaOutput {
    private String id;
    private String dataSourceId;
    private String displayName;
    private String description;
    private boolean introspected;
    private Instant lastIntrospectedAt;
    private List<DataSourceTableOutput> tables;

    public static DataSourceSchemaOutput from(DataSourceSchema s) {
        if (s == null) return null;
        DataSourceSchemaOutput o = new DataSourceSchemaOutput();
        cn.hutool.core.bean.BeanUtil.copyProperties(s, o);
        o.setTables(DataSourceTableOutput.fromList(s.getTables()));
        return o;
    }
}
