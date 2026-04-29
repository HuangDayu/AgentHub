package com.agenthub.application.command;

import com.agenthub.domain.model.ModelSupplier;
import com.agenthub.domain.model.ModelType;

/**
 * 创建模型配置命令。
 */
public record CreateModelConfigCommand(
        String name,
        ModelType type,
        ModelSupplier supplier,
        String apiKey,
        String baseUrl,
        String model,
        Boolean enabled,
        String createdBy
) {
    public CreateModelConfigCommand {
        if (enabled == null) {
            enabled = true;
        }
    }
}