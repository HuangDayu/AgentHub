package com.agenthub.infrastructure.store.db.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.Instant;

/**
 * 数据源表实体
 */
@Data
@TableName("data_source_table")
public class DataSourceTableEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String schemaId;
    private String name;
    private String displayName;
    private String description;
    private String allowedOperations;
    private String sampleDataJson;
    private Integer tableOrder;
    private Instant createdAt;
    private Instant updatedAt;
}
