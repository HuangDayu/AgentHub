package com.agenthub.application.usecase;

import com.agenthub.application.port.out.AgentDataSourcePort;
import com.agenthub.application.port.out.repositories.AgentDataSourceRepository;
import com.agenthub.common.annotations.IgnoreTenantContext;
import com.agenthub.domain.model.AgentDataSource;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 启动恢复 - 应用启动后扫描所有 enabled=true 的数据源并启动 Camel 路由
 */
@Component
@RequiredArgsConstructor
public class AgentDataSourceBootstrapUseCase {
    private static final Logger log = LoggerFactory.getLogger(AgentDataSourceBootstrapUseCase.class);
    private final AgentDataSourceRepository repository;
    private final AgentDataSourcePort port;

    /**
     * 应用启动完成后异步恢复
     */
    @EventListener(ApplicationReadyEvent.class)
    @IgnoreTenantContext
    public void onApplicationReady() {
        List<AgentDataSource> enabled = findEnabled();
        if (enabled.isEmpty()) return;
        enabled.parallelStream().forEach(this::bootstrapOrMarkError);
    }

    private List<AgentDataSource> findEnabled() {
        try {
            return repository.findAll().stream()
                .filter(AgentDataSource::isEnabled)
                .toList();
        } catch (Exception e) {
            log.warn("agent_data_source table not ready, skipping bootstrap: {}", e.getMessage());
            return List.of();
        }
    }

    private void bootstrapOrMarkError(AgentDataSource source) {
        try {
            port.bootstrap(source);
        } catch (Exception e) {
            source.setStatus(com.agenthub.domain.enums.AgentDataSourceStatus.ERROR);
            source.setLastErrorMessage(e.getMessage());
            repository.save(source);
        }
    }
}
