package com.agenthub.infrastructure.store.cache;

import com.agenthub.application.port.out.IdempotencyCachePort;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 测试用的幂等性缓存适配器.
 */
@Component
public class MemoryIdempotencyCache implements IdempotencyCachePort {
    
    private final Map<String, String> cache = new ConcurrentHashMap<>();
    
    @Override
    public Optional<String> getCachedResult(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(cache.get(idempotencyKey));
    }
    
    @Override
    public void cacheResult(String idempotencyKey, String result, int ttlSeconds) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return;
        }
        cache.put(idempotencyKey, result);
    }
    
    @Override
    public boolean exists(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return false;
        }
        return cache.containsKey(idempotencyKey);
    }
}
