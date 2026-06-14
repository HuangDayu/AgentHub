package com.agenthub.infrastructure.tools.data_tools;

import com.agenthub.domain.model.DataModelMetadata;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 过滤条件条目
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilterEntry {
    /**
     * 字段名
     */
    private String fieldName;

    /**
     * 字段值
     */
    private Object value;

    /**
     * 元数据
     */
    private DataModelMetadata metadata;
}
