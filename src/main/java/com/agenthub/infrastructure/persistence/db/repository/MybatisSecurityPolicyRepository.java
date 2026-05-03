package com.agenthub.infrastructure.persistence.db.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.agenthub.application.port.out.repositories.SecurityPolicyRepository;
import com.agenthub.domain.model.SecurityPolicy;
import com.agenthub.infrastructure.persistence.db.entity.SecurityPolicyEntity;
import com.agenthub.infrastructure.persistence.db.mapper.SecurityPolicyMybatisMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Primary
public class MybatisSecurityPolicyRepository implements SecurityPolicyRepository {
    private final SecurityPolicyMybatisMapper mapper;

    public MybatisSecurityPolicyRepository(SecurityPolicyMybatisMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public SecurityPolicy save(SecurityPolicy policy) {
        SecurityPolicyEntity entity = toEntity(policy);
        mapper.insertOrUpdate(entity);
        return policy;
    }

    @Override
    public Optional<SecurityPolicy> findById(String policyId) {
        return Optional.ofNullable(mapper.selectById(policyId)).map(this::toDomain);
    }

    @Override
    public List<SecurityPolicy> findAll() {
        return mapper.selectList(new LambdaQueryWrapper<>()).stream().map(this::toDomain).toList();
    }

    @Override
    public List<SecurityPolicy> findByTenantIdAndWorkspaceId(String tenantId, String workspaceId) {
        LambdaQueryWrapper<SecurityPolicyEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SecurityPolicyEntity::getTenantId, tenantId)
               .eq(SecurityPolicyEntity::getWorkspaceId, workspaceId);
        return mapper.selectList(wrapper).stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(String policyId) {
        mapper.deleteById(policyId);
    }

    private SecurityPolicyEntity toEntity(SecurityPolicy policy) {
        SecurityPolicyEntity entity = new SecurityPolicyEntity();
        entity.setId(policy.getId());
        entity.setTenantId(policy.getTenantId());
        entity.setWorkspaceId(policy.getWorkspaceId());
        entity.setName(policy.getName());
        entity.setDescription(policy.getDescription());
        entity.setInputValidation(policy.isInputValidation());
        entity.setOutputFiltering(policy.isOutputFiltering());
        entity.setRateLimitEnabled(policy.isRateLimitEnabled());
        entity.setRateLimitPerMinute(policy.getRateLimitPerMinute());
        entity.setContentModeration(policy.isContentModeration());
        entity.setPiiDetection(policy.isPiiDetection());
        entity.setAllowedDomains(policy.getAllowedDomains());
        entity.setBlockedPatterns(policy.getBlockedPatterns());
        entity.setCreatedAt(policy.getCreatedAt());
        entity.setUpdatedAt(policy.getUpdatedAt());
        return entity;
    }

    private SecurityPolicy toDomain(SecurityPolicyEntity entity) {
        SecurityPolicy policy = new SecurityPolicy(entity.getId(), entity.getTenantId(), entity.getWorkspaceId());
        policy.setName(entity.getName());
        policy.setDescription(entity.getDescription());
        policy.setInputValidation(entity.isInputValidation());
        policy.setOutputFiltering(entity.isOutputFiltering());
        policy.setRateLimitEnabled(entity.isRateLimitEnabled());
        policy.setRateLimitPerMinute(entity.getRateLimitPerMinute());
        policy.setContentModeration(entity.isContentModeration());
        policy.setPiiDetection(entity.isPiiDetection());
        policy.setAllowedDomains(entity.getAllowedDomains());
        policy.setBlockedPatterns(entity.getBlockedPatterns());
        policy.setCreatedAt(entity.getCreatedAt());
        policy.setUpdatedAt(entity.getUpdatedAt());
        return policy;
    }
}
