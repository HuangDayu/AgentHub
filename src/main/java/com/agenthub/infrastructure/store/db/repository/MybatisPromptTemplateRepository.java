package com.agenthub.infrastructure.store.db.repository;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.application.port.out.repositories.PromptTemplateRepository;
import com.agenthub.domain.model.PromptTemplateInfo;
import com.agenthub.infrastructure.store.db.entity.PromptTemplateEntity;
import com.agenthub.infrastructure.store.db.mapper.PromptTemplateMybatisMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Optional;

import static org.springframework.ai.util.json.JsonParser.fromJson;
import static org.springframework.ai.util.json.JsonParser.toJson;

@Component
@Primary
public class MybatisPromptTemplateRepository implements PromptTemplateRepository {
    private final PromptTemplateMybatisMapper mapper;

    public MybatisPromptTemplateRepository(PromptTemplateMybatisMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public PromptTemplateInfo save(PromptTemplateInfo template) {
        PromptTemplateEntity entity = toEntity(template);
        mapper.insert(entity);
        return toDomain(entity);
    }

    @Override
    public Optional<PromptTemplateInfo> findById(String id) {
        return Optional.ofNullable(mapper.selectById(id)).map(this::toDomain);
    }

    @Override
    public List<PromptTemplateInfo> findByWorkspaceId(String workspaceId) {
        return mapper.selectByWorkspaceId(workspaceId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<PromptTemplateInfo> findByWorkspaceIdAndCategory(String workspaceId, String category) {
        return mapper.selectByWorkspaceIdAndCategory(workspaceId, category).stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(String id) {
        mapper.deleteById(id);
    }

    @Override
    public PromptTemplateInfo update(PromptTemplateInfo template) {
        PromptTemplateEntity entity = toEntity(template);
        mapper.updateById(entity);
        return toDomain(entity);
    }

    private PromptTemplateEntity toEntity(PromptTemplateInfo template) {
        PromptTemplateEntity entity = BeanUtil.copyProperties(template, PromptTemplateEntity.class);
        entity.setVariables(toJson(template.getVariables()));
        return entity;
    }

    private PromptTemplateInfo toDomain(PromptTemplateEntity entity) {
        PromptTemplateInfo promptTemplateInfo = BeanUtil.copyProperties(entity, PromptTemplateInfo.class);
        promptTemplateInfo.setVariables(fromJson(entity.getVariables(), new TypeReference<>() {
        }));
        return promptTemplateInfo;
    }
}
