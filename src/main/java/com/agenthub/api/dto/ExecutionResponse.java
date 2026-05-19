package com.agenthub.api.dto;

import com.agenthub.domain.enums.workflow.WorkflowStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * 执行响应DTO。
 * 封装工作流执行的API响应数据。
 *
 * @author huangdayu
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionResponse {

    /** 执行ID */
    private String executionId;

    /** 工作流ID */
    private String workflowId;

    /** 执行状态 */
    private WorkflowStatus status;

    /** 变量上下文 */
    private Map<String, Object> variables;

    /** 开始时间 */
    private Instant startTime;

    /** 结束时间 */
    private Instant endTime;

    /** 错误信息 */
    private String errorMessage;
}
