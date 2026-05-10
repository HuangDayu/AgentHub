package com.agenthub.api.dto;

import java.time.Instant;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToolStrategyResponse {
    private String id;
    private String name;
    private String description;
    private int maxConcurrentCalls;
    private int timeoutSeconds;
    private int retryCount;
    private boolean fallbackEnabled;
    private Instant createdAt;
    private Instant updatedAt;
}
