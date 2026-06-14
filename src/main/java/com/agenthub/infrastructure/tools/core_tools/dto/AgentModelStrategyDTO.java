package com.agenthub.infrastructure.tools.core_tools.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent模型策略DTO，仅暴露Agent决策所需的模型策略信息。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentModelStrategyDTO {
    private String id;
    private String name;
    private String description;
    private double temperature;
    private int maxTokens;
    private int maxMessages;
    private double topP;
    private int topK;
    private double frequencyPenalty;
    private double presencePenalty;
}
