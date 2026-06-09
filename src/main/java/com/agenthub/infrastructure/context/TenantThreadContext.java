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
    private String agentId;
    private String sessionId;
    private String requestId;
    private String userId;
    private boolean ignoreTenantContext;



    public static TenantThreadContext ignoreContext(){
        TenantThreadContext tenantThreadContext = new TenantThreadContext();
        tenantThreadContext.setIgnoreTenantContext(true);
        return tenantThreadContext;
    }
}
