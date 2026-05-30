package com.agenthub.infrastructure.tools.system_tools.core_tools.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent技能DTO，仅暴露Agent决策所需的技能信息。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentSkillDTO {
    private String id;
    private String name;
    private String description;
    private String skillType;
    private boolean enabled;
}
