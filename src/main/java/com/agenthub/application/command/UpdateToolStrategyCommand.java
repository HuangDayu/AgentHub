package com.agenthub.application.command;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateToolStrategyCommand {
    private String workspaceId;
    private String name;
    private String description;
    private Integer maxConcurrentCalls;
    private Integer timeoutSeconds;
    private Integer retryCount;
    private Boolean fallbackEnabled;
    private List<String> allowedTools;
}
