package com.agenthub.infrastructure.store.db.repository;

import com.agenthub.domain.model.telemetry.OtlpMetric;
import com.agenthub.application.port.out.repositories.OtlpMetricRepository;
import com.agenthub.infrastructure.store.db.entity.OtlpMetricEntity;
import com.agenthub.infrastructure.store.db.mapper.OtlpMetricMybatisMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * OTLP Metric仓储实现
 */
@Repository
@RequiredArgsConstructor
public class MybatisOtlpMetricRepository implements OtlpMetricRepository {
    private final OtlpMetricMybatisMapper mapper;

    @Override
    public void save(OtlpMetric metric) {
        OtlpMetricEntity entity = toEntity(metric);
        entity.setCreatedAt(Instant.now());
        mapper.insert(entity);
    }

    @Override
    public List<OtlpMetric> findRecent(int limit) {
        return mapper.selectList(
            new LambdaQueryWrapper<OtlpMetricEntity>()
                .orderByDesc(OtlpMetricEntity::getCreatedAt)
                .last("LIMIT " + limit)
        ).stream().map(this::toDomain).toList();
    }

    @Override
    public long count() {
        return mapper.selectCount(null);
    }

    private OtlpMetricEntity toEntity(OtlpMetric metric) {
        OtlpMetricEntity entity = new OtlpMetricEntity();
        entity.setMetricName(metric.getMetricName());
        entity.setDescription(metric.getDescription());
        entity.setUnit(metric.getUnit());
        entity.setMetricType(metric.getMetricType());
        entity.setServiceName(metric.getServiceName());
        entity.setValue(metric.getValue());
        entity.setAttributes(metric.getAttributes());
        entity.setTimestamp(metric.getTimestamp());
        return entity;
    }

    private OtlpMetric toDomain(OtlpMetricEntity entity) {
        OtlpMetric metric = new OtlpMetric();
        metric.setMetricName(entity.getMetricName());
        metric.setDescription(entity.getDescription());
        metric.setUnit(entity.getUnit());
        metric.setMetricType(entity.getMetricType());
        metric.setServiceName(entity.getServiceName());
        metric.setValue(entity.getValue());
        metric.setAttributes(entity.getAttributes());
        metric.setTimestamp(entity.getTimestamp());
        metric.setCreatedAt(entity.getCreatedAt());
        return metric;
    }
}
