package com.agenthub.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentDataSourceTestResponse {
    private boolean success;
    private long elapsedMs;
    private String message;
}
