package com.agenthub.application.usecase;

import com.agenthub.domain.model.Tenant;
import com.agenthub.application.port.out.repositories.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 列出租户用例.
 * <p>
 * 处理分页查询租户列表的业务逻辑。
 * </p>
 */
@Component
@RequiredArgsConstructor
public class ListTenantsUseCase {
    private final TenantRepository tenantRepository;

    /**
     * 执行分页查询租户列表操作。
     *
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 租户列表
     */
    public List<Tenant> execute(int page, int size) {
        return tenantRepository.findAll(page, size);
    }
}
