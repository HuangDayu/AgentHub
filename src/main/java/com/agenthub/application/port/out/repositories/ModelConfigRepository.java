package com.agenthub.application.port.out.repositories;

import com.agenthub.domain.model.ModelConfig;
import com.agenthub.domain.model.ModelType;

import java.util.List;
import java.util.Optional;

/**
 * 模型配置仓储端口（六边形架构中的 Output Port）。
 */
public interface ModelConfigRepository {

    ModelConfig save(ModelConfig config);

    Optional<ModelConfig> findById(String id);

    Optional<ModelConfig> findByIdAndTenant(String id, String tenantId);

    List<ModelConfig> findByTenant(String tenantId);

    /**
     * 根据租户 ID 和模型类型查询配置（类型安全重载）。
     *
     * @param tenantId 租户 ID
     * @param type 模型类型
     * @return 模型配置列表
     */
    default List<ModelConfig> findByTenantAndType(String tenantId, ModelType type) {
        return findByTenantAndType(tenantId.toString(), type.name());
    }

    List<ModelConfig> findByTenantAndType(String tenantId, String type);

    List<ModelConfig> findByTenantAndEnabled(String tenantId, Boolean enabled);

    boolean deleteById(String id);

    boolean deleteByIdAndTenant(String id, String tenantId);

    List<ModelConfig> getByType(String type);

    List<ModelConfig> findAll();

    List<ModelConfig> findEnabledAll(Boolean enabled);

    List<ModelConfig> findByWorkspace(String workspaceId);
}
