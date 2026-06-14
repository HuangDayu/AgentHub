package com.agenthub.domain.model.data_source;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 协议字段定义
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentDataSourceField {
    private String name;
    private String type;
    private boolean required;
    private String defaultValue;
    private String description;
    private String placeholder;
}
