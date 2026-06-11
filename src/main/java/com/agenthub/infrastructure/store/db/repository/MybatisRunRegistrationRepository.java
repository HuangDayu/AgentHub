package com.agenthub.infrastructure.store.db.repository;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.application.port.out.repositories.RunRegistrationRepository;
import com.agenthub.domain.model.studio.RunRegistration;
import com.agenthub.infrastructure.store.db.entity.RunRegistrationEntity;
import com.agenthub.infrastructure.store.db.mapper.RunRegistrationMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.BadSqlGrammarException;
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
        try {
            return Optional.ofNullable(mapper.selectById(id)).map(this::toDomain);
        } catch (BadSqlGrammarException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<RunRegistration> findByProject(String project) {
        try {
            return findByProjectSafely(project);
        } catch (BadSqlGrammarException e) {
            return List.of();
        }
    }

    @Override
    public List<RunRegistration> findAll() {
        try {
            return mapper.selectList(null).stream().map(this::toDomain).toList();
        } catch (BadSqlGrammarException e) {
            return List.of();
        }
    }

    @Override
    public void deleteById(String id) {
        mapper.deleteById(id);
    }

    private RunRegistrationEntity toEntity(RunRegistration domain) {
        return BeanUtil.copyProperties(domain, RunRegistrationEntity.class);
    }

    private List<RunRegistration> findByProjectSafely(String project) {
        LambdaQueryWrapper<RunRegistrationEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RunRegistrationEntity::getProject, project);
        return mapper.selectList(wrapper).stream().map(this::toDomain).toList();
    }

    private RunRegistration toDomain(RunRegistrationEntity entity) {
        return BeanUtil.copyProperties(entity, RunRegistration.class);
    }
}
