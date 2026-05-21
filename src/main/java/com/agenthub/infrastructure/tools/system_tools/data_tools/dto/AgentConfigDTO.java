package com.agenthub.infrastructure.tools.system_tools.data_tools.dto;

import com.agenthub.domain.enums.AgentConfigCategory;
import com.agenthub.domain.enums.AgentConfigType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent配置DTO，仅暴露Agent决策所需的配置关联信息。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentConfigDTO {
    private AgentConfigCategory category;
    private AgentConfigType type;
    private String configId;
    private String name;
    private String description;
    private int priority;
    private boolean enabled;
}
