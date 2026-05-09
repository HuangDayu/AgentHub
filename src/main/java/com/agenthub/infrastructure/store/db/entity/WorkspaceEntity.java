package com.agenthub.infrastructure.store.db.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.Instant;

/**
 * 工作区持久化实体。
 * 对应数据库表 app.workspace。
 */
@Data
@TableName("app.workspace")
public class WorkspaceEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    @TableField(value = "tenant_id", fill = FieldFill.INSERT)
    private String tenantId;
    private String workspaceCode;
    private String name;
    private String region;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;


}
