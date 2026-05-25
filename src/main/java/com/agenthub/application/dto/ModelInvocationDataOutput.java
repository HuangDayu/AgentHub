package com.agenthub.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ModelInvocationDataOutput {
    private Integer modelInvocations;
    private ChatInvocationStatsOutput chat;
}
