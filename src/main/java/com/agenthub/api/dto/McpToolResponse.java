package com.agenthub.api.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class McpToolResponse {
    private String id;
    private String name;
    private String description;
    private String serverUrl;
    private String serverType;
    private String command;
    private List<String> args;
    private Map<String, String> env;
    private boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;
}
