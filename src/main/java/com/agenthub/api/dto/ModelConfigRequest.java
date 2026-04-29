package com.agenthub.api.dto;

import com.agenthub.domain.model.ModelSupplier;
import com.agenthub.domain.model.ModelType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 创建/更新模型配置请求 DTO。
 */
public record ModelConfigRequest(
        @NotBlank String name,
        @NotNull ModelType type,
        @NotNull ModelSupplier supplier,
        @NotBlank String apiKey,
        String baseUrl,
        String model,
        Boolean enabled
) {
}