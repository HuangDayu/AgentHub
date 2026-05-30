package com.agenthub.infrastructure.tools.system_tools.core_tools.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent工具策略DTO，仅暴露Agent决策所需的工具策略信息。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentToolStrategyDTO {
    private String id;
    private String name;
    private String description;
    private int maxConcurrentCalls;
    private int timeoutSeconds;
    private int retryCount;
    private boolean fallbackEnabled;
}
