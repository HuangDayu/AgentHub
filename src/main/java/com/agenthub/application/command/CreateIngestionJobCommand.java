package com.agenthub.application.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateIngestionJobCommand {
    private String tenantId;
    private String kbId;
    private String documentId;
    private String filePath;
}
