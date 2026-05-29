package com.agenthub.infrastructure.store.db.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.Instant;

/**
 * 子会话持久化对象。
 */
@Data
@TableName("subsession")
public class SubsessionEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String parentSessionId;
    private String subagentId;
    private String name;
    private String status;
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;
    @TableField(value = "updated_at", fill = FieldFill.INSERT)
    private Instant updatedAt;
}
