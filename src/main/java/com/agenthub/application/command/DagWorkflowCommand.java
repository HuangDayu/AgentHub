package com.agenthub.application.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 工作流命令对象。
 * 用于封装工作流创建和更新的输入参数。
 *
 * @author huangdayu
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DagWorkflowCommand {

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
