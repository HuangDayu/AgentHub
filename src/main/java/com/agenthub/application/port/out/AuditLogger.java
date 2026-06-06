package com.agenthub.application.port.out;

import com.agenthub.domain.event.AuditEvent;

/**
 * 全局审计日志记录端口
 * <p>所有 UseCase 操作通过 AuditAspect + @Audited 注解自动调用此接口。</p>
 */
public interface AuditLogger {
    /** 同步记录：用于关键操作 */
    void log(AuditEvent event);

    /** 异步记录：用于一般操作（默认） */
    void logAsync(AuditEvent event);
}
