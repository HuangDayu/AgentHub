package com.agenthub.application.dto;

import com.agenthub.domain.model.AgentDataSource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Agent 数据源输出
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentDataSourceOutput {
    private String id;
    private String tenantId;
    private String workspaceId;
    private String name;
    private String description;
    private String protocol;
    private String endpointUri;
    private String propertiesJson;
    private boolean enabled;
    private String status;
    private String lastErrorMessage;
    private Instant lastCheckedAt;
    private String permissionPolicyId;
    private String schemaId;
    private Instant createdAt;
    private Instant updatedAt;

    public static AgentDataSourceOutput from(AgentDataSource s) {
        if (s == null) return null;
        AgentDataSourceOutput o = new AgentDataSourceOutput();
        cn.hutool.core.bean.BeanUtil.copyProperties(s, o);
        o.setProtocol(s.getProtocol() != null ? s.getProtocol().name() : null);
        o.setStatus(s.getStatus() != null ? s.getStatus().name() : null);
        return o;
    }
}
