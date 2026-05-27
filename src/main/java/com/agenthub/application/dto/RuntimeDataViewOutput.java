package com.agenthub.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RuntimeDataViewOutput {
    private List<RuntimeRunOutput> runs;
    private RuntimeRunOutput selectedRun;
    private RuntimeTraceOutput trace;
    private List<SpanTreeNodeOutput> spanTree;
    private List<RuntimeSpanSummaryOutput> errorSpans;
    private List<RuntimeSpanSummaryOutput> slowSpans;
    private ModelInvocationDataOutput modelInvocationData;
}
