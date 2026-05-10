package com.agenthub.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateIngestionJobResponse {
    private String jobId;
    private String kbId;
    private String status;
    private int documentCount;
}
