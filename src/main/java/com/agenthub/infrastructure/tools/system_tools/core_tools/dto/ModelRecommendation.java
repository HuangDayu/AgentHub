package com.agenthub.infrastructure.tools.system_tools.core_tools.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模型推荐结果，根据任务特征推荐最适合的模型。
 */
@Data
@NoArgsConstructor
public class ModelRecommendation {
    private String modelConfigId;
    private String modelName;
    private String supplier;
    private String reason;
    private String confidence;
}
