package com.agenthub.api.mapper;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.api.dto.ModelConfigResponse;
import com.agenthub.domain.model.ModelConfig;
import com.agenthub.domain.model.ModelSupplier;
import com.agenthub.domain.model.ModelType;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 模型配置响应映射器。
 * <p>
 * 负责将领域对象转换为REST API响应DTO。
 * </p>
 */
@Component
public class ModelConfigResponseMapper {

    /**
     * 将模型配置领域对象转换为响应DTO。
     *
     * @param config 模型配置领域对象
     * @return 模型配置响应DTO
     */
    public ModelConfigResponse toResponse(ModelConfig config) {
        return BeanUtil.copyProperties(config, ModelConfigResponse.class);
    }

    /**
     * 将模型配置列表转换为响应DTO列表。
     *
     * @param configs 模型配置领域对象列表
     * @return 模型配置响应DTO列表
     */
    public List<ModelConfigResponse> toResponseList(List<ModelConfig> configs) {
        return configs.stream().map(this::toResponse).toList();
    }

    /**
     * 转换模型类型为名称。
     */
    private String toTypeName(ModelType type) {
        return type != null ? type.name() : null;
    }

    /**
     * 转换供应商为名称。
     */
    private String toSupplierName(ModelSupplier supplier) {
        return supplier != null ? supplier.name() : null;
    }

    /**
     * 脱敏处理 API Key，仅显示首尾字符。
     */
    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() <= 8) {
            return apiKey;
        }
        return apiKey.substring(0, 4) + "****" + apiKey.substring(apiKey.length() - 4);
    }
}
