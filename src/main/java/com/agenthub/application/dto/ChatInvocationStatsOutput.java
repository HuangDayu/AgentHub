package com.agenthub.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ChatInvocationStatsOutput {
    private Integer modelInvocations;
    private TokenStatsOutput avgTokens;
    private TokenStatsOutput totalTokens;
    private List<ModelInvocationByModelOutput> modelInvocationsByModel;
    private List<ModelTokenStatsOutput> avgTokensByModel;
    private List<ModelTokenStatsOutput> totalTokensByModel;
}
