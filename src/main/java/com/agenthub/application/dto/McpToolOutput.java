package com.agenthub.application.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record McpToolOutput(
        String id, String name, String description,
        String serverUrl, String serverType, String command,
        List<String> args, Map<String, String> env,
        boolean enabled, Instant createdAt, Instant updatedAt
) {}
