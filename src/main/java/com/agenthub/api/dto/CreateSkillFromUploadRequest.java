package com.agenthub.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 从上传创建技能请求。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSkillFromUploadRequest {
    private String skillCode;
    private String name;
    private String description;
}
