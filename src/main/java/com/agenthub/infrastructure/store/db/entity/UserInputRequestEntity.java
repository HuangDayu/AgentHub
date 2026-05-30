package com.agenthub.infrastructure.store.db.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.Instant;

/**
 * 用户输入请求实体.
 */
@Data
@TableName("user_input_requests")
public class UserInputRequestEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String requestId;
    private String runId;
    private String agentId;
    private String agentName;
    private String structuredInput;
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;
}
