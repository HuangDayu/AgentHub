package com.agenthub.api.controller;

import com.agenthub.api.dto.CreateSecurityPolicyRequest;
import com.agenthub.api.dto.SecurityPolicyResponse;
import com.agenthub.application.dto.SecurityPolicyOutput;
import com.agenthub.application.usecase.SecurityPolicyUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/security-policies")
public class SecurityPolicyController {
    private final SecurityPolicyUseCase useCase;

    public SecurityPolicyController(SecurityPolicyUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SecurityPolicyResponse create(@RequestBody CreateSecurityPolicyRequest request) {
        SecurityPolicyOutput result = useCase.create(request.tenantId(), request.workspaceId(),
                request.name(), request.description());
        return toResponse(result);
    }

    @GetMapping
    public List<SecurityPolicyResponse> list() {
        return useCase.list().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{policyId}")
    public SecurityPolicyResponse get(@PathVariable String policyId) {
        return toResponse(useCase.get(policyId));
    }

    @PutMapping("/{policyId}")
    public SecurityPolicyResponse update(@PathVariable String policyId,
                                         @RequestBody CreateSecurityPolicyRequest request) {
        SecurityPolicyOutput result = useCase.update(policyId, request.name(), request.description());
        return toResponse(result);
    }

    @DeleteMapping("/{policyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String policyId) {
        useCase.delete(policyId);
    }

    private SecurityPolicyResponse toResponse(SecurityPolicyOutput result) {
        return new SecurityPolicyResponse(result.id(), result.tenantId(), result.workspaceId(),
                result.name(), result.description(), result.inputValidation(),
                result.outputFiltering(), result.rateLimitEnabled(), result.rateLimitPerMinute(),
                result.contentModeration(), result.piiDetection(),
                result.allowedDomains(), result.blockedPatterns(),
                result.createdAt(), result.updatedAt());
    }
}
