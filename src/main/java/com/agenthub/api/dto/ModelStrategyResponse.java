package com.agenthub.api.dto;

import java.time.Instant;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModelStrategyResponse {
    private String id;
    private String name;
    private String description;
    private double temperature;
    private int maxTokens;
    private double topP;
    private double frequencyPenalty;
    private double presencePenalty;
    private Instant createdAt;
    private Instant updatedAt;
}
