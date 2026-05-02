package com.agenthub.application.usecase;

import com.agenthub.application.port.out.IdGenerator;
import com.agenthub.application.port.out.TimeProvider;
import com.agenthub.domain.model.Tenant;
import com.agenthub.application.port.out.repositories.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 创建租户用例.
 * <p>
 * 处理创建新租户的业务逻辑，包括生成租户ID和持久化存储。
 * </p>
 */
@Component
@RequiredArgsConstructor
public class CreateTenantUseCase {
    private final TenantRepository tenantRepository;
    private final IdGenerator idGenerator;
    private final TimeProvider timeProvider;

    /**
     * 执行创建租户操作。
     *
     * @param tenantCode     租户编码
     * @param name           租户名称
     * @param planCode       套餐编码
     * @param isolationLevel 隔离级别字符串
     * @param region         区域
     * @return 创建的租户
     */
    public Tenant execute(String tenantCode, String name, String planCode, String isolationLevel, String region) {
        // 解析隔离级别
        Tenant.IsolationLevel level = parseIsolationLevel(isolationLevel);
        // 使用ID生成器创建租户
        Tenant tenant = Tenant.createWithId(
                idGenerator.nextId(),
                tenantCode,
                name,
                planCode,
                level,
                region,
                timeProvider.now()
        );
        return tenantRepository.save(tenant);
    }

    /**
     * 解析隔离级别字符串为枚举值。
     *
     * @param isolationLevel 隔离级别字符串
     * @return 隔离级别枚举值
     */
    private Tenant.IsolationLevel parseIsolationLevel(String isolationLevel) {
        return isolationLevel != null
                ? Tenant.IsolationLevel.valueOf(isolationLevel)
                : Tenant.IsolationLevel.L1;
    }
}
