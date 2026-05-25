package com.agenthub.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class RuntimeDataViewResponse {
    private List<RuntimeRunResponse> runs;
    private RuntimeRunResponse selectedRun;
    private RuntimeTraceResponse trace;
    private List<SpanResponse> spans;
    private List<RuntimeSpanSummaryResponse> errorSpans;
    private List<RuntimeSpanSummaryResponse> slowSpans;
    private ModelInvocationDataResponse modelInvocationData;
}
