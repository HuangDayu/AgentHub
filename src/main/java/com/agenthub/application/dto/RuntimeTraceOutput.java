package com.agenthub.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RuntimeTraceOutput {
    private String runId;
    private String startTimeUnixNano;
    private String endTimeUnixNano;
    private Long latencyNs;
    private String status;
    private Integer spanCount;
    private Long totalTokens;
}
