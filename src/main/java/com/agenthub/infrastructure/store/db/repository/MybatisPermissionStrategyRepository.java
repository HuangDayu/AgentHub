package com.agenthub.infrastructure.store.db.repository;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.agenthub.application.port.out.repositories.PermissionStrategyRepository;
import com.agenthub.domain.enums.AgentDataSourceProtocol;
import com.agenthub.domain.enums.OperationLevel;
import com.agenthub.domain.enums.TableOperation;
import com.agenthub.domain.model.PermissionStrategy;
import com.agenthub.infrastructure.store.db.entity.PermissionStrategyEntity;
import com.agenthub.infrastructure.store.db.mapper.PermissionStrategyMybatisMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 权限策略 MyBatis 仓储
 */
@Repository
@Primary
public class MybatisPermissionStrategyRepository implements PermissionStrategyRepository {
    private final PermissionStrategyMybatisMapper mapper;

    public MybatisPermissionStrategyRepository(PermissionStrategyMybatisMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public PermissionStrategy save(PermissionStrategy policy) {
        PermissionStrategyEntity e = toEntity(policy);
        if (e.getId() == null || e.getId().isBlank()
                || mapper.selectById(e.getId()) == null) {
            mapper.insert(e);
        } else {
            mapper.updateById(e);
        }
        return toDomain(mapper.selectById(e.getId()));
    }

    @Override
    public Optional<PermissionStrategy> findById(String id) {
        return Optional.ofNullable(mapper.selectById(id)).map(this::toDomain);
    }

    @Override
    public List<PermissionStrategy> findByWorkspaceId(String workspaceId) {
        LambdaQueryWrapper<PermissionStrategyEntity> q = new LambdaQueryWrapper<>();
        q.eq(PermissionStrategyEntity::getWorkspaceId, workspaceId)
         .orderByAsc(PermissionStrategyEntity::getCreatedAt);
        return mapper.selectList(q).stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(String id) {
        mapper.deleteById(id);
    }

    @Override
    public boolean existsByWorkspaceIdAndName(String workspaceId, String name) {
        LambdaQueryWrapper<PermissionStrategyEntity> q = new LambdaQueryWrapper<>();
        q.eq(PermissionStrategyEntity::getWorkspaceId, workspaceId)
         .eq(PermissionStrategyEntity::getName, name);
        return mapper.selectCount(q) > 0;
    }

    private PermissionStrategyEntity toEntity(PermissionStrategy p) {
        PermissionStrategyEntity e = new PermissionStrategyEntity();
        BeanUtil.copyProperties(p, e, CopyOptions.create().setIgnoreProperties("allowedRoles", "allowedOperations", "protocolBlocklist", "requireApprovalFor", "tablePermissions"));
        e.setAllowedRoles(join(p.getAllowedRoles()));
        e.setAllowedOperations(joinEnums(p.getAllowedOperations(), OperationLevel.class));
        e.setProtocolBlocklist(joinEnums(p.getProtocolBlocklist(), AgentDataSourceProtocol.class));
        e.setRequireApprovalFor(joinEnums(p.getRequireApprovalFor(), TableOperation.class));
        e.setTablePermissions(serializeTablePermissions(p.getTablePermissions()));
        return e;
    }

    private PermissionStrategy toDomain(PermissionStrategyEntity e) {
        if (e == null) return null;
        PermissionStrategy p = new PermissionStrategy();
        BeanUtil.copyProperties(e, p, CopyOptions.create().setIgnoreProperties("dangerousSqlBlock", "rateLimitPerMinute", "rateLimitPerHour", "auditLogEnabled", "auditLogRetentionDays", "piiMaskingOnResult", "allowedRoles", "allowedOperations", "protocolBlocklist", "requireApprovalFor", "tablePermissions"));
        setNullSafeBooleans(p, e);
        setNullSafeInts(p, e);
        setCollectionFields(p, e);
        p.setTablePermissions(deserializeTablePermissions(e.getTablePermissions()));
        return p;
    }

    private void setNullSafeBooleans(PermissionStrategy p, PermissionStrategyEntity e) {
        p.setDangerousSqlBlock(Boolean.TRUE.equals(e.getDangerousSqlBlock()));
        p.setAuditLogEnabled(Boolean.TRUE.equals(e.getAuditLogEnabled()));
        p.setPiiMaskingOnResult(Boolean.TRUE.equals(e.getPiiMaskingOnResult()));
    }

    private void setNullSafeInts(PermissionStrategy p, PermissionStrategyEntity e) {
        p.setRateLimitPerMinute(e.getRateLimitPerMinute() == null ? 0 : e.getRateLimitPerMinute());
        p.setRateLimitPerHour(e.getRateLimitPerHour() == null ? 0 : e.getRateLimitPerHour());
        p.setAuditLogRetentionDays(e.getAuditLogRetentionDays() == null ? 0 : e.getAuditLogRetentionDays());
    }

    private void setCollectionFields(PermissionStrategy p, PermissionStrategyEntity e) {
        p.setAllowedRoles(splitToSet(e.getAllowedRoles()));
        p.setAllowedOperations(splitEnums(e.getAllowedOperations(), OperationLevel.class));
        p.setProtocolBlocklist(splitEnums(e.getProtocolBlocklist(), AgentDataSourceProtocol.class));
        p.setRequireApprovalFor(splitEnums(e.getRequireApprovalFor(), TableOperation.class));
    }

    private String serializeTablePermissions(Map<String, Set<TableOperation>> perms) {
        if (perms == null) return null;
        Map<String, String> map = new HashMap<>();
        perms.forEach((k, v) -> map.put(k, joinEnums(v, TableOperation.class)));
        return JSONUtil.toJsonStr(map);
    }

    private Map<String, Set<TableOperation>> deserializeTablePermissions(String json) {
        if (StrUtil.isBlank(json)) return null;
        Map<String, String> raw = JSONUtil.toBean(json, Map.class);
        Map<String, Set<TableOperation>> map = new HashMap<>();
        raw.forEach((k, v) -> map.put(k, splitEnums(v, TableOperation.class)));
        return map;
    }

    private String join(Set<String> s) {
        return s == null ? "" : String.join(",", s);
    }

    private <E extends Enum<E>> String joinEnums(Set<E> s, Class<E> clazz) {
        if (s == null) return "";
        return s.stream().map(Enum::name).collect(Collectors.joining(","));
    }

    private Set<String> splitToSet(String s) {
        if (StrUtil.isBlank(s)) return new HashSet<>();
        return Arrays.stream(s.split(",")).map(String::trim).collect(Collectors.toSet());
    }

    private <E extends Enum<E>> Set<E> splitEnums(String s, Class<E> clazz) {
        Set<E> result = new HashSet<>();
        if (StrUtil.isBlank(s)) return result;
        for (String part : s.split(",")) {
            if (part.isBlank()) continue;
            try {
                result.add(Enum.valueOf(clazz, part.trim()));
            } catch (IllegalArgumentException ignored) {
                // 忽略未知枚举
            }
        }
        return result;
    }
}
