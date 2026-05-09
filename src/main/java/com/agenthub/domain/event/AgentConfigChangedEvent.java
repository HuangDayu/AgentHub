package com.agenthub.domain.event;

import com.agenthub.common.annotations.ConfigChangeListenerEntity;
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
public class AgentConfigChangedEvent {

    private String entityType;
    private ChangeType changeType;
    private List<String> primaryKeys;
    private Instant timestamp;
    private String operator;
    private ConfigChangeListenerEntity entityAnnotation;

    public enum ChangeType {
        UPDATE, DELETE
    }
}
