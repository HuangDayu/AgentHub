package com.agenthub.infrastructure.store.db.repository;

import com.agenthub.application.port.out.repositories.McpToolRepository;
import com.agenthub.domain.model.McpTool;
import com.agenthub.infrastructure.store.db.entity.McpToolEntity;
import com.agenthub.infrastructure.store.db.mapper.McpToolMybatisMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Optional;

import static org.springframework.ai.util.json.JsonParser.fromJson;
import static org.springframework.ai.util.json.JsonParser.toJson;

@Component
@Primary
public class MybatisMcpToolRepository implements McpToolRepository {
    private final McpToolMybatisMapper mapper;

    public MybatisMcpToolRepository(McpToolMybatisMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public McpTool save(McpTool tool) {
        McpToolEntity entity = toEntity(tool);
        mapper.insert(entity);
        return toDomain(entity);
    }

    @Override
    public Optional<McpTool> findById(String id) {
        return Optional.ofNullable(mapper.selectById(id)).map(this::toDomain);
    }

    @Override
    public List<McpTool> findList() {
        return mapper.selectList(new LambdaQueryWrapper<>()).stream().map(this::toDomain).toList();
    }

    @Override
    public List<McpTool> findByWorkspaceId(String workspaceId) {
        return mapper.selectByWorkspaceId(workspaceId).stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(String id) {
        mapper.deleteById(id);
    }

    @Override
    public McpTool update(McpTool tool) {
        McpToolEntity entity = toEntity(tool);
        mapper.updateById(entity);
        return toDomain(entity);
    }

    private McpToolEntity toEntity(McpTool tool) {
        McpToolEntity entity = new McpToolEntity();
        entity.setId(tool.id());
        entity.setTenantId(tool.tenantId());
        entity.setWorkspaceId(tool.workspaceId());
        entity.setName(tool.name());
        entity.setDescription(tool.description());
        entity.setServerUrl(tool.serverUrl());
        entity.setServerType(tool.serverType().name());
        entity.setCommand(tool.command());
        entity.setArgs(toJson(tool.args()));
        entity.setEnv(toJson(tool.env()));
        entity.setAsync(tool.async());
        entity.setEnabled(tool.enabled());
        entity.setCreatedAt(tool.createdAt());
        entity.setUpdatedAt(tool.updatedAt());
        return entity;
    }

    private McpTool toDomain(McpToolEntity entity) {
        return new McpTool(
                entity.getId(),
                entity.getTenantId(),
                entity.getWorkspaceId(),
                entity.getName(),
                entity.getDescription(),
                entity.getServerUrl(),
                McpTool.ServerType.valueOf(entity.getServerType()),
                entity.getCommand(),
                fromJson(entity.getArgs(), new TypeReference<>() {
                }),
                fromJson(entity.getEnv(), new TypeReference<>() {
                }),
                entity.isAsync(),
                entity.isEnabled(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
