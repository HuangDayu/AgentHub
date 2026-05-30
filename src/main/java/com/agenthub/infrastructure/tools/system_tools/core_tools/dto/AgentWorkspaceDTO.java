package com.agenthub.infrastructure.tools.system_tools.core_tools.dto;

import com.agenthub.domain.model.Workspace.WorkspaceStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent工作空间DTO，仅暴露Agent决策所需的工作空间信息。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentWorkspaceDTO {
    private String id;
    private String name;
    private String workspaceCode;
    private String region;
    private WorkspaceStatus status;
}
