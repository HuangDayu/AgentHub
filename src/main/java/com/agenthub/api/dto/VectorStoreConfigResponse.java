package com.agenthub.api.dto;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.domain.model.VectorStoreConfig;
import com.agenthub.domain.model.VectorStoreType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 向量库配置响应 DTO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VectorStoreConfigResponse {
    private String id;
    private String name;
    private VectorStoreType type;
    private String host;
    private Integer port;
    private boolean hasApiKey;
    private String collectionName;
    private String extraParams;
    private Boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;
    // 注意: tenantId 不暴露到响应中

    /**
     * 从领域对象构建响应。
     */
    public static VectorStoreConfigResponse from(VectorStoreConfig config) {
        VectorStoreConfigResponse response = BeanUtil.copyProperties(config, VectorStoreConfigResponse.class);
        response.setHasApiKey(config.getApiKey() != null && !config.getApiKey().isEmpty());
        return response;
    }
}
