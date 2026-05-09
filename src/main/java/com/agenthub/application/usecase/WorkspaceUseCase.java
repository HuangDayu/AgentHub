package com.agenthub.application.usecase;

import com.agenthub.application.port.out.IdGenerator;
import com.agenthub.application.port.out.TimeProvider;
import com.agenthub.application.port.out.repositories.TenantRepository;
import com.agenthub.application.port.out.repositories.WorkspaceRepository;
import com.agenthub.common.exception.NotFoundException;
import com.agenthub.domain.model.Workspace;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 创建工作空间用例.
 * <p>
 * 处理在指定租户下创建新工作空间的业务逻辑。
 * 首先验证租户是否存在，然后创建工作空间并持久化存储。
 * </p>
 */
@Component
@RequiredArgsConstructor
public class WorkspaceUseCase {
    private final TenantRepository tenantRepository;
    private final WorkspaceRepository workspaceRepository;
    private final IdGenerator idGenerator;
    private final TimeProvider timeProvider;

    /**
     * 执行创建工作空间操作。
     *
     * @param workspaceCode 工作空间编码
     * @param name          工作空间名称
     * @param region        区域
     * @return 创建的工作空间
     * @throws NotFoundException 当租户不存在时抛出
     */
    public Workspace execute(String workspaceCode, String name, String region) {
        Workspace workspace = Workspace.create(workspaceCode, name, region);
        return workspaceRepository.save(workspace);
    }

    /**
     * 执行分页查询工作空间列表操作。
     *
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 工作空间列表
     * @throws NotFoundException 当租户不存在时抛出
     */
    public List<Workspace> execute(int page, int size) {
        return workspaceRepository.findByTenantId(page, size);
    }

    /**
     * 根据租户ID分页查找工作空间。
     */
    public List<Workspace> findWorkspacesByTenantId(String tenantId, int page, int size) {
        return workspaceRepository.findWorkspacesByTenantId(tenantId, page, size);
    }

    public void update(Workspace workspace) {
        workspaceRepository.update(workspace);

    }
}
