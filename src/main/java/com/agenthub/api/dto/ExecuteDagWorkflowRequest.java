package com.agenthub.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 执行工作流请求DTO。
 * 封装工作流执行的API请求参数。
 *
 * @author huangdayu
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteDagWorkflowRequest {

    /** 执行输入参数 */
    private Map<String, Object> input;

    /** 触发者 */
    private String triggeredBy;
}
