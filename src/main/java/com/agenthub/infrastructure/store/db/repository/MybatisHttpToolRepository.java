package com.agenthub.infrastructure.store.db.repository;

import com.agenthub.application.port.out.repositories.HttpToolRepository;
import com.agenthub.domain.model.HttpTool;
import com.agenthub.infrastructure.store.db.entity.HttpToolsEntity;
import com.agenthub.infrastructure.store.db.mapper.HttpToolMybatisMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 基于 MyBatis-Plus 的工具仓储适配器。
 * <p>
 * 遵循整洁架构：实现领域层 {@link HttpToolRepository} 端口接口。
 *
 * @since 1.0.0
 */
@Repository
@Primary
public class MybatisHttpToolRepository implements HttpToolRepository {
    private final HttpToolMybatisMapper mapper;

    public MybatisHttpToolRepository(HttpToolMybatisMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public HttpTool save(HttpTool httpTool) {
        HttpToolsEntity entity = toEntity(httpTool);
        if (mapper.selectById(entity.getId()) == null) {
            mapper.insert(entity);
        } else {
            mapper.updateById(entity);
        }
        return toDomain(entity);
    }

    @Override
    public Optional<HttpTool> findById(String id) {
        HttpToolsEntity entity = mapper.selectById(id);
        return Optional.ofNullable(entity).map(this::toDomain);
    }

    @Override
    public List<HttpTool> findAll() {
        LambdaQueryWrapper<HttpToolsEntity> query = new LambdaQueryWrapper<>();
        query.orderByAsc(HttpToolsEntity::getCreatedAt).orderByAsc(HttpToolsEntity::getId);
        return mapper.selectList(query).stream().map(this::toDomain).toList();
    }

    @Override
    public List<HttpTool> findByWorkspaceId(java.lang.String workspaceId) {
        LambdaQueryWrapper<HttpToolsEntity> query = new LambdaQueryWrapper<>();
        query.eq(HttpToolsEntity::getWorkspaceId, workspaceId);
        return mapper.selectList(query).stream().map(this::toDomain).toList();
    }

    @Override
    public List<HttpTool> findByIds(List<String> toolIds) {
        if (toolIds == null || toolIds.isEmpty()) {
            return List.of();
        }
        return mapper.selectByIds(toolIds).stream().map(this::toDomain).toList();
    }

    private HttpTool toDomain(HttpToolsEntity entity) {
        return new HttpTool(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                Boolean.TRUE.equals(entity.isEnabled()),
                entity.getEndpoint(),
                entity.getAuthType(),
                entity.getInputSchema(),
                entity.getTimeoutMs(),
                entity.getCreatedAt()
        );
    }

    private HttpToolsEntity toEntity(HttpTool httpTool) {
        HttpToolsEntity entity = new HttpToolsEntity();
        entity.setId(httpTool.id());
        entity.setName(httpTool.name());
        entity.setDescription(httpTool.description());
        entity.setEnabled(httpTool.enabled());
        entity.setEndpoint(httpTool.endpoint());
        entity.setInputSchema(httpTool.inputSchemaJson());
        entity.setTimeoutMs(httpTool.timeoutMs());
        entity.setCreatedAt(httpTool.createdAt());
        return entity;
    }
}
