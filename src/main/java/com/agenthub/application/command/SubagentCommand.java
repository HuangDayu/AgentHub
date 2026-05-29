package com.agenthub.application.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 子智能体命令，用于创建/更新子Agent。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubagentCommand {
    private String id;
    private String tenantId;
    private String workspaceId;
    private String parentAgentId;
    private String name;
    private String description;
    private String systemPrompt;
    private String modelConfigId;
    /**
     * 创建子Agent时，同时在此会话中创建对应的Subsession。
     */
    
    private String sessionId;
}
