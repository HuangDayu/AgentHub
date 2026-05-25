package com.agenthub.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RuntimeSpanSummaryOutput {
    private String spanId;
    private String parentSpanId;
    private String name;
    private Long latencyNs;
    private Integer statusCode;
    private String status;
    private String model;
}
