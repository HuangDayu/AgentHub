package com.agenthub.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ModelTokenStatsOutput {
    private String modelName;
    private Double promptTokens;
    private Double completionTokens;
    private Double totalTokens;
}
