package com.agenthub.application.command;

import com.agenthub.domain.model.ModelSupplier;
import com.agenthub.domain.model.ModelType;

/**
 * 更新模型配置命令。
 */
public record UpdateModelConfigCommand(
        String id,
        String name,
        ModelType type,
        ModelSupplier supplier,
        String apiKey,
        String baseUrl,
        String model,
        Boolean enabled
) {
}