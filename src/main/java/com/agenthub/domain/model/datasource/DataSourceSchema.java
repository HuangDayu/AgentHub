package com.agenthub.domain.model.datasource;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * 数据源 Schema 元数据（表/字段/关联）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceSchema {
    private String id;
    private String tenantId;
    private String workspaceId;
    private String dataSourceId;
    private String displayName;
    private String description;
    private boolean introspected;
    private Instant lastIntrospectedAt;
    private List<DataSourceTable> tables;
    private Instant createdAt;
    private Instant updatedAt;
}
