package com.agenthub.infrastructure.context.listener;

import com.agenthub.common.annotations.ConfigChangeListenerEntity;
import com.agenthub.domain.event.AgentConfigChangeEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;

/**
 * 数据审计拦截器
 */
@Slf4j
@Component
public class AgentConfigChangeInterceptor implements com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor {

    private final ApplicationEventPublisher eventPublisher;

    public AgentConfigChangeInterceptor(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void beforeUpdate(Executor executor, MappedStatement ms, Object parameter) throws SQLException {
        SqlCommandType commandType = ms.getSqlCommandType();
        if (!isAuditCommand(commandType)) {
            return;
        }

        Class<?> entityClass = extractEntityClass(ms);
        if (!isAuditedEntity(entityClass)) {
            return;
        }

        List<String> primaryKeys = extractPrimaryKeys(parameter, entityClass, commandType);
        if (primaryKeys.isEmpty()) {
            return;
        }

        publishAuditEvent(entityClass, commandType, primaryKeys);
    }

    private boolean isAuditCommand(SqlCommandType commandType) {
        return commandType == SqlCommandType.UPDATE 
            || commandType == SqlCommandType.DELETE;
    }

    private Class<?> extractEntityClass(MappedStatement ms) {
        try {
            String mapperName = getMapperClassName(ms.getId());
            return Class.forName(mapperName);
        } catch (ClassNotFoundException e) {
            log.warn("提取实体类失败: {}", e.getMessage());
            return null;
        }
    }

    private String getMapperClassName(String mappedStatementId) {
        int lastDotIndex = mappedStatementId.lastIndexOf('.');
        return mappedStatementId.substring(0, lastDotIndex);
    }

    private boolean isAuditedEntity(Class<?> entityClass) {
        if (entityClass == null) {
            return false;
        }
        return entityClass.isAnnotationPresent(ConfigChangeListenerEntity.class);
    }

    private List<String> extractPrimaryKeys(Object parameter, Class<?> entityClass, SqlCommandType commandType) {
        PrimaryKeyExtractor extractor = new PrimaryKeyExtractor();
        return extractor.extract(parameter, entityClass, commandType);
    }

    private void publishAuditEvent(Class<?> entityClass, SqlCommandType commandType, List<String> primaryKeys) {
        AgentConfigChangeEvent.ChangeType changeType = convertToChangeType(commandType);
        AgentConfigChangeEvent event = buildEvent(entityClass, changeType, primaryKeys);
        logAuditInfo(entityClass, changeType, primaryKeys);
        eventPublisher.publishEvent(event);
    }

    private AgentConfigChangeEvent.ChangeType convertToChangeType(SqlCommandType commandType) {
        if (commandType == SqlCommandType.DELETE) {
            return AgentConfigChangeEvent.ChangeType.DELETE;
        }
        return AgentConfigChangeEvent.ChangeType.UPDATE;
    }

    private AgentConfigChangeEvent buildEvent(Class<?> entityClass, AgentConfigChangeEvent.ChangeType changeType, List<String> primaryKeys) {
        return AgentConfigChangeEvent.builder()
            .entityType(entityClass.getSimpleName())
            .changeType(changeType)
            .primaryKeys(primaryKeys)
            .timestamp(java.time.Instant.now())
            .build();
    }

    private void logAuditInfo(Class<?> entityClass, AgentConfigChangeEvent.ChangeType changeType, List<String> primaryKeys) {
        log.info("检测到数据变更 - 实体: {}, 类型: {}, 主键IDs: {}", 
            entityClass.getSimpleName(), changeType, primaryKeys);
    }
}
