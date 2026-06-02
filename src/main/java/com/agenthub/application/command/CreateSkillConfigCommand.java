package com.agenthub.application.command;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 创建技能配置命令。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSkillConfigCommand {
    private String tenantId;
    private String workspaceId;
    private String name;
    private String description;
    private List<String> skillPaths;
    private boolean syncEnabled;
    private int syncInterval;
    private boolean autoSync;
}
