package com.agenthub.api.dto;

import lombok.Data;

@Data
public class ModelInvocationDataResponse {
    private Integer modelInvocations;
    private ChatInvocationStatsResponse chat;
}
