package com.agenthub.infrastructure.store.cache;

import com.agenthub.application.command.RateLimitCheckCommand;
import com.agenthub.application.port.out.DataSourcePermissionPort;
import com.agenthub.domain.exception.RateLimitExceededException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 滑动窗口速率限制器 - 内存实现
 */
@Component
public class InMemoryRateLimiter implements DataSourcePermissionPort {
    private final ConcurrentHashMap<String, Deque<Instant>> minuteCounters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Deque<Instant>> hourCounters = new ConcurrentHashMap<>();

    @Override
    public void checkRateLimit(RateLimitCheckCommand cmd) {
        if (cmd.getPerMinute() <= 0 && cmd.getPerHour() <= 0) return;
        String key = cmd.getUserId() + ":" + cmd.getDataSourceId();
        Instant now = Instant.now();
        checkWindow(new WindowSpec(key, now, minuteCounters, 60, cmd.getPerMinute(), "min"));
        checkWindow(new WindowSpec(key, now, hourCounters, 3600, cmd.getPerHour(), "hour"));
    }

    /**
     * 滑动窗口检查
     */
    private void checkWindow(WindowSpec spec) {
        if (spec.limit() <= 0) return;
        Deque<Instant> dq = spec.counters().computeIfAbsent(spec.key(), k -> new ArrayDeque<>());
        synchronized (dq) {
            purge(dq, spec.now().minusSeconds(spec.windowSeconds()));
            enforceLimit(dq, spec.limit(), spec.unit());
            dq.offerLast(spec.now());
        }
    }

    private void purge(Deque<Instant> dq, Instant cutoff) {
        while (!dq.isEmpty() && dq.peekFirst().isBefore(cutoff)) dq.pollFirst();
    }

    private void enforceLimit(Deque<Instant> dq, int limit, String unit) {
        if (dq.size() >= limit) {
            throw new RateLimitExceededException("rate limit exceeded: " + limit + "/" + unit);
        }
    }

    /**
     * 窗口检查参数 - 打包为 record
     */
    private record WindowSpec(String key, Instant now, ConcurrentHashMap<String, Deque<Instant>> counters,
                               int windowSeconds, int limit, String unit) {}

    @Override
    public String getUserRole(String userId, String workspaceId) {
        return "DEVELOPER";
    }
}
