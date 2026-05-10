package com.agenthub.api.dto;

import java.time.Instant;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentConfigResponse {
    private String id;
    private String agentId;
    private String category;
    private String type;
    private String configId;
    private String name;
    private String description;
    private int priority;
    private boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;
}
