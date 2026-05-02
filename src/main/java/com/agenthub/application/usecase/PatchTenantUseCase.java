package com.agenthub.application.usecase;

import com.agenthub.application.command.PatchTenantCommand;
import com.agenthub.application.port.out.TimeProvider;
import com.agenthub.common.exception.NotFoundException;
import com.agenthub.domain.model.Tenant;
import com.agenthub.application.port.out.repositories.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 更新租户用例.
 * <p>
 * 处理部分更新租户信息的业务逻辑，支持更新租户名称等字段。
 * </p>
 */
@Component
@RequiredArgsConstructor
public class PatchTenantUseCase {
    private final TenantRepository tenantRepository;
    private final TimeProvider timeProvider;

    /**
     * 执行更新租户操作。
     *
     * @param tenantId 租户ID
     * @param command  更新命令
     * @return 更新后的租户
     * @throws NotFoundException 当租户不存在时抛出
     */
    public Tenant execute(String tenantId, PatchTenantCommand command) {
        // 查找租户，如果不存在则抛出异常
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new NotFoundException("Tenant not found: " + tenantId));

        // 根据命令更新租户信息
        Tenant updated = command.name() == null ? tenant : tenant.rename(command.name(), timeProvider.now());
        return tenantRepository.save(updated);
    }
}
