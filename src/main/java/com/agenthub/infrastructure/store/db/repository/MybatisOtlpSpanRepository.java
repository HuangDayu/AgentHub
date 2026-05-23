package com.agenthub.infrastructure.store.db.repository;

import com.agenthub.domain.model.telemetry.OtlpSpan;
import com.agenthub.application.port.out.OtlpSpanRepository;
import com.agenthub.infrastructure.store.db.entity.OtlpSpanEntity;
import com.agenthub.infrastructure.store.db.mapper.OtlpSpanMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * OTLP Span仓储实现
 */
@Repository
@RequiredArgsConstructor
public class MybatisOtlpSpanRepository implements OtlpSpanRepository {
    private final OtlpSpanMapper mapper;

    @Override
    public void save(OtlpSpan span) {
        OtlpSpanEntity entity = toEntity(span);
        entity.setCreatedAt(Instant.now());
        mapper.insert(entity);
    }

    @Override
    public List<OtlpSpan> findRecent(int limit) {
        return mapper.selectList(
            new LambdaQueryWrapper<OtlpSpanEntity>()
                .orderByDesc(OtlpSpanEntity::getCreatedAt)
                .last("LIMIT " + limit)
        ).stream().map(this::toDomain).toList();
    }

    @Override
    public List<OtlpSpan> findByTraceId(String traceId) {
        return mapper.selectList(
            new LambdaQueryWrapper<OtlpSpanEntity>()
                .eq(OtlpSpanEntity::getTraceId, traceId)
                .orderByAsc(OtlpSpanEntity::getStartTimestamp)
        ).stream().map(this::toDomain).toList();
    }

    @Override
    public List<OtlpSpan> findByServiceName(String serviceName, int limit) {
        return mapper.selectList(
            new LambdaQueryWrapper<OtlpSpanEntity>()
                .eq(OtlpSpanEntity::getServiceName, serviceName)
                .orderByDesc(OtlpSpanEntity::getCreatedAt)
                .last("LIMIT " + limit)
        ).stream().map(this::toDomain).toList();
    }

    @Override
    public long count() {
        return mapper.selectCount(null);
    }

    private OtlpSpanEntity toEntity(OtlpSpan span) {
        OtlpSpanEntity entity = new OtlpSpanEntity();
        entity.setSpanId(span.getSpanId());
        entity.setTraceId(span.getTraceId());
        entity.setParentSpanId(span.getParentSpanId());
        entity.setOperationName(span.getOperationName());
        entity.setServiceName(span.getServiceName());
        entity.setKind(span.getKind());
        entity.setStartTimestamp(span.getStartTimestamp());
        entity.setEndTimestamp(span.getEndTimestamp());
        entity.setDuration(span.getDuration());
        entity.setStatus(span.getStatus());
        entity.setStatusDescription(span.getStatusDescription());
        entity.setAttributes(span.getAttributes());
        entity.setEvents(span.getEvents());
        entity.setLinks(span.getLinks());
        return entity;
    }

    private OtlpSpan toDomain(OtlpSpanEntity entity) {
        OtlpSpan span = new OtlpSpan();
        span.setSpanId(entity.getSpanId());
        span.setTraceId(entity.getTraceId());
        span.setParentSpanId(entity.getParentSpanId());
        span.setOperationName(entity.getOperationName());
        span.setServiceName(entity.getServiceName());
        span.setKind(entity.getKind());
        span.setStartTimestamp(entity.getStartTimestamp());
        span.setEndTimestamp(entity.getEndTimestamp());
        span.setDuration(entity.getDuration());
        span.setStatus(entity.getStatus());
        span.setStatusDescription(entity.getStatusDescription());
        span.setAttributes(entity.getAttributes());
        span.setEvents(entity.getEvents());
        span.setLinks(entity.getLinks());
        span.setCreatedAt(entity.getCreatedAt());
        return span;
    }
}
