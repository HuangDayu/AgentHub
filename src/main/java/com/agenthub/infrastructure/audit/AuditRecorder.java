package com.agenthub.infrastructure.audit;

import com.agenthub.application.port.out.AuditLogger;
import com.agenthub.application.port.out.repositories.AuditLogRepository;
import com.agenthub.domain.event.AuditEvent;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 全局异步审计记录器 - 批量写入 audit_log 表
 */
@Component
public class AuditRecorder implements AuditLogger {
    private static final Logger log = LoggerFactory.getLogger(AuditRecorder.class);
    private static final int QUEUE_CAPACITY = 10000;
    private static final int BATCH_SIZE = 500;
    private static final int FLUSH_INTERVAL_SECONDS = 1;

    private final AuditLogRepository repository;
    private final ExecutorService ttlExecutorService;
    private final BlockingQueue<AuditEvent> queue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
    private final java.util.concurrent.atomic.AtomicBoolean running = new java.util.concurrent.atomic.AtomicBoolean(false);
    private Thread flushThread;

    public AuditRecorder(AuditLogRepository repository, @Qualifier("ttlExecutorService") ExecutorService ttlExecutorService) {
        this.repository = repository;
        this.ttlExecutorService = ttlExecutorService;
    }

    @PostConstruct
    public void start() {
        running.set(true);
        flushThread = new Thread(this::flushLoop, "audit-recorder");
        flushThread.setDaemon(true);
        ttlExecutorService.execute(flushThread);
    }

    @PreDestroy
    public void stop() {
        running.set(false);
        if (flushThread != null) {
            flushThread.interrupt();
        }
        flush();
    }

    @Override
    public void log(AuditEvent event) {
        if (event == null) return;
        try {
            repository.save(event);
        } catch (Exception e) {
            log.warn("sync audit log failed: {}", e.getMessage());
        }
    }

    @Override
    public void logAsync(AuditEvent event) {
        if (event == null) return;
        boolean offered = queue.offer(event);
        if (!offered) {
            log.warn("audit queue full, dropping event: {}", event.getId());
        }
    }

    private void flushLoop() {
        while (running.get()) {
            sleepOrBreak();
            flush();
        }
        flush();
    }

    private void sleepOrBreak() {
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(FLUSH_INTERVAL_SECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            running.set(false);
        } catch (Exception e) {
            log.error("audit flush error", e);
        }
    }

    private void flush() {
        List<AuditEvent> batch = new ArrayList<>();
        queue.drainTo(batch, BATCH_SIZE);
        if (batch.isEmpty()) return;
        try {
            repository.saveAll(batch);
        } catch (Exception e) {
            log.error("audit batch insert failed, size={}", batch.size(), e);
        }
    }
}
