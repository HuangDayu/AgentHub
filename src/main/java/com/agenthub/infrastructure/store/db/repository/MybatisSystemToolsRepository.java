package com.agenthub.infrastructure.store.db.repository;

import com.agenthub.application.port.out.repositories.SystemToolsRepository;
import com.agenthub.domain.model.SystemTool;
import com.agenthub.infrastructure.store.db.entity.SystemEntity;
import com.agenthub.infrastructure.store.db.mapper.SystemToolsMybatisMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Primary
public class MybatisSystemToolsRepository implements SystemToolsRepository {
    private final SystemToolsMybatisMapper mapper;

    public MybatisSystemToolsRepository(SystemToolsMybatisMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public SystemTool insertOrUpdate(SystemTool tool) {
        mapper.insertOrUpdate(toEntity(tool));
        return tool;
    }

    @Override
    public List<SystemTool> syncTools(List<SystemTool> tools) {
        return tools.stream().map(tool -> toDomain(syncTool(tool))).collect(Collectors.toList());
    }

    private SystemEntity syncTool(SystemTool tool) {
        SystemEntity newEntity = toEntity(tool);
        LambdaQueryWrapper<SystemEntity> w = new LambdaQueryWrapper<>();
        w.eq(SystemEntity::getToolClassName, tool.getToolClassName());
        SystemEntity oldEntity = mapper.selectOne(w);
        if (oldEntity != null) {
            newEntity.setId(oldEntity.getId());
        }
        mapper.insertOrUpdate(newEntity);
        return newEntity;
    }

    @Override
    public Optional<SystemTool> findById(String id) {
        return Optional.ofNullable(mapper.selectById(id)).map(this::toDomain);
    }

    @Override
    public List<SystemTool> findAll() {
        return mapper.selectList(new LambdaQueryWrapper<>()).stream().map(this::toDomain).toList();
    }

    @Override
    public List<SystemTool> findByEnabled(boolean enabled) {
        LambdaQueryWrapper<SystemEntity> w = new LambdaQueryWrapper<>();
        w.eq(SystemEntity::isEnabled, enabled);
        return mapper.selectList(w).stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<SystemTool> findByToolClassName(String name) {
        LambdaQueryWrapper<SystemEntity> w = new LambdaQueryWrapper<>();
        w.eq(SystemEntity::getToolClassName, name);
        return Optional.ofNullable(mapper.selectOne(w)).map(this::toDomain);
    }

    @Override
    public void deleteById(String id) {
        mapper.deleteById(id);
    }

    @Override
    public void updateEnabled(String id, boolean enabled) {
        mapper.updateEnabled(id, enabled);
    }

    @Override
    public List<SystemTool> findByWorkspaceId(String workspaceId) {
        LambdaQueryWrapper<SystemEntity> w = new LambdaQueryWrapper<>();
        w.eq(SystemEntity::getWorkspaceId, workspaceId);
        return mapper.selectList(w).stream().map(this::toDomain).toList();
    }

    @Override
    public List<SystemTool> findByIds(List<String> toolIds) {
        if (toolIds == null || toolIds.isEmpty()) {
            return List.of();
        }
        return mapper.selectByIds(toolIds).stream().map(this::toDomain).toList();
    }

    private SystemEntity toEntity(SystemTool tool) {
        SystemEntity e = new SystemEntity();
        e.setId(tool.getId());
        e.setTenantId(tool.getTenantId());
        e.setToolClassName(tool.getToolClassName());
        e.setToolName(tool.getToolName());
        e.setDescription(tool.getDescription());
        e.setCategory(tool.getCategory());
        e.setMethodCount(tool.getMethodCount());
        e.setEnabled(tool.isEnabled());
        e.setSystemTool(tool.isSystemTool());
        e.setCreatedAt(tool.getCreatedAt());
        e.setUpdatedAt(tool.getUpdatedAt());
        return e;
    }

    private SystemTool toDomain(SystemEntity e) {
        SystemTool t = new SystemTool();
        t.setId(e.getId());
        t.setTenantId(e.getTenantId());
        t.setToolClassName(e.getToolClassName());
        t.setToolName(e.getToolName());
        t.setDescription(e.getDescription());
        t.setCategory(e.getCategory());
        t.setMethodCount(e.getMethodCount());
        t.setEnabled(e.isEnabled());
        t.setSystemTool(e.isSystemTool());
        t.setCreatedAt(e.getCreatedAt());
        t.setUpdatedAt(e.getUpdatedAt());
        return t;
    }
}
