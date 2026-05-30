package com.agenthub.infrastructure.tools.system_tools.core_tools.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模型切换结果 DTO。
 */
@Data
@NoArgsConstructor
public class ModelSwitchResult {
    private String modelConfigId;
    private String message;
}
