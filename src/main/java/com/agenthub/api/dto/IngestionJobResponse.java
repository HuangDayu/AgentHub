package com.agenthub.api.dto;

import java.time.Instant;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IngestionJobResponse {
    private String jobId;
    private String kbId;
    private String status;
    private int documentCount;
    private Instant createdAt;
    private List<IngestionDocumentResponse> documents;
}
