package com.agenthub.infrastructure.context.listener;

import com.agenthub.domain.event.AgentConfigChangeEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 数据变更事件监听器
 */
@Slf4j
@Component
public class AgentConfigChangeEventListener {

    @Async
    @EventListener
    public void handleDataChange(AgentConfigChangeEvent event) {
        log.info("【审计日志】实体: {}, 操作: {}, IDs: {}", event.getEntityType(), event.getChangeType(), event.getPrimaryKeys());
        
        routeByChangeType(event);
    }

    private void routeByChangeType(AgentConfigChangeEvent event) {
        if (event.getChangeType() == AgentConfigChangeEvent.ChangeType.DELETE) {
            handleDelete(event);
        } else {
            handleUpdate(event);
        }
    }

    private void handleDelete(AgentConfigChangeEvent event) {
        log.info("执行删除审计 - 实体: {}, IDs: {}", 
            event.getEntityType(), 
            event.getPrimaryKeys());
        
        recordAuditLog(event);
    }

    private void handleUpdate(AgentConfigChangeEvent event) {
        log.info("执行更新审计 - 实体: {}, IDs: {}", 
            event.getEntityType(), 
            event.getPrimaryKeys());
        
        recordAuditLog(event);
    }

    private void recordAuditLog(AgentConfigChangeEvent event) {
        // TODO: 记录到审计日志表
        // TODO: 发送通知
        // TODO: 清理缓存
        log.debug("审计记录完成");
    }
}
