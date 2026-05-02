package com.agenthub.application.port.out;

import java.util.Optional;

/**
 * 幂等性缓存端口.
 * <p>
 * 用于存储和检索工具调用的幂等性结果。
 * </p>
 */
public interface IdempotencyCachePort {
    
    /**
     * 尝试获取已缓存的结果.
     *
     * @param idempotencyKey 幂等键
     * @return 如果存在缓存结果则返回
     */
    Optional<String> getCachedResult(String idempotencyKey);
    
    /**
     * 缓存结果.
     *
     * @param idempotencyKey 幂等键
     * @param result         结果
     * @param ttlSeconds     过期时间（秒）
     */
    void cacheResult(String idempotencyKey, String result, int ttlSeconds);
    
    /**
     * 检查幂等键是否存在.
     *
     * @param idempotencyKey 幂等键
     * @return 如果存在返回true
     */
    boolean exists(String idempotencyKey);
}
