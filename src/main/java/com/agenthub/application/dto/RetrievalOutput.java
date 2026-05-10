package com.agenthub.application.dto;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RetrievalOutput {
    private String rewrittenQuery;
    private List<RetrievalResultOutput> results;
    private List<CitationOutput> citations;
}
