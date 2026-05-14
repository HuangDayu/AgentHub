package com.agenthub.domain.exception;

/**
 * 入库任务未找到异常。
 * <p>
 * 当请求的入库任务不存在时抛出此异常。
 * </p>
 */
public class JobNotFoundException extends RuntimeException {

    public JobNotFoundException(String jobId) {
        super("Ingestion job not found: " + jobId);
    }
}
