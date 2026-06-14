package com.agenthub.infrastructure.tools.core_tools.dto;

import com.agenthub.domain.model.agent.ReActAgentContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 提示模板规格参数。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateSpec {
    private String name;
    private String description;
    private String category;
    private String content;
    private Boolean active;
    private ReActAgentContext ctx;
}
