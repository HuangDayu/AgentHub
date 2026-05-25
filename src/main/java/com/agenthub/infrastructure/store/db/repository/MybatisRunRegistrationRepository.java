package com.agenthub.infrastructure.store.db.repository;

import com.agenthub.application.port.out.repositories.RunRegistrationRepository;
import com.agenthub.domain.model.studio.RunRegistration;
import com.agenthub.infrastructure.store.db.entity.RunRegistrationEntity;
import com.agenthub.infrastructure.store.db.mapper.RunRegistrationMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Run注册仓储实现.
 */
@Component
@RequiredArgsConstructor
public class MybatisRunRegistrationRepository implements RunRegistrationRepository {

    private final RunRegistrationMapper mapper;

    @Override
    public RunRegistration save(RunRegistration registration) {
        RunRegistrationEntity entity = toEntity(registration);
        mapper.insert(entity);
        return toDomain(entity);
    }

    @Override
    public Optional<RunRegistration> findById(String id) {
        return Optional.ofNullable(mapper.selectById(id))
            .map(this::toDomain);
    }

    @Override
    public List<RunRegistration> findByProject(String project) {
        LambdaQueryWrapper<RunRegistrationEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RunRegistrationEntity::getProject, project);
        return mapper.selectList(wrapper).stream()
            .map(this::toDomain)
            .toList();
    }

    @Override
    public List<RunRegistration> findAll() {
        return mapper.selectList(null).stream()
            .map(this::toDomain)
            .toList();
    }

    @Override
    public void deleteById(String id) {
        mapper.deleteById(id);
    }

    private RunRegistrationEntity toEntity(RunRegistration domain) {
        RunRegistrationEntity entity = new RunRegistrationEntity();
        entity.setId(domain.getId());
        entity.setProject(domain.getProject());
        entity.setName(domain.getName());
        entity.setTimestamp(domain.getTimestamp());
        entity.setPid(domain.getPid());
        entity.setStatus(domain.getStatus());
        entity.setRunDir(domain.getRunDir());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }

    private RunRegistration toDomain(RunRegistrationEntity entity) {
        RunRegistration domain = new RunRegistration();
        domain.setId(entity.getId());
        domain.setProject(entity.getProject());
        domain.setName(entity.getName());
        domain.setTimestamp(entity.getTimestamp());
        domain.setPid(entity.getPid());
        domain.setStatus(entity.getStatus());
        domain.setRunDir(entity.getRunDir());
        domain.setCreatedAt(entity.getCreatedAt());
        return domain;
    }
}
