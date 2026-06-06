package com.agenthub.domain.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent 数据源变更事件 - 触发 ToolCallback/MCP 同步刷新
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentDataSourceChangedEvent {
    public enum ChangeType { CREATED, UPDATED, ENABLED, DISABLED, DELETED }
    private String dataSourceId;
    private String workspaceId;
    private ChangeType changeType;
}
