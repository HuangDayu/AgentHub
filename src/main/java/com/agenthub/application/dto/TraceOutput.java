package com.agenthub.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

/**
 * Trace 输出 DTO.
 */
@Data
@AllArgsConstructor
public class TraceOutput {
    private String id;
    private String traceId;
    private String runId;

    private String rootSpanId;
    private Integer spanCount;

    private String startTimeUnixNano;
    private String endTimeUnixNano;
    private Long durationNs;

    private Integer statusCode;
    private String errorMessage;

    private Long totalTokens;

    private Instant createdAt;
}
