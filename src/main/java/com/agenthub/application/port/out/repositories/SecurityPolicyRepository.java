package com.agenthub.application.port.out.repositories;

import com.agenthub.domain.model.SecurityPolicy;

import java.util.List;
import java.util.Optional;

/**
 * 安全策略仓储接口，定义安全策略的持久化操作。
 */
public interface SecurityPolicyRepository {

    SecurityPolicy save(SecurityPolicy policy);

    Optional<SecurityPolicy> findById(String policyId);

    List<SecurityPolicy> findAll();

    List<SecurityPolicy> findByTenantIdAndWorkspaceId(String tenantId, String workspaceId);

    void deleteById(String policyId);
}
