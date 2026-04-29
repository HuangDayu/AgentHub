package com.agenthub.api.dto;

import java.util.List;

public record ConfigTypeDefinition(
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
