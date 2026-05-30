package com.agenthub.infrastructure.tools.system_tools.core_tools.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent知识库DTO，仅暴露Agent决策所需的知识库信息，不含内部配置ID。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentKnowledgeBaseDTO {
    private String id;
    private String kbCode;
    private String name;
    private String description;
}
