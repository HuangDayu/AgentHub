package com.agenthub.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 创建工作流请求DTO。
 * 封装创建工作流的API请求参数。
 *
 * @author huangdayu
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDagWorkflowRequest {

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
}
