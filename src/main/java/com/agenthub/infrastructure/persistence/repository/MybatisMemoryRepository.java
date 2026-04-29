package com.agenthub.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.agenthub.application.port.out.repositories.MemoryRepository;
import com.agenthub.domain.model.Memory;
import com.agenthub.infrastructure.persistence.entity.MemoryEntity;
import com.agenthub.infrastructure.persistence.mapper.MemoryMybatisMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Primary
public class MybatisMemoryRepository implements MemoryRepository {
    private final MemoryMybatisMapper mapper;

    public MybatisMemoryRepository(MemoryMybatisMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Memory save(Memory memory) {
        MemoryEntity entity = toEntity(memory);
        mapper.insertOrUpdate(entity);
        return memory;
    }

    @Override
    public Optional<Memory> findById(String memoryId) {
        return Optional.ofNullable(mapper.selectById(memoryId)).map(this::toDomain);
    }

    @Override
    public List<Memory> findByAgentId(String agentId) {
        LambdaQueryWrapper<MemoryEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemoryEntity::getAgentId, agentId);
        return mapper.selectList(wrapper).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Memory> findByTenantIdAndWorkspaceId(String tenantId, String workspaceId) {
        LambdaQueryWrapper<MemoryEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemoryEntity::getTenantId, tenantId)
               .eq(MemoryEntity::getWorkspaceId, workspaceId);
        return mapper.selectList(wrapper).stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(String memoryId) {
        mapper.deleteById(memoryId);
    }

    @Override
    public void deleteByAgentId(String agentId) {
        LambdaQueryWrapper<MemoryEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemoryEntity::getAgentId, agentId);
        mapper.delete(wrapper);
    }

    private MemoryEntity toEntity(Memory memory) {
        MemoryEntity entity = new MemoryEntity();
        entity.setId(memory.getId());
        entity.setTenantId(memory.getTenantId());
        entity.setWorkspaceId(memory.getWorkspaceId());
        entity.setAgentId(memory.getAgentId());
        entity.setMemoryType(memory.getMemoryType());
        entity.setContent(memory.getContent());
        entity.setMetadata(memory.getMetadata());
        entity.setImportance(memory.getImportance());
        entity.setExpiresAt(memory.getExpiresAt());
        entity.setCreatedAt(memory.getCreatedAt());
        entity.setUpdatedAt(memory.getUpdatedAt());
        return entity;
    }

    private Memory toDomain(MemoryEntity entity) {
        Memory memory = new Memory();
        memory.setId(entity.getId());
        memory.setTenantId(entity.getTenantId());
        memory.setWorkspaceId(entity.getWorkspaceId());
        memory.setAgentId(entity.getAgentId());
        memory.setMemoryType(entity.getMemoryType());
        memory.setContent(entity.getContent());
        memory.setMetadata(entity.getMetadata());
        memory.setImportance(entity.getImportance());
        memory.setExpiresAt(entity.getExpiresAt());
        memory.setCreatedAt(entity.getCreatedAt());
        memory.setUpdatedAt(entity.getUpdatedAt());
        return memory;
    }
}
