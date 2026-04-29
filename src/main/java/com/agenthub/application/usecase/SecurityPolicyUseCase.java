package com.agenthub.application.usecase;

import com.agenthub.common.exception.NotFoundException;
import com.agenthub.application.dto.SecurityPolicyOutput;
import com.agenthub.application.port.out.repositories.SecurityPolicyRepository;
import com.agenthub.domain.model.SecurityPolicy;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SecurityPolicyUseCase {
    private final SecurityPolicyRepository repository;

    public SecurityPolicyUseCase(SecurityPolicyRepository repository) {
        this.repository = repository;
    }

    public SecurityPolicyOutput create(String tenantId, String workspaceId,
                                       String name, String description) {
        SecurityPolicy policy = SecurityPolicy.create(tenantId, workspaceId, name, description);
        return toOutput(repository.save(policy));
    }

    public SecurityPolicyOutput get(String policyId) {
        return toOutput(findById(policyId));
    }

    public List<SecurityPolicyOutput> list() {
        return repository.findAll().stream().map(this::toOutput).toList();
    }

    public List<SecurityPolicyOutput> listByTenantAndWorkspace(String tenantId, String workspaceId) {
        return repository.findByTenantIdAndWorkspaceId(tenantId, workspaceId)
                .stream().map(this::toOutput).toList();
    }

    public SecurityPolicyOutput update(String policyId, String name, String description) {
        SecurityPolicy policy = findById(policyId);
        policy.update(name, description);
        return toOutput(repository.save(policy));
    }

    public SecurityPolicyOutput configure(String policyId, boolean inputValidation,
                                          boolean outputFiltering, boolean rateLimitEnabled,
                                          int rateLimitPerMinute, boolean contentModeration,
                                          boolean piiDetection) {
        SecurityPolicy policy = findById(policyId);
        policy.configureSecurity(inputValidation, outputFiltering, rateLimitEnabled,
                rateLimitPerMinute, contentModeration, piiDetection);
        return toOutput(repository.save(policy));
    }

    public void delete(String policyId) {
        findById(policyId);
        repository.deleteById(policyId);
    }

    private SecurityPolicy findById(String policyId) {
        return repository.findById(policyId)
                .orElseThrow(() -> new NotFoundException("SecurityPolicy not found: " + policyId));
    }

    private SecurityPolicyOutput toOutput(SecurityPolicy policy) {
        return new SecurityPolicyOutput(policy.getId(), policy.getTenantId(), policy.getWorkspaceId(),
                policy.getName(), policy.getDescription(), policy.isInputValidation(),
                policy.isOutputFiltering(), policy.isRateLimitEnabled(), policy.getRateLimitPerMinute(),
                policy.isContentModeration(), policy.isPiiDetection(),
                policy.getAllowedDomains(), policy.getBlockedPatterns(),
                policy.getCreatedAt(), policy.getUpdatedAt());
    }
}
