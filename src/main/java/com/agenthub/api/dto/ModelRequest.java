package com.agenthub.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelRequest {
    private String model;
    private String provider;
    private String apiKey;
    private String baseUrl;
    private String temperature;
    private String maxTokens;
}
