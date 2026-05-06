package com.agenthub.infrastructure.store.db.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.agenthub.application.port.out.repositories.FunctionToolsRepository;
import com.agenthub.domain.model.FunctionTool;
import com.agenthub.infrastructure.store.db.entity.FunctionToolsEntity;
import com.agenthub.infrastructure.store.db.mapper.FunctionToolsMybatisMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Primary
public class MybatisFunctionToolsRepository implements FunctionToolsRepository {
    private final FunctionToolsMybatisMapper mapper;

    public MybatisFunctionToolsRepository(FunctionToolsMybatisMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public FunctionTool save(FunctionTool tool) {
        mapper.insertOrUpdate(toEntity(tool));
        return tool;
    }

    @Override
    public Optional<FunctionTool> findById(String id) {
        return Optional.ofNullable(mapper.selectById(id)).map(this::toDomain);
    }

    @Override
    public List<FunctionTool> findAll() {
        return mapper.selectList(new LambdaQueryWrapper<>()).stream().map(this::toDomain).toList();
    }

    @Override
    public List<FunctionTool> findByEnabled(boolean enabled) {
        LambdaQueryWrapper<FunctionToolsEntity> w = new LambdaQueryWrapper<>();
        w.eq(FunctionToolsEntity::isEnabled, enabled);
        return mapper.selectList(w).stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<FunctionTool> findByToolClassName(String name) {
        LambdaQueryWrapper<FunctionToolsEntity> w = new LambdaQueryWrapper<>();
        w.eq(FunctionToolsEntity::getToolClassName, name);
        return Optional.ofNullable(mapper.selectOne(w)).map(this::toDomain);
    }

    @Override
    public void deleteById(String id) { mapper.deleteById(id); }

    @Override
    public void updateEnabled(String id, boolean enabled) { mapper.updateEnabled(id, enabled); }

    @Override
    public List<FunctionTool> findByWorkspaceId(String workspaceId) {
        LambdaQueryWrapper<FunctionToolsEntity> w = new LambdaQueryWrapper<>();
        w.eq(FunctionToolsEntity::getWorkspaceId, workspaceId);
        return mapper.selectList(w).stream().map(this::toDomain).toList();
    }

    private FunctionToolsEntity toEntity(FunctionTool tool) {
        FunctionToolsEntity e = new FunctionToolsEntity();
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

    private FunctionTool toDomain(FunctionToolsEntity e) {
        FunctionTool t = new FunctionTool();
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
