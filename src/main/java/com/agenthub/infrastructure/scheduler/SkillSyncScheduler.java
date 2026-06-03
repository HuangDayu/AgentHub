package com.agenthub.infrastructure.scheduler;

import com.agenthub.application.port.out.SkillSyncSchedulerPort;
import com.agenthub.application.port.out.repositories.SkillConfigRepository;
import com.agenthub.application.usecase.SkillUseCase;
import com.agenthub.common.annotations.IgnoreTenantContext;
import com.agenthub.domain.model.skill.SkillConfig;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * 技能同步调度器，管理定时任务生命周期。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SkillSyncScheduler implements SkillSyncSchedulerPort {

    private final SkillConfigRepository skillConfigRepository;
    private final SkillUseCase skillUseCase;
    private ThreadPoolTaskScheduler taskScheduler;
    private final Map<String, ScheduledFuture<?>> futures = new ConcurrentHashMap<>();

    /**
     * 初始化调度器并加载所有自动同步配置。
     */
    @PostConstruct
    void init() {
        taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(2);
        taskScheduler.setThreadNamePrefix("skill-sync-");
        taskScheduler.initialize();
    }

    @EventListener(ApplicationReadyEvent.class)
    @IgnoreTenantContext
    public void onApplicationReady() {
        loadAndScheduleAll();
    }

    /**
     * 从数据库加载所有启用的自动同步配置。
     */
    private void loadAndScheduleAll() {
        List<SkillConfig> configs = skillConfigRepository.findAllEnabledAutoSync();
        configs.forEach(this::schedule);
        log.info("Loaded {} skill sync configs", configs.size());
    }

    /**
     * 注册定时任务。
     */
    public void schedule(SkillConfig config) {
        unschedule(config.getId());
        if (!config.isAutoSync() || !config.isSyncEnabled()) {
            return;
        }
        ScheduledFuture<?> future = taskScheduler.scheduleWithFixedDelay(
                () -> executeSync(config.getId()),
                Duration.ofSeconds(config.getSyncInterval())
        );
        futures.put(config.getId(), future);
        log.info("Scheduled skill sync: {} every {}s", config.getId(), config.getSyncInterval());
    }

    /**
     * 取消定时任务。
     */
    public void unschedule(String configId) {
        ScheduledFuture<?> future = futures.remove(configId);
        if (future != null) {
            future.cancel(false);
        }
    }

    /**
     * 重新调度定时任务。
     */
    public void reschedule(SkillConfig config) {
        schedule(config);
    }

    /**
     * 执行同步任务。
     */
    private void executeSync(String configId) {
        try {
            skillUseCase.syncWithConfig(configId);
            log.info("Skill sync completed: {}", configId);
        } catch (Exception e) {
            log.error("Skill sync failed: {}", configId, e);
        }
    }

    /**
     * 关闭调度器。
     */
    @PreDestroy
    void shutdown() {
        futures.values().forEach(f -> f.cancel(false));
        if (taskScheduler != null) {
            taskScheduler.shutdown();
        }
    }
}
