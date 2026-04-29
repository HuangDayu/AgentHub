package com.agenthub.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.agenthub.domain.model.Tool;
import com.agenthub.domain.model.ToolId;
import com.agenthub.application.port.out.repositories.ToolRepository;
import com.agenthub.infrastructure.persistence.entity.ToolEntity;
import com.agenthub.infrastructure.persistence.mapper.ToolMybatisMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 基于 MyBatis-Plus 的工具仓储适配器。
 * <p>
 * 遵循整洁架构：实现领域层 {@link ToolRepository} 端口接口。
 *
 * @since 1.0.0
 */
@Repository
@Primary
public class MybatisToolRepository implements ToolRepository {
    private final ToolMybatisMapper mapper;

    public MybatisToolRepository(ToolMybatisMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Tool save(Tool tool) {
        ToolEntity entity = toEntity(tool);
        if (mapper.selectById(entity.getId()) == null) {
            mapper.insert(entity);
        } else {
            mapper.updateById(entity);
        }
        return toDomain(entity);
    }

    @Override
    public Optional<Tool> findById(ToolId id) {
        ToolEntity entity = mapper.selectById(id.value());
        return Optional.ofNullable(entity).map(this::toDomain);
    }

    @Override
    public List<Tool> findAll() {
        LambdaQueryWrapper<ToolEntity> query = new LambdaQueryWrapper<>();
        query.orderByAsc(ToolEntity::getCreatedAt).orderByAsc(ToolEntity::getId);
        return mapper.selectList(query).stream().map(this::toDomain).toList();
    }

    private Tool toDomain(ToolEntity entity) {
        return new Tool(
                ToolId.of(entity.getId()),
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

    private ToolEntity toEntity(Tool tool) {
        ToolEntity entity = new ToolEntity();
        entity.setId(tool.id().value());
        entity.setName(tool.name());
        entity.setDescription(tool.description());
        entity.setEnabled(tool.enabled());
        entity.setEndpoint(tool.endpoint());
        entity.setInputSchema(tool.inputSchemaJson());
        entity.setTimeoutMs(tool.timeoutMs());
        entity.setCreatedAt(tool.createdAt());
        return entity;
    }
}
