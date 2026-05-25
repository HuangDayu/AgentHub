package com.agenthub.infrastructure.store.db.repository;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.agenthub.application.port.out.repositories.MetricRepository;
import com.agenthub.domain.model.monitor.Metric;
import com.agenthub.infrastructure.store.db.entity.MetricEntity;
import com.agenthub.infrastructure.store.db.mapper.MetricMybatisMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import tools.jackson.core.type.TypeReference;

import java.util.List;

import static org.springframework.ai.util.json.JsonParser.fromJson;
import static org.springframework.ai.util.json.JsonParser.toJson;

/**
 * Metric Repository 实现.
 */
@Repository
@RequiredArgsConstructor
public class MybatisMetricRepository implements MetricRepository {
    private final MetricMybatisMapper mapper;

    @Override
    public Metric save(Metric metric) {
        MetricEntity entity = toEntity(metric);
        mapper.insert(entity);
        return toDomain(entity);
    }

    @Override
    public List<Metric> findByRunId(String runId) {
        LambdaQueryWrapper<MetricEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MetricEntity::getRunId, runId);
        return mapper.selectList(wrapper).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Metric> findByAgentId(String agentId) {
        LambdaQueryWrapper<MetricEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MetricEntity::getAgentId, agentId);
        return mapper.selectList(wrapper).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Metric> findByMetricType(String metricType) {
        LambdaQueryWrapper<MetricEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MetricEntity::getMetricType, metricType);
        return mapper.selectList(wrapper).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Metric> findAll() {
        return mapper.selectList(null).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void deleteById(String id) {
        mapper.deleteById(id);
    }

    private MetricEntity toEntity(Metric metric) {
        MetricEntity metricEntity = new MetricEntity();
        BeanUtil.copyProperties(metric, metricEntity, labelsCopyOptions());
        metricEntity.setLabels(toJson(metric.getLabels()));
        return metricEntity;
    }

    private Metric toDomain(MetricEntity entity) {
        Metric metric = new Metric();
        BeanUtil.copyProperties(entity, metric, labelsCopyOptions());
        metric.setLabels(fromJson(entity.getLabels(), new TypeReference<>() {
        }));
        return metric;
    }

    private CopyOptions labelsCopyOptions() {
        return CopyOptions.create().setIgnoreProperties("labels");
    }
}
