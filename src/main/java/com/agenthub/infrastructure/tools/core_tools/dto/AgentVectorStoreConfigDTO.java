package com.agenthub.infrastructure.tools.core_tools.dto;

import com.agenthub.domain.enums.VectorStoreType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent向量库配置DTO，仅暴露Agent决策所需的向量库信息，不含主机地址、API密钥等敏感配置。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentVectorStoreConfigDTO {
    private String id;
    private String name;
    private VectorStoreType type;
    private String collectionName;
    private Boolean enabled;
}
