package com.agenthub.api.dto;

public record SetAgentConfigRequest(
        String category,
        String type,
        String configId,
        String description,
        Integer priority,
        Boolean enabled
) {}
