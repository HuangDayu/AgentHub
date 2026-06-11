package com.agenthub.infrastructure.store.db.repository;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.application.port.out.DynamicWorkflowPort;
import com.agenthub.domain.model.workflow.AgentTask;
import com.agenthub.domain.model.workflow.DynamicWorkflow;
import com.agenthub.domain.model.workflow.WorkflowStage;
import com.agenthub.infrastructure.store.db.entity.AgentTaskEntity;
import com.agenthub.infrastructure.store.db.entity.DynamicWorkflowEntity;
import com.agenthub.infrastructure.store.db.entity.WorkflowStageEntity;
import com.agenthub.infrastructure.store.db.mapper.AgentTaskMybatisMapper;
import com.agenthub.infrastructure.store.db.mapper.DynamicWorkflowMybatisMapper;
import com.agenthub.infrastructure.store.db.mapper.WorkflowStageMybatisMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 基于MyBatis的动态工作流仓储实现。
 */
@Component
@Primary
public class MybatisDynamicWorkflowRepository implements DynamicWorkflowPort {

    private final DynamicWorkflowMybatisMapper workflowMapper;
    private final WorkflowStageMybatisMapper stageMapper;
    private final AgentTaskMybatisMapper taskMapper;

    public MybatisDynamicWorkflowRepository(DynamicWorkflowMybatisMapper workflowMapper,
                                            WorkflowStageMybatisMapper stageMapper,
                                            AgentTaskMybatisMapper taskMapper) {
        this.workflowMapper = workflowMapper;
        this.stageMapper = stageMapper;
        this.taskMapper = taskMapper;
    }

    @Override
    public DynamicWorkflow save(DynamicWorkflow workflow) {
        DynamicWorkflowEntity entity = toWorkflowEntity(workflow);
        saveOrUpdateWorkflow(entity);
        saveStages(workflow);
        return toDomain(entity, workflow.getStages());
    }

    @Override
    public Optional<DynamicWorkflow> findById(String workflowId) {
        DynamicWorkflowEntity entity = workflowMapper.selectById(workflowId);
        if (entity == null) return Optional.empty();
        List<WorkflowStage> stages = findStagesByWorkflowId(workflowId);
        return Optional.of(toDomain(entity, stages));
    }

    @Override
    public Optional<DynamicWorkflow> findActiveBySessionId(String sessionId) {
        LambdaQueryWrapper<DynamicWorkflowEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DynamicWorkflowEntity::getSessionId, sessionId).in(DynamicWorkflowEntity::getStatus, "PLANNING", "EXECUTING", "VERIFYING").orderByDesc(DynamicWorkflowEntity::getCreatedAt).last("LIMIT 1");
        DynamicWorkflowEntity entity = workflowMapper.selectOne(wrapper);
        if (entity == null) return Optional.empty();
        return Optional.of(toDomain(entity, findStagesByWorkflowId(entity.getId())));
    }

    @Override
    public List<DynamicWorkflow> findByAgentId(String agentId) {
        LambdaQueryWrapper<DynamicWorkflowEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DynamicWorkflowEntity::getAgentId, agentId)
                .orderByDesc(DynamicWorkflowEntity::getCreatedAt);
        return workflowMapper.selectList(wrapper).stream()
                .map(e -> toDomain(e, findStagesByWorkflowId(e.getId())))
                .toList();
    }

    @Override
    public List<DynamicWorkflow> findBySessionId(String sessionId) {
        LambdaQueryWrapper<DynamicWorkflowEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DynamicWorkflowEntity::getSessionId, sessionId)
                .orderByDesc(DynamicWorkflowEntity::getCreatedAt);
        return workflowMapper.selectList(wrapper).stream()
                .map(e -> toDomain(e, findStagesByWorkflowId(e.getId())))
                .toList();
    }

    @Override
    public WorkflowStage saveStage(WorkflowStage stage) {
        WorkflowStageEntity entity = toStageEntity(stage);
        saveOrUpdateStage(entity);
        saveTasks(stage);
        return toStageDomain(entity, stage.getTasks());
    }

    @Override
    public List<WorkflowStage> findStagesByWorkflowId(String workflowId) {
        if (workflowId == null) return List.of();
        LambdaQueryWrapper<WorkflowStageEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkflowStageEntity::getWorkflowId, workflowId)
                .orderByAsc(WorkflowStageEntity::getStageOrder);
        return stageMapper.selectList(wrapper).stream()
                .map(e -> toStageDomain(e, findTasksByStageId(e.getId())))
                .toList();
    }

    @Override
    public AgentTask saveTask(AgentTask task) {
        AgentTaskEntity entity = toTaskEntity(task);
        saveOrUpdateTask(entity);
        return toTaskDomain(entity);
    }

    @Override
    public List<AgentTask> findTasksByStageId(String stageId) {
        if (stageId == null) return List.of();
        LambdaQueryWrapper<AgentTaskEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgentTaskEntity::getStageId, stageId);
        return taskMapper.selectList(wrapper).stream()
                .map(this::toTaskDomain).toList();
    }

    @Override
    public List<AgentTask> findTasksByWorkflowId(String workflowId) {
        if (workflowId == null) return List.of();
        LambdaQueryWrapper<AgentTaskEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgentTaskEntity::getWorkflowId, workflowId);
        return taskMapper.selectList(wrapper).stream()
                .map(this::toTaskDomain).toList();
    }

    @Override
    public void deleteById(String workflowId) {
        deleteTasksByWorkflowId(workflowId);
        deleteStagesByWorkflowId(workflowId);
        workflowMapper.deleteById(workflowId);
    }

    private void saveStages(DynamicWorkflow workflow) {
        for (WorkflowStage stage : workflow.getStages()) {
            saveStage(stage);
        }
    }

    private void saveTasks(WorkflowStage stage) {
        for (AgentTask task : stage.getTasks()) {
            saveTask(task);
        }
    }

    private void saveOrUpdateWorkflow(DynamicWorkflowEntity entity) {
        if (entity.getId() != null && workflowMapper.selectById(entity.getId()) != null) {
            workflowMapper.updateById(entity);
        } else {
            workflowMapper.insert(entity);
        }
    }

    private void saveOrUpdateStage(WorkflowStageEntity entity) {
        if (entity.getId() != null && stageMapper.selectById(entity.getId()) != null) {
            stageMapper.updateById(entity);
        } else {
            stageMapper.insert(entity);
        }
    }

    private void saveOrUpdateTask(AgentTaskEntity entity) {
        if (entity.getId() != null && taskMapper.selectById(entity.getId()) != null) {
            taskMapper.updateById(entity);
        } else {
            taskMapper.insert(entity);
        }
    }

    private void deleteTasksByWorkflowId(String workflowId) {
        LambdaQueryWrapper<AgentTaskEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgentTaskEntity::getWorkflowId, workflowId);
        taskMapper.delete(wrapper);
    }

    private void deleteStagesByWorkflowId(String workflowId) {
        LambdaQueryWrapper<WorkflowStageEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkflowStageEntity::getWorkflowId, workflowId);
        stageMapper.delete(wrapper);
    }

    private DynamicWorkflowEntity toWorkflowEntity(DynamicWorkflow workflow) {
        DynamicWorkflowEntity entity = new DynamicWorkflowEntity();
        BeanUtil.copyProperties(workflow, entity);
        return entity;
    }

    private WorkflowStageEntity toStageEntity(WorkflowStage stage) {
        WorkflowStageEntity entity = new WorkflowStageEntity();
        BeanUtil.copyProperties(stage, entity);
        entity.setStageOrder(stage.getOrder());
        return entity;
    }

    private AgentTaskEntity toTaskEntity(AgentTask task) {
        AgentTaskEntity entity = new AgentTaskEntity();
        BeanUtil.copyProperties(task, entity);
        if (task.getToolNames() != null) {
            entity.setToolNames(String.join(",", task.getToolNames()));
        }
        return entity;
    }

    private DynamicWorkflow toDomain(DynamicWorkflowEntity entity, List<WorkflowStage> stages) {
        DynamicWorkflow workflow = new DynamicWorkflow();
        BeanUtil.copyProperties(entity, workflow);
        workflow.setStages(stages != null ? stages : List.of());
        return workflow;
    }

    private WorkflowStage toStageDomain(WorkflowStageEntity entity, List<AgentTask> tasks) {
        WorkflowStage stage = new WorkflowStage();
        BeanUtil.copyProperties(entity, stage);
        stage.setOrder(entity.getStageOrder());
        stage.setTasks(tasks != null ? tasks : List.of());
        return stage;
    }

    private AgentTask toTaskDomain(AgentTaskEntity entity) {
        AgentTask task = new AgentTask();
        BeanUtil.copyProperties(entity, task);
        if (entity.getToolNames() != null && !entity.getToolNames().isBlank()) {
            task.setToolNames(List.of(entity.getToolNames().split(",")));
        }
        return task;
    }
}
