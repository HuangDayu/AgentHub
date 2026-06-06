package com.agenthub.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentDataSourceInvokeResponse {
    private boolean success;
    private Object data;
    private long elapsedMs;
    private String exchangeId;
    private String errorMessage;
}
