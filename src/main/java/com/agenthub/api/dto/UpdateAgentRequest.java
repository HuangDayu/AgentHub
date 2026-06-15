package com.agenthub.api.dto;

import com.agenthub.domain.enums.AgentRuntimeCategory;
import com.agenthub.domain.enums.AgentType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAgentRequest {
    private String name;
    private String description;
    private AgentType type;
    private AgentRuntimeCategory runtimeCategory;
}
