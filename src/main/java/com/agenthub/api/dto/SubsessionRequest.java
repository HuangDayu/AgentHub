package com.agenthub.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建子会话请求DTO。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubsessionRequest {
    private String subagentId;
    private String name;
}
