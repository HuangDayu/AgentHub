package com.agenthub.infrastructure.tools.data_tools;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 查询输入参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueryInput {
    private String model;
    private Map<String, Object> filters;
    private Integer page;
    private Integer size;
}
