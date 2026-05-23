package com.agenthub.application.port.out;
import com.agenthub.domain.model.telemetry.OtlpLog;

import java.util.List;

/**
 * OTLP Log仓储接口
 */
public interface OtlpLogRepository {
    void save(OtlpLog log);
    List<OtlpLog> findRecent(int limit);
    long count();
}
