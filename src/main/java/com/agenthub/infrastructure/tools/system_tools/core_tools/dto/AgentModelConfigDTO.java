package com.agenthub.infrastructure.tools.system_tools.core_tools.dto;

import com.agenthub.domain.enums.ModelSupplier;
import com.agenthub.domain.enums.ModelType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent模型配置DTO，仅暴露Agent决策所需的模型信息，不含API密钥等敏感数据。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentModelConfigDTO {
    private String id;
    private String name;
    private ModelType type;
    private ModelSupplier supplier;
    private String model;
    private Boolean enabled;
}
