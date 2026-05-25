package com.agenthub.infrastructure.store.db.repository;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.application.port.out.repositories.TraceRepository;
import com.agenthub.domain.model.trace.Trace;
import com.agenthub.infrastructure.store.db.entity.TraceEntity;
import com.agenthub.infrastructure.store.db.mapper.TraceMybatisMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Trace Repository 实现.
 */
@Repository
@RequiredArgsConstructor
public class MybatisTraceRepository implements TraceRepository {
    private final TraceMybatisMapper mapper;

    @Override
    public Trace save(Trace trace) {
        TraceEntity entity = toEntity(trace);
        mapper.insert(entity);
        return toDomain(entity);
    }

    @Override
    public Optional<Trace> findById(String id) {
        return Optional.ofNullable(mapper.selectById(id))
            .map(this::toDomain);
    }

    @Override
    public Optional<Trace> findByTraceId(String traceId) {
        LambdaQueryWrapper<TraceEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TraceEntity::getTraceId, traceId);
        return Optional.ofNullable(mapper.selectOne(wrapper))
            .map(this::toDomain);
    }

    @Override
    public List<Trace> findByRunId(String runId) {
        LambdaQueryWrapper<TraceEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TraceEntity::getRunId, runId);
        return mapper.selectList(wrapper).stream()
            .map(this::toDomain)
            .toList();
    }

    @Override
    public List<Trace> findAll() {
        return mapper.selectList(null).stream()
            .map(this::toDomain)
            .toList();
    }

    @Override
    public void deleteById(String id) {
        mapper.deleteById(id);
    }

    private TraceEntity toEntity(Trace trace) {
        return BeanUtil.copyProperties(trace, TraceEntity.class);
    }

    private Trace toDomain(TraceEntity entity) {
        return BeanUtil.copyProperties(entity, Trace.class);
    }
}
