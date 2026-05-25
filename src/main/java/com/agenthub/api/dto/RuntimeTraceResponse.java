package com.agenthub.api.dto;

import lombok.Data;

@Data
public class RuntimeTraceResponse {
    private String runId;
    private String startTimeUnixNano;
    private String endTimeUnixNano;
    private Long latencyNs;
    private String status;
    private Integer spanCount;
    private Long totalTokens;
    private Integer errorSpanCount;
    private String slowestSpanId;
    private String slowestSpanName;
    private Long slowestLatencyNs;
}
