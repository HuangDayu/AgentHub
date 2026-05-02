package com.agenthub.application.usecase;

import com.agenthub.common.exception.NotFoundException;
import com.agenthub.application.port.out.repositories.TenantRepository;
import com.agenthub.domain.model.Workspace;
import com.agenthub.application.port.out.repositories.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 列出工作空间用例.
 * <p>
 * 处理分页查询租户下工作空间列表的业务逻辑。
 * 首先验证租户是否存在，然后查询工作空间列表。
 * </p>
 */
@Component
@RequiredArgsConstructor
public class ListWorkspacesUseCase {
    private final TenantRepository tenantRepository;
    private final WorkspaceRepository workspaceRepository;

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
}
