package com.agenthub.infrastructure.store.db.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 表关联实体
 */
@Data
@TableName("table_relationship")
public class TableRelationshipEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String sourceTableId;
    private String targetTableId;
    private String name;
    private String type;
    private String sourceColumn;
    private String targetColumn;
    private String description;
}
