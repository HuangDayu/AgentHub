package com.agenthub.infrastructure.scheduler;

import com.agenthub.application.port.out.agent.AgentChatPort;
import com.agenthub.application.port.out.repositories.AgentRepository;
import com.agenthub.application.port.out.repositories.ScheduledTaskRepository;
import com.agenthub.common.annotations.IgnoreTenantContext;
import com.agenthub.domain.model.ScheduledTask;
import com.agenthub.domain.model.agent.Agent;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * 定时任务调度器，负责定时任务的注册、执行和生命周期管理。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledTaskScheduler {

    private final ScheduledTaskRepository scheduledTaskRepository;
    private final AgentRepository agentRepository;
    private final AgentChatPort agentChatPort;

    private ThreadPoolTaskScheduler taskScheduler;
    private final Map<String, ScheduledFuture<?>> scheduledFutures = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(5);
        taskScheduler.setThreadNamePrefix("scheduled-task-");
        taskScheduler.setWaitForTasksToCompleteOnShutdown(true);
        taskScheduler.setAwaitTerminationSeconds(30);
        taskScheduler.initialize();
    }

    @PreDestroy
    public void shutdown() {
        scheduledFutures.values().forEach(f -> f.cancel(false));
        scheduledFutures.clear();
        taskScheduler.shutdown();
    }

    @EventListener(ApplicationReadyEvent.class)
    @IgnoreTenantContext
    public void onApplicationReady() {
        loadAndScheduleAllTasks();
    }

    public void scheduleTask(ScheduledTask task) {
        unscheduleTask(task.getId());
        if (!task.isEnabled()) return;
        try {
            CronTrigger trigger = new CronTrigger(task.getCronExpression());
            ScheduledFuture<?> future = taskScheduler.schedule(() -> executeTask(task), trigger);
            scheduledFutures.put(task.getId(), future);
            log.info("定时任务已注册: {} ({})", task.getName(), task.getCronExpression());
        } catch (IllegalArgumentException e) {
            log.error("定时任务Cron表达式无效: {} - {}", task.getName(), task.getCronExpression(), e);
        }
    }

    public void unscheduleTask(String taskId) {
        ScheduledFuture<?> future = scheduledFutures.remove(taskId);
        if (future != null) {
            future.cancel(false);
            log.info("定时任务已取消注册: {}", taskId);
        }
    }

    public void rescheduleTask(ScheduledTask task) {
        unscheduleTask(task.getId());
        scheduleTask(task);
    }

    public boolean isTaskScheduled(String taskId) {
        return scheduledFutures.containsKey(taskId);
    }

    public int getScheduledCount() {
        return scheduledFutures.size();
    }


    private void loadAndScheduleAllTasks() {
        List<ScheduledTask> allTasks = scheduledTaskRepository.findAllEnabled();
        for (ScheduledTask task : allTasks) {
            scheduleTask(task);
        }
        log.info("已加载并注册 {} 个定时任务", allTasks.size());
    }

    private void executeTask(ScheduledTask task) {
        log.info("开始执行定时任务: {} (ID={})", task.getName(), task.getId());
        try {
            Agent agent = findAgentForTask(task);
            if (agent == null) {
                log.warn("定时任务关联的Agent不存在，跳过: {}", task.getName());
                return;
            }
            String sessionId = "scheduled-" + task.getId() + "-" + System.currentTimeMillis();
            com.agenthub.application.command.AgentChatCommand command =
                    new com.agenthub.application.command.AgentChatCommand();
            command.setAgentId(agent.getId());
            command.setSessionId(sessionId);
            command.setUserMessage(task.getPrompt());
            agentChatPort.chatMessages(command);
            updateTaskAfterExecution(task, true, "执行成功");
            log.info("定时任务执行完成: {}", task.getName());
        } catch (Exception e) {
            log.error("定时任务执行失败: {}", task.getName(), e);
            updateTaskAfterExecution(task, false, e.getMessage());
        }
    }

    private Agent findAgentForTask(ScheduledTask task) {
        if (task.getAgentId() != null && !task.getAgentId().isBlank()) {
            return agentRepository.findById(task.getAgentId()).orElse(null);
        }
        return agentRepository.findAll().stream()
                .filter(a -> a.getWorkspaceId().equals(task.getWorkspaceId()))
                .findFirst()
                .orElse(null);
    }

    private void updateTaskAfterExecution(ScheduledTask task, boolean success, String result) {
        task.setLastExecuteTime(LocalDateTime.now());
        task.setStatus(success ? "SUCCESS" : "FAILED");
        task.setLastRunResult(result);
        task.setRunCount(task.getRunCount() + 1);
        task.setUpdatedAt(LocalDateTime.now());
        scheduledTaskRepository.saveOrUpdate(task);
    }
}
