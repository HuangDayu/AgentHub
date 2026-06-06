package com.agenthub.application.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 审计日志查询命令
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryAuditLogCommand {
    private String tenantId;
    private String workspaceId;
    private String resourceType;
    private String resourceId;
    private String actorId;
    private String action;
    private String status;
    private Instant from;
    private Instant to;
    private int page;
    private int size;
}
