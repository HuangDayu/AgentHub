package com.agenthub.api.dto;

import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RerankResponse {
    private Map<String, Double> scores;
}
