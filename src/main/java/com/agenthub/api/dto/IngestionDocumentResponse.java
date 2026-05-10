package com.agenthub.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IngestionDocumentResponse {
    private String documentId;
    private String fileName;
    private String contentType;
    private long size;
}
