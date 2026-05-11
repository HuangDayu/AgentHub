package com.agenthub.infrastructure.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantThreadContext {
    private String tenantId;
    private String workspaceId;
    private String requestId;
    private boolean ignoreTenantContext;
}
