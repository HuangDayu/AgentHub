package com.agenthub.infrastructure.store.db.repository;

import com.agenthub.application.port.out.repositories.SystemToolsRepository;
import com.agenthub.domain.model.tools.SystemTool;
import com.agenthub.infrastructure.store.db.entity.SystemToolsEntity;
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

    private SystemToolsEntity syncTool(SystemTool tool) {
        SystemToolsEntity newEntity = toEntity(tool);
        LambdaQueryWrapper<SystemToolsEntity> w = new LambdaQueryWrapper<>();
        w.eq(SystemToolsEntity::getToolClassName, tool.getToolClassName());
        SystemToolsEntity oldEntity = mapper.selectOne(w);
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
        LambdaQueryWrapper<SystemToolsEntity> w = new LambdaQueryWrapper<>();
        w.eq(SystemToolsEntity::isEnabled, enabled);
        return mapper.selectList(w).stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<SystemTool> findByToolClassName(String name) {
        LambdaQueryWrapper<SystemToolsEntity> w = new LambdaQueryWrapper<>();
        w.eq(SystemToolsEntity::getToolClassName, name);
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
        LambdaQueryWrapper<SystemToolsEntity> w = new LambdaQueryWrapper<>();
        w.eq(SystemToolsEntity::getWorkspaceId, workspaceId);
        return mapper.selectList(w).stream().map(this::toDomain).toList();
    }

    @Override
    public List<SystemTool> findByIds(List<String> toolIds) {
        if (toolIds == null || toolIds.isEmpty()) {
            return List.of();
        }
        return mapper.selectByIds(toolIds).stream().map(this::toDomain).toList();
    }

    private SystemToolsEntity toEntity(SystemTool tool) {
        SystemToolsEntity e = new SystemToolsEntity();
        e.setId(tool.getId());
        e.setTenantId(tool.getTenantId());
        e.setToolClassName(tool.getToolClassName());
        e.setToolName(tool.getToolName());
        e.setDescription(tool.getDescription());
        e.setCategory(tool.getCategory());
        e.setMethodCount(tool.getMethodCount());
        e.setDescription(tool.getDescription());
        e.setEnabled(tool.isEnabled());
        e.setSystemTool(tool.isSystemTool());
        e.setCreatedAt(tool.getCreatedAt());
        e.setUpdatedAt(tool.getUpdatedAt());
        return e;
    }

    private SystemTool toDomain(SystemToolsEntity e) {
        SystemTool t = new SystemTool();
        t.setId(e.getId());
        t.setTenantId(e.getTenantId());
        t.setToolClassName(e.getToolClassName());
        t.setToolName(e.getToolName());
        t.setDescription(e.getDescription());
        t.setCategory(e.getCategory());
        t.setMethodCount(e.getMethodCount());
        t.setDescription(e.getDescription());
        t.setEnabled(e.isEnabled());
        t.setSystemTool(e.isSystemTool());
        t.setCreatedAt(e.getCreatedAt());
        t.setUpdatedAt(e.getUpdatedAt());
        return t;
    }
}
