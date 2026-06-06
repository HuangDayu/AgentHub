package com.agenthub.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentDataSourceFieldResponse {
    private String name;
    private String type;
    private boolean required;
    private String defaultValue;
    private String description;
    private String placeholder;
}
