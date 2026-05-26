package com.agenthub.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatAttachmentOutput {
    private String fileName;
    private String path;
    private String contentType;
    private long size;
}
