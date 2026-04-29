package com.agenthub.application.dto;

import java.util.List;

public record AgentConfigTypeOutput(
        String category,
        String displayName,
        String description,
        List<TypeInfo> types
) {
    public record TypeInfo(
            String type,
            String displayName,
            String description
    ) {}
}
