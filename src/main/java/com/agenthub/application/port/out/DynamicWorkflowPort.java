package com.agenthub.application.port.out;

import com.agenthub.domain.model.workflow.AgentTask;
import com.agenthub.domain.model.workflow.DynamicWorkflow;
import com.agenthub.domain.model.workflow.WorkflowStage;

import java.util.List;
import java.util.Optional;

/**
 * 动态工作流持久化端口。
 */
public interface DynamicWorkflowPort {

    /**
     * 保存工作流。
     *
     * @param workflow 工作流
     * @return 保存后的工作流
     */
    DynamicWorkflow save(DynamicWorkflow workflow);

    /**
     * 根据ID查找工作流。
     *
     * @param workflowId 工作流ID
     * @return 工作流
     */
    Optional<DynamicWorkflow> findById(String workflowId);

    /**
     * 根据会话ID查找活跃工作流。
     *
     * @param sessionId 会话ID
     * @return 活跃工作流
     */
    Optional<DynamicWorkflow> findActiveBySessionId(String sessionId);

    /**
     * 根据Agent ID查找所有工作流。
     *
     * @param agentId Agent ID
     * @return 工作流列表
     */
    List<DynamicWorkflow> findByAgentId(String agentId);

    /**
     * 根据会话ID查找所有工作流。
     *
     * @param sessionId 会话ID
     * @return 工作流列表
     */
    List<DynamicWorkflow> findBySessionId(String sessionId);

    /**
     * 保存阶段。
     *
     * @param stage 阶段
     * @return 保存后的阶段
     */
    WorkflowStage saveStage(WorkflowStage stage);

    /**
     * 根据工作流ID查找所有阶段。
     *
     * @param workflowId 工作流ID
     * @return 阶段列表
     */
    List<WorkflowStage> findStagesByWorkflowId(String workflowId);

    /**
     * 保存任务。
     *
     * @param task 任务
     * @return 保存后的任务
     */
    AgentTask saveTask(AgentTask task);

    /**
     * 根据阶段ID查找所有任务。
     *
     * @param stageId 阶段ID
     * @return 任务列表
     */
    List<AgentTask> findTasksByStageId(String stageId);

    /**
     * 根据工作流ID查找所有任务。
     *
     * @param workflowId 工作流ID
     * @return 任务列表
     */
    List<AgentTask> findTasksByWorkflowId(String workflowId);

    /**
     * 删除工作流及其关联数据。
     *
     * @param workflowId 工作流ID
     */
    void deleteById(String workflowId);
}
