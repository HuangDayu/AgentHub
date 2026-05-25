package com.agenthub.api.dto;

import lombok.Data;

@Data
public class RuntimeSpanSummaryResponse {
    private String spanId;
    private String parentSpanId;
    private String name;
    private Long latencyNs;
    private Integer statusCode;
    private String status;
    private String model;
}
