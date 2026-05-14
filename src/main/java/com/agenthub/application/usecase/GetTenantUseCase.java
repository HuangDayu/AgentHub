package com.agenthub.application.usecase;

import com.agenthub.domain.exception.NotFoundException;
import com.agenthub.domain.model.Tenant;
import com.agenthub.application.port.out.repositories.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 获取租户用例.
 * <p>
 * 处理根据ID获取租户详情的业务逻辑。
 * </p>
 */
@Component
@RequiredArgsConstructor
public class GetTenantUseCase {
    private final TenantRepository tenantRepository;

    /**
     * 执行获取租户操作。
     *
     * @param tenantId 租户ID
     * @return 租户
     * @throws NotFoundException 当租户不存在时抛出
     */
    public Tenant execute(String tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new NotFoundException("Tenant not found: " + tenantId));
    }
}
