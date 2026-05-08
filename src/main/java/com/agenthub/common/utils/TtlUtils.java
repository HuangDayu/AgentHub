package com.agenthub.common.utils;

import com.alibaba.ttl.threadpool.TtlExecutors;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author huangdayu
 */
public class TtlUtils {

    /**
     * TTL 框架装饰后的线程池保证跨线程池时能够保证上下文不丢失
     * https://github.com/alibaba/transmittable-thread-local
     */
    private static final ExecutorService executorService = TtlExecutors.getTtlExecutorService(Executors.newVirtualThreadPerTaskExecutor());

    public static ExecutorService getTtlExecutorService() {
        return executorService;
    }

    public static <T, R> Collection<R> parallelStreamWithTtl(int parallelism, Collection<T> list, Function<T, R> mapper) {
        ExecutorService pool = TtlExecutors.getTtlExecutorService(new ForkJoinPool(parallelism));
        try {
            return pool.submit(() -> list.parallelStream().map(mapper).collect(Collectors.toList())).get();
        } catch (Exception e) {
            throw new RuntimeException("Parallel stream execution failed", e);
        } finally {
            pool.shutdown();
        }
    }

}
