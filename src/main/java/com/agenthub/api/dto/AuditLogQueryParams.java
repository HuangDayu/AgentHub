package com.agenthub.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;

/**
 * 审计日志查询参数 DTO - 通过 @ModelAttribute 自动绑定
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogQueryParams {
    private String workspaceId;
    private String resourceType;
    private String resourceId;
    private String actorId;
    private String action;
    private String status;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Instant from;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Instant to;
    private int page = 1;
    private int size = 50;
}
