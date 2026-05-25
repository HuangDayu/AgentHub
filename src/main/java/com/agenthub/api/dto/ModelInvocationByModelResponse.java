package com.agenthub.api.dto;

import lombok.Data;

@Data
public class ModelInvocationByModelResponse {
    private String modelName;
    private Integer invocations;
}
