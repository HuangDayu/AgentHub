package com.agenthub.infrastructure.store.db.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;

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
    private String workspaceId;
    private String actorId;
    private String actorType;
    private String agentId;
    private String sessionId;
    private String resourceType;
    private String resourceId;
    @TableField(jdbcType = JdbcType.LONGVARCHAR)
    private String resourceName;
    @TableField(jdbcType = JdbcType.LONGVARCHAR)
    private String action;
    private String status;
    @TableField(jdbcType = JdbcType.LONGVARCHAR)
    private String request;
    @TableField(jdbcType = JdbcType.LONGVARCHAR)
    private String response;
    @TableField(jdbcType = JdbcType.LONGVARCHAR)
    private String errorMessage;
    @TableField(jdbcType = JdbcType.LONGVARCHAR)
    private String metadata;
    private Long elapsedMs;
    @TableField(fill = FieldFill.INSERT)
    private Instant createdAt;
}
