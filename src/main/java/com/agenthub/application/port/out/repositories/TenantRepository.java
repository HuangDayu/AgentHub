package com.agenthub.application.port.out.repositories;

import com.agenthub.domain.model.auth.Tenant;

import java.util.List;
import java.util.Optional;

/**
 * 租户仓储端口.
 * <p>
 * 定义租户持久化的领域接口。
 * </p>
 */
public interface TenantRepository {

    /**
     * 保存租户。
     *
     * @param tenant 待保存的租户
     * @return 保存后的租户
     */
    Tenant save(Tenant tenant);

    /**
     * 根据租户ID查找租户。
     *
     * @param tenantId 租户ID
     * @return 包含租户的Optional，不存在时为空
     */
    Optional<Tenant> findById(String tenantId);

    /**
     * 分页查询租户列表。
     *
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 租户列表
     */
    List<Tenant> findAll(int page, int size);

    /**
     * 统计租户总数。
     *
     * @return 租户总数
     */
    long count();
}
