package com.agenthub.infrastructure.store.db.repository;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.agenthub.application.port.out.repositories.SpanRepository;
import com.agenthub.domain.model.trace.Span;
import com.agenthub.infrastructure.store.db.entity.SpanEntity;
import com.agenthub.infrastructure.store.db.mapper.SpanMybatisMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import tools.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Optional;

import static org.springframework.ai.util.json.JsonParser.fromJson;
import static org.springframework.ai.util.json.JsonParser.toJson;

/**
 * Span Repository 实现.
 */
@Repository
@RequiredArgsConstructor
public class MybatisSpanRepository implements SpanRepository {
    private final SpanMybatisMapper mapper;

    @Override
    public Span save(Span span) {
        SpanEntity entity = toEntity(span);
        mapper.insert(entity);
        return toDomain(entity);
    }

    @Override
    public Optional<Span> findById(String id) {
        return Optional.ofNullable(mapper.selectById(id))
                .map(this::toDomain);
    }

    @Override
    public Optional<Span> findBySpanId(String spanId) {
        LambdaQueryWrapper<SpanEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SpanEntity::getSpanId, spanId);
        return Optional.ofNullable(mapper.selectOne(wrapper))
                .map(this::toDomain);
    }

    @Override
    public List<Span> findByTraceId(String traceId) {
        LambdaQueryWrapper<SpanEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SpanEntity::getTraceId, traceId);
        return mapper.selectList(wrapper).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Span> findByRunId(String runId) {
        LambdaQueryWrapper<SpanEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SpanEntity::getRunId, runId);
        return mapper.selectList(wrapper).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Span> findAll() {
        return mapper.selectList(null).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void deleteById(String id) {
        mapper.deleteById(id);
    }

    private SpanEntity toEntity(Span span) {
        SpanEntity spanEntity = new SpanEntity();
        BeanUtil.copyProperties(span, spanEntity, jsonCopyOptions());
        spanEntity.setAttributes(toJson(span.getAttributes()));
        spanEntity.setEvents(toJson(span.getEvents()));
        spanEntity.setLinks(toJson(span.getLinks()));
        spanEntity.setResource(toJson(span.getResource()));
        spanEntity.setScope(toJson(span.getScope()));
        return spanEntity;
    }

    private Span toDomain(SpanEntity entity) {
        Span span = new Span();
        BeanUtil.copyProperties(entity, span, jsonCopyOptions());
        span.setAttributes(fromJson(entity.getAttributes(), new TypeReference<>() {
        }));
        span.setEvents(fromJson(entity.getEvents(), new TypeReference<>() {
        }));
        span.setLinks(fromJson(entity.getLinks(), new TypeReference<>() {
        }));
        span.setResource(fromJson(entity.getResource(), new TypeReference<>() {
        }));
        span.setScope(fromJson(entity.getScope(), new TypeReference<>() {
        }));
        return span;
    }

    private CopyOptions jsonCopyOptions() {
        return CopyOptions.create()
                .setIgnoreProperties("attributes", "events", "links", "resource", "scope");
    }
}
