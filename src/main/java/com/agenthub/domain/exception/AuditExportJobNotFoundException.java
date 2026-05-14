package com.agenthub.domain.exception;

/**
 * 审计导出任务未找到异常。
 *
 * <p>当根据任务 ID 查询不到对应导出任务时抛出。</p>
 */
public class AuditExportJobNotFoundException extends RuntimeException {
    public AuditExportJobNotFoundException(String jobId) {
        super("Audit export job not found: " + jobId);
    }
}
