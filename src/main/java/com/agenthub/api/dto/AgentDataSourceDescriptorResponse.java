package com.agenthub.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentDataSourceDescriptorResponse {
    private String protocol;
    private String scheme;
    private String displayName;
    private String description;
    private String syntaxHint;
    private List<AgentDataSourceFieldResponse> fields;
}
