package com.agenthub.infrastructure.store.db.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

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
    private String content;
    private String metadata;
    private Instant timestamp;
    private Instant createdAt;
}
