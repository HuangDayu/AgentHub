package com.agenthub.infrastructure.persistence.db.entity;
import lombok.Data;

import com.baomidou.mybatisplus.annotation.*;

import java.time.Instant;
import java.util.UUID;

/**
 * 向量库配置数据库持久化对象。
 * <p>
 * 映射到 app.vector_store_config 表。
 * </p>
 */
@Data
@TableName("app.vector_store_config")
public class VectorStoreConfigEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    @TableField(value = "tenant_id",fill = FieldFill.INSERT)
    private String tenantId;
    @TableField(value = "workspace_id", fill = FieldFill.INSERT)
    private String workspaceId;
    private String name;
    private String type;
    private String host;
    private Integer port;
    private String apiKey;
    private String collectionName;
    private String extraParams;
    private Boolean enabled;
    @TableField(value = "created_at",fill = FieldFill.INSERT)
    private Instant createdAt;
    @TableField(value = "updated_at",fill = FieldFill.UPDATE)
    private Instant updatedAt;


}
