package com.agenthub.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ModelInvocationByModelOutput {
    private String modelName;
    private Integer invocations;
}
