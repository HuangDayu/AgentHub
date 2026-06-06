package com.agenthub.infrastructure.store.db.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 数据源字段实体
 */
@Data
@TableName("data_source_column")
public class DataSourceColumnEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String tableId;
    private String name;
    private String type;
    private Boolean nullable;
    private Boolean isPrimary;
    private String defaultValue;
    private String description;
    private Boolean isPii;
    private String piiType;
    private Integer columnOrder;
}
