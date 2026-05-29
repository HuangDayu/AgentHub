package com.agenthub.application.port.out.repositories;

import com.agenthub.domain.model.agent.Subagent;

import java.util.List;
import java.util.Optional;

/**
 * 子智能体仓储接口，定义子Agent的持久化操作。
 */
public interface SubagentRepository {

    /**
     * 保存子Agent。
     *
     * @param subagent 子Agent领域模型
     * @return 保存后的子Agent
     */
    Subagent save(Subagent subagent);

    /**
     * 根据ID查找子Agent。
     *
     * @param id 子Agent ID
     * @return 可选子Agent
     */
    Optional<Subagent> findById(String id);

    /**
     * 根据父Agent ID查找所有子Agent。
     *
     * @param parentAgentId 父Agent ID
     * @return 子Agent列表
     */
    List<Subagent> findByParentAgentId(String parentAgentId);

    /**
     * 根据ID删除子Agent。
     *
     * @param id 子Agent ID
     */
    void deleteById(String id);

    /**
     * 根据租户ID和工作空间ID查找。
     *
     * @param tenantId    租户ID
     * @param workspaceId 工作空间ID
     * @return 子Agent列表
     */
    List<Subagent> findByTenantIdAndWorkspaceId(String tenantId, String workspaceId);
}
