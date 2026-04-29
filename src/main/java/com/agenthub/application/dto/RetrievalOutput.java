package com.agenthub.application.dto;

import java.util.List;

/**
 * 检索流水线输出结果。
 */
public record RetrievalOutput(
        String rewrittenQuery,
        List<RetrievalResultOutput> results,
        List<CitationOutput> citations
) {
}
