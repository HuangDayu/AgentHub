package com.agenthub.infrastructure.store.db.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.Instant;

/**
 * 全局审计日志实体
 */
@Data
@TableName("audit_log")
public class AuditLogEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String tenantId;
    @TableField(exist = false)
    private String workspaceId;
    private String actorId;
    private String actorType;
    private String agentId;
    private String sessionId;
    private String resourceType;
    private String resourceId;
    private String resourceName;
    private String action;
    private String status;
    private String request;
    private String response;
    private String errorMessage;
    private String metadata;
    private Long elapsedMs;
    @TableField(fill = FieldFill.INSERT)
    private Instant createdAt;
}
