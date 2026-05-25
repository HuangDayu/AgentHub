package com.agenthub.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class ChatInvocationStatsResponse {
    private Integer modelInvocations;
    private TokenStatsResponse avgTokens;
    private TokenStatsResponse totalTokens;
    private List<ModelInvocationByModelResponse> modelInvocationsByModel;
    private List<ModelTokenStatsResponse> avgTokensByModel;
    private List<ModelTokenStatsResponse> totalTokensByModel;
}
