package com.agenthub.infrastructure.store.db.repository;

import com.agenthub.application.port.out.repositories.UserInputRequestRepository;
import com.agenthub.domain.model.studio.UserInputPrompt;
import com.agenthub.infrastructure.store.db.entity.UserInputRequestEntity;
import com.agenthub.infrastructure.store.db.mapper.UserInputRequestMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 用户输入请求仓储实现.
 */
@Component
@RequiredArgsConstructor
public class MybatisUserInputRequestRepository implements UserInputRequestRepository {

    private final UserInputRequestMapper mapper;

    @Override
    public UserInputPrompt save(UserInputPrompt request) {
        UserInputRequestEntity entity = toEntity(request);
        mapper.insert(entity);
        return toDomain(entity);
    }

    @Override
    public Optional<UserInputPrompt> findByRequestId(String requestId) {
        LambdaQueryWrapper<UserInputRequestEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserInputRequestEntity::getRequestId, requestId);
        return Optional.ofNullable(mapper.selectOne(wrapper))
            .map(this::toDomain);
    }

    @Override
    public List<UserInputPrompt> findByRunId(String runId) {
        LambdaQueryWrapper<UserInputRequestEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserInputRequestEntity::getRunId, runId);
        return mapper.selectList(wrapper).stream()
            .map(this::toDomain)
            .toList();
    }

    @Override
    public List<UserInputPrompt> findAll() {
        return mapper.selectList(null).stream()
            .map(this::toDomain)
            .toList();
    }

    @Override
    public void deleteById(String id) {
        mapper.deleteById(id);
    }

    private UserInputRequestEntity toEntity(UserInputPrompt domain) {
        UserInputRequestEntity entity = new UserInputRequestEntity();
        entity.setId(domain.getRequestId());
        entity.setRequestId(domain.getRequestId());
        entity.setRunId(domain.getRunId());
        entity.setAgentId(domain.getAgentId());
        entity.setAgentName(domain.getAgentName());
        entity.setStructuredInput(domain.getStructuredInput());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }

    private UserInputPrompt toDomain(UserInputRequestEntity entity) {
        UserInputPrompt domain = new UserInputPrompt();
        domain.setRequestId(entity.getRequestId());
        domain.setRunId(entity.getRunId());
        domain.setAgentId(entity.getAgentId());
        domain.setAgentName(entity.getAgentName());
        domain.setStructuredInput(entity.getStructuredInput());
        domain.setCreatedAt(entity.getCreatedAt());
        return domain;
    }
}
