package com.agenthub.infrastructure.store.db.repository;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.agenthub.application.port.out.repositories.AuditLogRepository;
import com.agenthub.domain.event.AuditEvent;
import com.agenthub.infrastructure.store.db.entity.AuditLogEntity;
import com.agenthub.infrastructure.store.db.mapper.AuditLogMybatisMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 全局审计日志 MyBatis 仓储
 */
@Repository
@Primary
public class MybatisAuditLogRepository implements AuditLogRepository {
    private final AuditLogMybatisMapper mapper;

    public MybatisAuditLogRepository(AuditLogMybatisMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void save(AuditEvent event) {
        mapper.insert(toEntity(event));
    }

    @Override
    public void saveAll(List<AuditEvent> events) {
        if (events == null || events.isEmpty()) return;
        List<AuditLogEntity> entities = events.stream().map(this::toEntity).toList();
        for (AuditLogEntity e : entities) {
            mapper.insert(e);
        }
    }

    @Override
    public List<AuditEvent> query(AuditLogQuery query) {
        LambdaQueryWrapper<AuditLogEntity> q = buildQuery(query);
        q.orderByDesc(AuditLogEntity::getCreatedAt);
        if (query.getPage() > 0 && query.getSize() > 0) {
            long offset = (long) (query.getPage() - 1) * query.getSize();
            q.last("LIMIT " + query.getSize() + " OFFSET " + offset);
        }
        return mapper.selectList(q).stream().map(this::toDomain).toList();
    }

    @Override
    public long count(AuditLogQuery query) {
        return mapper.selectCount(buildQuery(query));
    }

    private LambdaQueryWrapper<AuditLogEntity> buildQuery(AuditLogQuery query) {
        LambdaQueryWrapper<AuditLogEntity> q = new LambdaQueryWrapper<>();
        applyStringFilters(q, query);
        applyDateFilters(q, query);
        return q;
    }

    private void applyStringFilters(LambdaQueryWrapper<AuditLogEntity> q, AuditLogQuery query) {
        if (StrUtil.isNotBlank(query.getTenantId())) q.eq(AuditLogEntity::getTenantId, query.getTenantId());
        if (StrUtil.isNotBlank(query.getWorkspaceId())) q.eq(AuditLogEntity::getWorkspaceId, query.getWorkspaceId());
        if (StrUtil.isNotBlank(query.getResourceType())) q.eq(AuditLogEntity::getResourceType, query.getResourceType());
        if (StrUtil.isNotBlank(query.getResourceId())) q.eq(AuditLogEntity::getResourceId, query.getResourceId());
        if (StrUtil.isNotBlank(query.getActorId())) q.eq(AuditLogEntity::getActorId, query.getActorId());
        if (StrUtil.isNotBlank(query.getAction())) q.eq(AuditLogEntity::getAction, query.getAction());
        if (StrUtil.isNotBlank(query.getStatus())) q.eq(AuditLogEntity::getStatus, query.getStatus());
    }

    private void applyDateFilters(LambdaQueryWrapper<AuditLogEntity> q, AuditLogQuery query) {
        if (query.getFrom() != null) q.ge(AuditLogEntity::getCreatedAt, query.getFrom());
        if (query.getTo() != null) q.le(AuditLogEntity::getCreatedAt, query.getTo());
    }

    private AuditLogEntity toEntity(AuditEvent e) {
        AuditLogEntity entity = new AuditLogEntity();
        BeanUtil.copyProperties(e, entity);
        setEnumFields(entity, e);
        setJsonFields(entity, e);
        return entity;
    }

    private void setEnumFields(AuditLogEntity entity, AuditEvent e) {
        if (e.getActorType() != null) entity.setActorType(e.getActorType().name());
        if (e.getResourceType() != null) entity.setResourceType(e.getResourceType().name());
        if (e.getAction() != null) entity.setAction(e.getAction().name());
        if (e.getStatus() != null) entity.setStatus(e.getStatus().name());
    }

    private void setJsonFields(AuditLogEntity entity, AuditEvent e) {
        if (e.getRequest() != null) entity.setRequest(JSONUtil.toJsonStr(e.getRequest()));
        if (e.getResponse() != null) entity.setResponse(JSONUtil.toJsonStr(e.getResponse()));
        if (e.getMetadata() != null) entity.setMetadata(JSONUtil.toJsonStr(e.getMetadata()));
    }

    private AuditEvent toDomain(AuditLogEntity e) {
        if (e == null) return null;
        AuditEvent event = new AuditEvent();
        BeanUtil.copyProperties(e, event, "metadata");
        event.setMetadata(parseMetadata(e.getMetadata()));
        return event;
    }

    private HashMap<String, Object> parseMetadata(String metadata) {
        if (StrUtil.isNotBlank(metadata)) {
            try {
                return new HashMap<>(JSONUtil.toBean(metadata, Map.class));
            } catch (Exception ignored) {}
        }
        return new HashMap<>();
    }
}
