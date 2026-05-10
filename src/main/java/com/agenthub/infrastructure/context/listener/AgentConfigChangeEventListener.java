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

    @Async
    @EventListener
    public void handleDataChange(AgentConfigChangedEvent event) {
        log.info("【Agent配置改变】实体: {}, 操作: {}, IDs: {}", event.getEntityType(), event.getChangeType(), event.getPrimaryKeys());
        ConfigChangeListenerEntity entityAnnotation = event.getEntityAnnotation();
        for (String primaryKey : event.getPrimaryKeys()) {
            List<AgentConfig> agentConfigs = agentConfigRepository.findAgentConfigs(entityAnnotation.category(), entityAnnotation.type(), primaryKey);
            if (agentConfigs != null && !agentConfigs.isEmpty()) {
                for (AgentConfig agentConfig : agentConfigs) {
                    if (event.getChangeType() == AgentConfigChangedEvent.ChangeType.DELETE) {
                        eventPublisher.publishEvent(new AgentConfigDeletedEvent(agentConfig.getAgentId(), entityAnnotation.category(), entityAnnotation.type(), agentConfig.getId()));
                        agentConfigRepository.deleteById(agentConfig.getId());
                    } else {
                        eventPublisher.publishEvent(new AgentConfigUpdatedEvent(agentConfig.getAgentId(), entityAnnotation.category(), entityAnnotation.type(), agentConfig.getId()));
                    }
                }
            }
        }
    }

}
