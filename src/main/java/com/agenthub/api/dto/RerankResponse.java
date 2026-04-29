package com.agenthub.api.dto;

import java.util.Map;

/**
 * 重排响应记录。
 */
public record RerankResponse(
        Map<String, Double> scores
) {}
