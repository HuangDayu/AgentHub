package com.agenthub.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SetAgentConfigRequest {
    private String category;
    private String type;
    private String configId;
    private String name;
    private String description;
    private Integer priority;
    private Boolean enabled;
}
