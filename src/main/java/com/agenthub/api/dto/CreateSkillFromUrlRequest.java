package com.agenthub.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 从 URL 创建技能请求。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSkillFromUrlRequest {
    private String tenantId;
    private String skillCode;
    private String name;
    private String description;
    private String zipUrl;
}
