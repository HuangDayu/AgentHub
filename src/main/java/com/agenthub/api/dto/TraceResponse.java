package com.agenthub.api.dto;

import lombok.Data;

import java.time.Instant;

/**
 * Trace Response DTO.
 */
@Data
public class TraceResponse {
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

    private Integer totalTokens;

    private Instant createdAt;
}
