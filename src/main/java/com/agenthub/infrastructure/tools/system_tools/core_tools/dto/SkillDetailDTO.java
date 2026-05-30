package com.agenthub.infrastructure.tools.system_tools.core_tools.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 技能详情DTO，包含完整的技能信息。
 */
@Data
@NoArgsConstructor
public class SkillDetailDTO {
    private String id;
    private String name;
    private String description;
    private String skillType;
    private String skillPath;
    private String skillFilesTree;
    private boolean enabled;
}
