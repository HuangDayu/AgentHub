package com.agenthub.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新工作流请求DTO。
 * 封装工作流更新的API请求参数。
 *
 * @author huangdayu
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateWorkflowRequest {

    /** 工作流编码 */
    private String workflowCode;

    /** 名称 */
    private String name;

    /** 描述 */
    private String description;

    /** 图定义JSON */
    private String graphDefinition;
}
