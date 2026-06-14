package com.agenthub.infrastructure.tools.core_tools.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模型能力摘要，供Agent进行模型选择决策。
 */
@Data
@NoArgsConstructor
public class ModelCapabilitySummary {
    private String modelConfigId;
    private String modelName;
    private String supplier;
    private String capabilityDomain;
    private String costLevel;
    private String speedLevel;
    private int maxTokens;
    private boolean available;
}
