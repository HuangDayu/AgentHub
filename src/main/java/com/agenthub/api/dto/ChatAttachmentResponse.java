package com.agenthub.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatAttachmentResponse {
    private String fileName;
    private String path;
    private String contentType;
    private long size;
}
