package com.agenthub.infrastructure.tools.core_tools.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RESTful 工具 DTO。
 */
@Data
@NoArgsConstructor
public class RestfulToolDTO {
    private String id;
    private String name;
    private String description;
    private String endpoint;
    private String httpMethod;
    private String inputSchemaJson;
    private int timeoutMs;
    private boolean enabled;
}
