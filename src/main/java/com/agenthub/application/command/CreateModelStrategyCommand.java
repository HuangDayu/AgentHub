package com.agenthub.application.command;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateModelStrategyCommand {
    private String workspaceId;
    private String name;
    private String description;
    private double temperature;
    private int maxTokens;
    private double topP;
    private double frequencyPenalty;
    private double presencePenalty;
}
