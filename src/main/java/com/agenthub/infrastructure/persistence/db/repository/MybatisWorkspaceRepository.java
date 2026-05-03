package com.agenthub.infrastructure.persistence.db.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.agenthub.domain.model.Workspace;
import com.agenthub.application.port.out.repositories.WorkspaceRepository;
import com.agenthub.infrastructure.persistence.db.entity.WorkspaceEntity;
import com.agenthub.infrastructure.persistence.db.mapper.WorkspaceMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * 基于MyBatis的工作空间仓储实现.
 * <p>
 * 使用MyBatis框架实现工作空间数据的持久化操作。
 * </p>
 */
@Component
@Primary
public class MybatisWorkspaceRepository implements WorkspaceRepository {
    private final WorkspaceMapper mapper;

    /**
     * 构造基于MyBatis的工作空间仓储。
     *
     * @param mapper MyBatis工作空间映射器
     */
    public MybatisWorkspaceRepository(WorkspaceMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 保存工作空间。
     *
     * @param workspace 待保存的工作空间
     * @return 保存后的工作空间
     */
    @Override
    public Workspace save(Workspace workspace) {
        WorkspaceEntity po = toPo(workspace);
        mapper.insert(po);
        return workspace;
    }

    /**
     * 根据ID查找工作空间。
     *
     * @param workspaceId 工作空间ID
     * @return 包含工作空间的Optional，如果不存在则为空
     */
    @Override
    public Optional<Workspace> findById(String workspaceId) {
        try {
            return Optional.ofNullable(mapper.selectById(workspaceId))
                    .filter(this::isValidEntity)
                    .map(this::toDomain);
        } catch (Exception e) {
            // ID格式错误或查询异常时返回空
            return Optional.empty();
        }
    }

    /**
     * 根据租户ID分页查询工作空间列表。
     *
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 工作空间列表
     */
    @Override
    public List<Workspace> findByTenantId(int page, int size) {
        // 构建查询条件
        LambdaQueryWrapper<WorkspaceEntity> query = new LambdaQueryWrapper<WorkspaceEntity>();
        // 执行查询并分页
        return mapper.selectList(query).stream()
                .filter(this::isValidEntity)
                .map(this::toDomain)
                .filter(w -> w.createdAt() != null)
                .sorted(Comparator.comparing(Workspace::createdAt))
                .skip((long) page * size)
                .limit(size)
                .toList();
    }

    /**
     * 获取指定租户的工作空间总数。
     *
     * @param tenantId 租户ID
     * @return 工作空间数量
     */
    @Override
    public long countByTenantId(String tenantId) {
        // 构建查询条件
        LambdaQueryWrapper<WorkspaceEntity> query = new LambdaQueryWrapper<WorkspaceEntity>();
        try {
            query.eq(WorkspaceEntity::getTenantId, tenantId);
        } catch (Exception e) {
            // 租户ID格式错误时返回0
            return 0;
        }
        return mapper.selectCount(query);
    }

    /**
     * 根据租户ID分页查找工作空间。
     */
    @Override
    public List<Workspace> findWorkspacesByTenantId(String tenantId, int page, int size) {
        // 构建查询条件
        LambdaQueryWrapper<WorkspaceEntity> query = new LambdaQueryWrapper<WorkspaceEntity>();
        query.eq(WorkspaceEntity::getTenantId, tenantId);
        // 执行查询并分页
        return mapper.selectList(query).stream()
                .filter(this::isValidEntity)
                .map(this::toDomain)
                .filter(w -> w.createdAt() != null)
                .sorted(Comparator.comparing(Workspace::createdAt))
                .skip((long) page * size)
                .limit(size)
                .toList();
    }

    /**
     * 验证实体是否有效。
     */
    private boolean isValidEntity(WorkspaceEntity po) {
        return po.getId() != null && !po.getId().isBlank()
                && po.getTenantId() != null && !po.getTenantId().isBlank()
                && po.getWorkspaceCode() != null && !po.getWorkspaceCode().isBlank()
                && po.getName() != null && !po.getName().isBlank()
                && po.getRegion() != null && !po.getRegion().isBlank()
                && po.getStatus() != null
                && po.getCreatedAt() != null;
    }

    /**
     * 将工作空间领域对象转换为持久化对象。
     *
     * @param workspace 工作空间领域对象
     * @return 工作空间持久化对象
     */
    private WorkspaceEntity toPo(Workspace workspace) {
        WorkspaceEntity po = new WorkspaceEntity();
        po.setId(workspace.id());
        po.setTenantId(workspace.tenantId());
        po.setWorkspaceCode(workspace.workspaceCode());
        po.setName(workspace.name());
        po.setRegion(workspace.region());
        po.setStatus(workspace.status().name());
        po.setCreatedAt(workspace.createdAt());
        po.setUpdatedAt(workspace.updatedAt());
        return po;
    }

    /**
     * 将持久化对象转换为工作空间领域对象。
     *
     * @param po 工作空间持久化对象
     * @return 工作空间领域对象
     */
    private Workspace toDomain(WorkspaceEntity po) {
        return Workspace.rehydrate(
                po.getId(),
                po.getTenantId(),
                po.getWorkspaceCode(),
                po.getName(),
                po.getRegion(),
                Workspace.WorkspaceStatus.valueOf(po.getStatus()),
                po.getCreatedAt(),
                po.getUpdatedAt()
        );
    }
}
