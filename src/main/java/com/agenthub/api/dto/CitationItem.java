package com.agenthub.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CitationItem {
    private int index;
    private String documentId;
    private String chunkId;
    private String excerpt;
}
