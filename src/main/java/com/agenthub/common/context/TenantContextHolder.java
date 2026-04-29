package com.agenthub.common.context;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.alibaba.ttl.threadpool.TtlExecutors;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 租户上下文持有者。
 * <p>
 * 使用ThreadLocal管理当前线程的租户上下文信息。
 * </p>
 */
public final class TenantContextHolder {

    /**
     * 当前线程的租户上下文
     */
    private static final TransmittableThreadLocal<TenantThreadContext> CURRENT = new TransmittableThreadLocal<>();

    /**
     * TTL 框架装饰后的线程池保证跨线程池时能够保证上下文不丢失
     * https://github.com/alibaba/transmittable-thread-local
     */
    private static final ExecutorService executorService = TtlExecutors.getTtlExecutorService(Executors.newVirtualThreadPerTaskExecutor());

    public static ExecutorService getExecutorService() {
        return executorService;
    }

    /**
     * 私有构造函数，防止实例化。
     */
    private TenantContextHolder() {
    }

    /**
     * 获取当前租户上下文。
     *
     * @return 租户上下文的Optional包装
     */
    public static Optional<TenantThreadContext> current() {
        return Optional.ofNullable(CURRENT.get());
    }

    /**
     * 打开新的租户上下文作用域。
     *
     * @param context 新的租户上下文
     * @return 上下文作用域对象，支持try-with-resources
     */
    public static TenantContextScope open(TenantThreadContext context) {
        TenantThreadContext previous = CURRENT.get();
        CURRENT.set(context);
        return new TenantContextScope(previous);
    }

    /**
     * 清除当前租户上下文。
     */
    public static void clear() {
        CURRENT.remove();
    }

    /**
     * 租户上下文作用域类。
     * <p>
     * 实现AutoCloseable，支持try-with-resources自动恢复之前的上下文。
     * </p>
     */
    public static final class TenantContextScope implements AutoCloseable {

        /**
         * 之前的租户上下文
         */
        private final TenantThreadContext previous;

        /**
         * 私有构造函数。
         */
        private TenantContextScope(TenantThreadContext previous) {
            this.previous = previous;
        }

        /**
         * 关闭作用域，恢复之前的上下文。
         */
        @Override
        public void close() {
            if (previous == null) {
                clear();
                return;
            }
            CURRENT.set(previous);
        }
    }
}
