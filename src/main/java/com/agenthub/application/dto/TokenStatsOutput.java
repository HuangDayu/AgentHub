package com.agenthub.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenStatsOutput {
    private Double promptTokens;
    private Double completionTokens;
    private Double totalTokens;
}
