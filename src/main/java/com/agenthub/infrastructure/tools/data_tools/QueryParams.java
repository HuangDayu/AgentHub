package com.agenthub.infrastructure.tools.data_tools;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 查询参数封装对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueryParams {
    private String tenantId;
    private String workspaceId;
    private int page;
    private int size;
}
