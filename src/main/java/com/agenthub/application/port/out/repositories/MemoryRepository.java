package com.agenthub.application.port.out.repositories;

import com.agenthub.domain.model.Memory;

import java.util.List;
import java.util.Optional;

/**
 * 记忆仓储接口，定义记忆的持久化操作。
 */
public interface MemoryRepository {

    Memory save(Memory memory);

    Optional<Memory> findById(String memoryId);

    List<Memory> findByAgentId(String agentId);

    List<Memory> findByTenantIdAndWorkspaceId(String tenantId, String workspaceId);

    void deleteById(String memoryId);

    void deleteByAgentId(String agentId);
}
