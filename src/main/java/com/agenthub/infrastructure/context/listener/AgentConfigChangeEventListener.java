package com.agenthub.infrastructure.context.listener;

import com.agenthub.application.port.out.repositories.AgentConfigRepository;
import com.agenthub.common.annotations.ConfigChangeListenerEntity;
import com.agenthub.domain.event.AgentConfigChangedEvent;
import com.agenthub.domain.event.AgentConfigDeletedEvent;
import com.agenthub.domain.event.AgentConfigUpdatedEvent;
import com.agenthub.domain.model.AgentConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 数据变更事件监听器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AgentConfigChangeEventListener {

    private final AgentConfigRepository agentConfigRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Async("ttlExecutorService")
    @EventListener
    public void handleDataChange(AgentConfigChangedEvent event) {
        log.info("【Agent配置改变】实体: {}, 操作: {}, IDs: {}", event.getEntityType(), event.getChangeType(), event.getPrimaryKeys());
        ConfigChangeListenerEntity entityAnnotation = event.getEntityAnnotation();
        if (event.getPrimaryKeys() != null && !event.getPrimaryKeys().isEmpty()) {
            List<AgentConfig> agentConfigs = agentConfigRepository.findAgentConfigs(entityAnnotation.category(), entityAnnotation.type(), event.getPrimaryKeys());
            if (agentConfigs != null && !agentConfigs.isEmpty()) {
                if (event.getChangeType() == AgentConfigChangedEvent.ChangeType.DELETE) {
                    eventPublisher.publishEvent(new AgentConfigDeletedEvent(agentConfigs, entityAnnotation.category(), entityAnnotation.type()));
                    agentConfigRepository.deleteByIds(agentConfigs.stream().map(AgentConfig::getId).toList());
                } else {
                    // TODO 同步更新配置数据中冗余的名称和描述
                    eventPublisher.publishEvent(new AgentConfigUpdatedEvent(agentConfigs, entityAnnotation.category(), entityAnnotation.type()));
                }
            }
        }
    }

}
