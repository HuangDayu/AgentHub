package com.agenthub.domain.model.datasource;

import com.agenthub.domain.enums.AgentDataSourceProtocol;
import com.agenthub.domain.enums.AgentDataSourceStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * AI Agent 数据源聚合根
 * <p>对外屏蔽 Camel 实现细节，是 Agent 调用外部系统的统一抽象。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentDataSource {
    private String id;
    private String tenantId;
    private String workspaceId;
    private String name;
    private String description;
    private AgentDataSourceProtocol protocol;
    private String endpointUri;
    private String propertiesJson;
    private boolean enabled;
    private AgentDataSourceStatus status;
    private String lastErrorMessage;
    private Instant lastCheckedAt;
    private String permissionPolicyId;
    private String schemaId;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
