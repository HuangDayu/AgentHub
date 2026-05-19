package com.agenthub.infrastructure.store.db.repository;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.application.port.out.repositories.McpToolRepository;
import com.agenthub.domain.model.tools.McpTool;
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

    @Override
    public List<McpTool> findByIds(List<String> toolIds) {
        if (toolIds == null || toolIds.isEmpty()) {
            return List.of();
        }
        List<McpToolEntity> mcpToolEntities = mapper.selectByIds(toolIds);
        return mcpToolEntities.stream().map(this::toDomain).toList();
    }

    private McpToolEntity toEntity(McpTool tool) {
        McpToolEntity mcpToolEntity = BeanUtil.copyProperties(tool, McpToolEntity.class);
        mcpToolEntity.setArgs(toJson(tool.getArgs()));
        mcpToolEntity.setEnv(toJson(tool.getEnv()));
        return mcpToolEntity;
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
