package com.agenthub.api.dto;

import lombok.Data;

@Data
public class ModelTokenStatsResponse {
    private String modelName;
    private Double promptTokens;
    private Double completionTokens;
    private Double totalTokens;
}
