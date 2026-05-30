package com.agenthub.infrastructure.tools.system_tools.core_tools.dto;

import com.agenthub.domain.model.auth.Tenant.IsolationLevel;
import com.agenthub.domain.model.auth.Tenant.TenantStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent租户DTO，仅暴露Agent决策所需的租户信息。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentTenantDTO {
    private String id;
    private String name;
    private String tenantCode;
    private String planCode;
    private IsolationLevel isolationLevel;
    private String region;
    private TenantStatus status;
}
