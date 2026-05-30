package com.agenthub.infrastructure.store.db.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.Instant;

/**
 * Run注册实体.
 */
@Data
@TableName("run_registrations")
public class RunRegistrationEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String project;
    private String name;
    private Instant timestamp;
    private Integer pid;
    private String status;
    private String runDir;
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;
}
