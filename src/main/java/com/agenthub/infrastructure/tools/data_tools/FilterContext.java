package com.agenthub.infrastructure.tools.data_tools;

import com.agenthub.domain.model.DataModelMetadata;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 过滤条件上下文
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilterContext {
    private DataModelMetadata metadata;
    private Map<String, Object> filters;
    private String tenantId;
    private String workspaceId;
}
