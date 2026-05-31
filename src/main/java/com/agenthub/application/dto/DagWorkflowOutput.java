package com.agenthub.application.dto;

import java.time.Instant;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 工作流输出对象。
 * 用于封装工作流的输出数据。
 *
 * @author huangdayu
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DagWorkflowOutput {

    /** 工作流ID */
    private String id;

    /** 租户ID */
    private String tenantId;

    /** 工作空间ID */
    private String workspaceId;

    /** 工作流编码 */
    private String workflowCode;

    /** 名称 */
    private String name;

    /** 描述 */
    private String description;

    /** 图定义JSON */
    private String graphDefinition;

    /** 状态 */
    private String status;

    /** 创建时间 */
    private Instant createdAt;

    /** 更新时间 */
    private Instant updatedAt;
}
