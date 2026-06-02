package com.agenthub.api.dto;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 创建技能配置请求。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSkillConfigRequest {
    private String tenantId;
    private String name;
    private String description;
    private List<String> skillPaths;
    private boolean syncEnabled;
    private int syncInterval;
    private boolean autoSync;
}
