package com.agenthub.application.command.workflow;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 执行命令对象。
 * 用于封装工作流执行的输入参数。
 *
 * @author huangdayu
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionCommand {

    /** 工作流ID */
    private String workflowId;

    /** 租户ID */
    private String tenantId;

    /** 工作空间ID */
    private String workspaceId;

    /** 执行输入参数 */
    private Map<String, Object> input;

    /** 触发者 */
    private String triggeredBy;
}
