package com.agenthub.infrastructure.store.db.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;

import java.time.Instant;

/**
 * 消息推送实体.
 */
@Data
@TableName("message_pushes")
public class MessagePushEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String messageId;
    private String runId;
    private String role;
    @TableField(jdbcType = JdbcType.LONGVARCHAR)
    private String content;
    @TableField(jdbcType = JdbcType.LONGVARCHAR)
    private String metadata;
    private Instant timestamp;
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;
}
