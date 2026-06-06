package com.agenthub.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 协议描述符 - Camel Component 元数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentDataSourceDescriptor {
    private String protocol;
    private String scheme;
    private String displayName;
    private String description;
    private String syntaxHint;
    private List<AgentDataSourceField> fields;
}
