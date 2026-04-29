package com.agenthub.application.usecase;

import com.agenthub.application.port.out.IdGenerator;
import com.agenthub.application.port.out.TimeProvider;
import com.agenthub.common.exception.NotFoundException;
import com.agenthub.application.port.out.repositories.TenantRepository;
import com.agenthub.domain.model.Workspace;
import com.agenthub.application.port.out.repositories.WorkspaceRepository;
import org.springframework.stereotype.Component;

/**
 * 创建工作空间用例.
 * <p>
 * 处理在指定租户下创建新工作空间的业务逻辑。
 * 首先验证租户是否存在，然后创建工作空间并持久化存储。
 * </p>
 */
@Component
public class CreateWorkspaceUseCase {
    private final TenantRepository tenantRepository;
    private final WorkspaceRepository workspaceRepository;
    private final IdGenerator idGenerator;
    private final TimeProvider timeProvider;

    /**
     * 构造创建工作空间用例。
     *
     * @param tenantRepository    租户仓储
     * @param workspaceRepository 工作空间仓储
     * @param idGenerator         ID生成器
     * @param timeProvider        时间提供者
     */
    public CreateWorkspaceUseCase(
            TenantRepository tenantRepository,
            WorkspaceRepository workspaceRepository,
            IdGenerator idGenerator,
            TimeProvider timeProvider
    ) {
        this.tenantRepository = tenantRepository;
        this.workspaceRepository = workspaceRepository;
        this.idGenerator = idGenerator;
        this.timeProvider = timeProvider;
    }

    /**
     * 执行创建工作空间操作。
     *
     * @param workspaceCode 工作空间编码
     * @param name          工作空间名称
     * @param region        区域
     * @param tenantId      租户ID（必需）
     * @return 创建的工作空间
     * @throws NotFoundException 当租户不存在时抛出
     */
    public Workspace execute(String workspaceCode, String name, String region, String tenantId) {
        // 验证租户存在
        if (tenantRepository.findById(tenantId).isEmpty()) {
            throw new NotFoundException("Tenant not found: " + tenantId);
        }
        Workspace workspace = Workspace.createWithId(
                idGenerator.nextId(),
                tenantId,
                workspaceCode,
                name,
                region,
                timeProvider.now()
        );
        return workspaceRepository.save(workspace);
    }
}
