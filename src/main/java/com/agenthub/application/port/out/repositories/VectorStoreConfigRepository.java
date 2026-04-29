package com.agenthub.application.port.out.repositories;

import com.agenthub.domain.model.VectorStoreConfig;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 向量库配置仓储接口（Hexagonal Architecture Outbound Port）。
 */
public interface VectorStoreConfigRepository {

    /** 根据 ID 查询 */
    Optional<VectorStoreConfig> findById(String id);

    /** 根据租户 ID 查询所有启用的配置 */
    List<VectorStoreConfig> findAllByTenantId(String tenantId);

    /** 根据租户 ID 和名称查询 */
    Optional<VectorStoreConfig> findByName(String name);

    /** 保存（新增或更新） */
    VectorStoreConfig save(VectorStoreConfig config);

    /** 根据 ID 删除 */
    void deleteById(String id);

    Collection<VectorStoreConfig> findAll();

}
