package com.agenthub.api.dto;

import lombok.Data;

@Data
public class TokenStatsResponse {
    private Double promptTokens;
    private Double completionTokens;
    private Double totalTokens;
}
