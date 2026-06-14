package com.agenthub.infrastructure.context.listener;

import com.agenthub.application.port.out.repositories.AgentConfigRepository;
import com.agenthub.common.annotations.ConfigChangeListenerEntity;
import com.agenthub.domain.event.AgentConfigChangedEvent;
import com.agenthub.domain.event.AgentConfigDeletedEvent;
import com.agenthub.domain.event.AgentConfigUpdatedEvent;
import com.agenthub.domain.model.agent.AgentConfig;
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
        log.debug("【Agent配置改变】实体: {}, 操作: {}, IDs: {}", event.getEntityType(), event.getChangeType(), event.getPrimaryKeys());
        List<String> pks = event.getPrimaryKeys();
        if (pks == null || pks.isEmpty()) return;
        ConfigChangeListenerEntity entity = event.getEntityAnnotation();
        List<AgentConfig> configs = agentConfigRepository.findAgentConfigs(entity.category(), entity.type(), pks);
        if (configs == null || configs.isEmpty()) return;
        publishConfigChange(configs, entity, event);
    }

    private void publishConfigChange(List<AgentConfig> configs, ConfigChangeListenerEntity entity,
                                     AgentConfigChangedEvent event) {
        if (event.getChangeType() == AgentConfigChangedEvent.ChangeType.DELETE) {
            eventPublisher.publishEvent(new AgentConfigDeletedEvent(configs, entity.category(), entity.type()));
            agentConfigRepository.deleteByIds(configs.stream().map(AgentConfig::getId).toList());
        } else {
            eventPublisher.publishEvent(new AgentConfigUpdatedEvent(configs, entity.category(), entity.type()));
        }
    }

}
