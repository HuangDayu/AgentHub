package com.agenthub.infrastructure.store.db.repository;

import com.agenthub.domain.model.telemetry.OtlpLog;
import com.agenthub.application.port.out.OtlpLogRepository;
import com.agenthub.infrastructure.store.db.entity.OtlpLogEntity;
import com.agenthub.infrastructure.store.db.mapper.OtlpLogMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * OTLP Log仓储实现
 */
@Repository
@RequiredArgsConstructor
public class MybatisOtlpLogRepository implements OtlpLogRepository {
    private final OtlpLogMapper mapper;

    @Override
    public void save(OtlpLog log) {
        OtlpLogEntity entity = toEntity(log);
        entity.setCreatedAt(Instant.now());
        mapper.insert(entity);
    }

    @Override
    public List<OtlpLog> findRecent(int limit) {
        return mapper.selectList(
            new LambdaQueryWrapper<OtlpLogEntity>()
                .orderByDesc(OtlpLogEntity::getCreatedAt)
                .last("LIMIT " + limit)
        ).stream().map(this::toDomain).toList();
    }

    @Override
    public long count() {
        return mapper.selectCount(null);
    }

    private OtlpLogEntity toEntity(OtlpLog log) {
        OtlpLogEntity entity = new OtlpLogEntity();
        entity.setLogId(log.getLogId());
        entity.setTraceId(log.getTraceId());
        entity.setSpanId(log.getSpanId());
        entity.setServiceName(log.getServiceName());
        entity.setSeverity(log.getSeverity());
        entity.setSeverityNumber(log.getSeverityNumber());
        entity.setBody(log.getBody());
        entity.setAttributes(log.getAttributes());
        entity.setTimestamp(log.getTimestamp());
        return entity;
    }

    private OtlpLog toDomain(OtlpLogEntity entity) {
        OtlpLog log = new OtlpLog();
        log.setLogId(entity.getLogId());
        log.setTraceId(entity.getTraceId());
        log.setSpanId(entity.getSpanId());
        log.setServiceName(entity.getServiceName());
        log.setSeverity(entity.getSeverity());
        log.setSeverityNumber(entity.getSeverityNumber());
        log.setBody(entity.getBody());
        log.setAttributes(entity.getAttributes());
        log.setTimestamp(entity.getTimestamp());
        log.setCreatedAt(entity.getCreatedAt());
        return log;
    }
}
