package com.agenthub.infrastructure.store.db.repository;

import com.agenthub.domain.model.Tenant;
import com.agenthub.application.port.out.repositories.TenantRepository;
import com.agenthub.infrastructure.store.db.entity.TenantEntity;
import com.agenthub.infrastructure.store.db.mapper.TenantMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * 基于MyBatis的租户仓储实现.
 * <p>
 * 使用MyBatis框架实现租户数据的持久化操作。
 * </p>
 */
@Component
@Primary
public class MybatisTenantRepository implements TenantRepository {
    private final TenantMapper mapper;

    /**
     * 构造基于MyBatis的租户仓储。
     *
     * @param mapper MyBatis租户映射器
     */
    public MybatisTenantRepository(TenantMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 保存租户。
     *
     * @param tenant 待保存的租户
     * @return 保存后的租户
     */
    @Override
    public Tenant save(Tenant tenant) {
        TenantEntity po = toPo(tenant);
        mapper.insertOrUpdate(po);
        return tenant;
    }

    /**
     * 根据ID查找租户。
     *
     * @param tenantId 租户ID
     * @return 包含租户的Optional，如果不存在则为空
     */
    @Override
    public Optional<Tenant> findById(String tenantId) {
        try {
            return Optional.ofNullable(mapper.selectById(tenantId))
                    .filter(this::isValidEntity)
                    .map(this::toDomain);
        } catch (Exception e) {
            // ID格式错误或查询异常时返回空
            return Optional.empty();
        }
    }

    /**
     * 分页查询租户列表。
     *
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 租户列表
     */
    @Override
    public List<Tenant> findAll(int page, int size) {
        return mapper.selectList(null).stream()
                .filter(this::isValidEntity)
                .map(this::toDomain)
                .sorted(Comparator.comparing(Tenant::createdAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .skip((long) page * size)
                .limit(size)
                .toList();
    }

    /**
     * 获取租户总数。
     *
     * @return 租户数量
     */
    @Override
    public long count() {
        return mapper.selectCount(null);
    }

    /**
     * 验证实体是否有效。
     */
    private boolean isValidEntity(TenantEntity po) {
        return po.getId() != null && !po.getId().isBlank()
                && po.getTenantCode() != null && !po.getTenantCode().isBlank()
                && po.getName() != null && !po.getName().isBlank()
                && po.getPlanCode() != null && !po.getPlanCode().isBlank()
                && po.getIsolationLevel() != null && !po.getIsolationLevel().isBlank()
                && po.getStatus() != null && !po.getStatus().isBlank()
                && po.getRegion() != null && !po.getRegion().isBlank();
    }

    /**
     * 将租户领域对象转换为持久化对象。
     *
     * @param tenant 租户领域对象
     * @return 租户持久化对象
     */
    private TenantEntity toPo(Tenant tenant) {
        TenantEntity po = new TenantEntity();
        po.setId(tenant.id());
        po.setTenantCode(tenant.tenantCode());
        po.setName(tenant.name());
        po.setPlanCode(tenant.planCode());
        po.setIsolationLevel(tenant.isolationLevel().name());
        po.setStatus(tenant.status().name());
        po.setRegion(tenant.region());
        po.setCreatedAt(tenant.createdAt());
        po.setUpdatedAt(tenant.updatedAt());
        return po;
    }

    /**
     * 将持久化对象转换为租户领域对象。
     *
     * @param po 租户持久化对象
     * @return 租户领域对象
     */
    private Tenant toDomain(TenantEntity po) {
        return Tenant.rehydrate(
                po.getId(),
                po.getTenantCode(),
                po.getName(),
                po.getPlanCode(),
                Tenant.IsolationLevel.valueOf(po.getIsolationLevel()),
                Tenant.TenantStatus.valueOf(po.getStatus()),
                po.getRegion(),
                po.getCreatedAt(),
                po.getUpdatedAt()
        );
    }
}
