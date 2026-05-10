package com.agenthub.application.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentConfigCommand {
    private String id;
    private String agentId;
    private String category;
    private String type;
    private String configId;
    private String name;
    private String description;
    private Integer priority;
    private Boolean enabled;
}
