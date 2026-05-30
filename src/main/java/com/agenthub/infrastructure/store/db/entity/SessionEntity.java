package com.agenthub.infrastructure.store.db.entity;
import lombok.Data;

import com.baomidou.mybatisplus.annotation.*;

import java.time.Instant;

/**
 * 会话持久化对象。
 */
@Data
@TableName("session")
public class SessionEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String agentId;
    private String name;
    @TableField(value = "tenant_id",fill = FieldFill.INSERT)
    private String tenantId;
    @TableField(value = "workspace_id", fill = FieldFill.INSERT)
    private String workspaceId;
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;

}
