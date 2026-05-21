package com.agenthub.infrastructure.tools.system_tools.data_tools.dto;

import com.agenthub.domain.model.PromptTemplateInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent提示模板DTO，仅暴露Agent决策所需的模板信息，不含模板内容。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentPromptTemplateDTO {
    private String id;
    private String name;
    private String description;
    private PromptTemplateInfo.Category category;
    private boolean isActive;
}
