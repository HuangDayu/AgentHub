package com.agenthub.domain.model.rag;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetrievalPolicy {
    private String id;
    private String name;
    private String description;
    private String type;
    private String config;
}
