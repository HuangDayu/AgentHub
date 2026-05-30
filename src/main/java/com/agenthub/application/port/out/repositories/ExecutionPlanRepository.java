package com.agenthub.application.port.out.repositories;

import com.agenthub.domain.model.plan.ExecutionPlan;

import java.util.List;
import java.util.Optional;

/**
 * 执行计划仓储接口，定义执行计划的持久化操作。
 */
public interface ExecutionPlanRepository {

    /**
     * 保存执行计划。
     *
     * @param plan 执行计划领域模型
     * @return 保存后的执行计划
     */
    ExecutionPlan save(ExecutionPlan plan);

    /**
     * 根据ID查找执行计划。
     *
     * @param id 执行计划ID
     * @return 可选执行计划
     */
    Optional<ExecutionPlan> findById(String id);

    /**
     * 查找会话的当前活跃计划。
     *
     * @param sessionId 会话ID
     * @return 可选活跃执行计划
     */
    Optional<ExecutionPlan> findActiveBySessionId(String sessionId);

    /**
     * 根据Agent ID查找所有执行计划。
     *
     * @param agentId Agent ID
     * @return 执行计划列表
     */
    List<ExecutionPlan> findByAgentId(String agentId);

    /**
     * 根据ID删除执行计划。
     *
     * @param id 执行计划ID
     */
    void deleteById(String id);
}
