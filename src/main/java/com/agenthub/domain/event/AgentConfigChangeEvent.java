package com.agenthub.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * 数据变更事件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentConfigChangeEvent {

    private String entityType;
    private ChangeType changeType;
    private List<String> primaryKeys;
    private Instant timestamp;
    private String operator;

    public enum ChangeType {
        UPDATE, DELETE
    }
}
