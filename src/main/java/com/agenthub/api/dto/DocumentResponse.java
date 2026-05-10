package com.agenthub.api.dto;

import java.time.Instant;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {
    private String docId;
    private String kbId;
    private String fileName;
    private String contentType;
    private long size;
    private String status;
    private Instant createdAt;
}
