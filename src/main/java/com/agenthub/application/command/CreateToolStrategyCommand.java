package com.agenthub.application.command;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateToolStrategyCommand {
    private String workspaceId;
    private String name;
    private String description;
    private int maxConcurrentCalls;
    private int timeoutSeconds;
    private int retryCount;
    private boolean fallbackEnabled;
    private List<String> allowedTools;
}
