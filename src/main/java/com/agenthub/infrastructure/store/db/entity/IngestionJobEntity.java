package com.agenthub.infrastructure.store.db.entity;
import lombok.Data;

import com.baomidou.mybatisplus.annotation.*;

import java.time.Instant;

/**
 * 入库任务数据库实体。
 * <p>
 * 映射到 ingestion_job 表，存储任务的状态和元数据。
 * </p>
 */
@Data
@TableName("ingestion_job")
public class IngestionJobEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    @TableField(value = "tenant_id",fill = FieldFill.INSERT)
    private String tenantId;
    @TableField(value = "workspace_id",fill = FieldFill.INSERT)
    private String workspaceId;
    private String kbId;
    private String documentId;
    private String triggerType;
    private String status;
    private Integer progress;
    private String parserName;
    private String embeddingModel;
    private Integer indexVersion;
    private Integer documentCount;
    private String errorCode;
    private String errorMessage;
    private Instant startedAt;
    private Instant endedAt;
    @TableField(value = "created_at",fill = FieldFill.INSERT)
    private Instant createdAt;
    @TableField(value = "updated_at",fill = FieldFill.UPDATE)
    private Instant updatedAt;


}
