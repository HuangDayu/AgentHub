package com.agenthub.infrastructure.store.db.repository;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.application.port.out.repositories.ExecutionPlanRepository;
import com.agenthub.domain.model.plan.ExecutionPlan;
import com.agenthub.domain.model.plan.PlanStep;
import com.agenthub.infrastructure.store.db.entity.ExecutionPlanEntity;
import com.agenthub.infrastructure.store.db.entity.PlanStepEntity;
import com.agenthub.infrastructure.store.db.mapper.ExecutionPlanMybatisMapper;
import com.agenthub.infrastructure.store.db.mapper.PlanStepMybatisMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 基于MyBatis的执行计划仓储实现。
 */
@Component
@Primary
public class MybatisExecutionPlanRepository implements ExecutionPlanRepository {

    private final ExecutionPlanMybatisMapper planMapper;
    private final PlanStepMybatisMapper stepMapper;

    public MybatisExecutionPlanRepository(ExecutionPlanMybatisMapper planMapper,
                                          PlanStepMybatisMapper stepMapper) {
        this.planMapper = planMapper;
        this.stepMapper = stepMapper;
    }

    @Override
    public ExecutionPlan save(ExecutionPlan plan) {
        ExecutionPlanEntity planEntity = toPlanEntity(plan);
        saveOrUpdatePlan(planEntity);
        saveSteps(plan);
        return toDomain(planEntity, plan.getSteps());
    }

    @Override
    public Optional<ExecutionPlan> findById(String id) {
        ExecutionPlanEntity planEntity = planMapper.selectById(id);
        if (planEntity == null) return Optional.empty();
        List<PlanStep> steps = findStepsByPlanId(id);
        return Optional.of(toDomain(planEntity, steps));
    }

    @Override
    public Optional<ExecutionPlan> findActiveBySessionId(String sessionId) {
        LambdaQueryWrapper<ExecutionPlanEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExecutionPlanEntity::getSessionId, sessionId).in(ExecutionPlanEntity::getStatus, "PLANNING", "EXECUTING").orderByDesc(ExecutionPlanEntity::getCreatedAt).last("LIMIT 1");
        ExecutionPlanEntity planEntity = planMapper.selectOne(wrapper);
        if (planEntity == null) return Optional.empty();
        return Optional.of(toDomain(planEntity, findStepsByPlanId(planEntity.getId())));
    }

    @Override
    public List<ExecutionPlan> findByAgentId(String agentId) {
        LambdaQueryWrapper<ExecutionPlanEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExecutionPlanEntity::getAgentId, agentId)
                .orderByDesc(ExecutionPlanEntity::getCreatedAt);
        return planMapper.selectList(wrapper).stream()
                .map(e -> toDomain(e, findStepsByPlanId(e.getId())))
                .toList();
    }

    @Override
    public void deleteById(String id) {
        deleteStepsByPlanId(id);
        planMapper.deleteById(id);
    }

    private void saveSteps(ExecutionPlan plan) {
        for (PlanStep step : plan.getSteps()) {
            PlanStepEntity entity = toStepEntity(step);
            saveOrUpdateStep(entity);
        }
    }

    private void saveOrUpdatePlan(ExecutionPlanEntity entity) {
        if (entity.getId() != null && planMapper.selectById(entity.getId()) != null) {
            planMapper.updateById(entity);
        } else {
            planMapper.insert(entity);
        }
    }

    private void saveOrUpdateStep(PlanStepEntity entity) {
        stepMapper.insertOrUpdate(entity);
    }

    private List<PlanStep> findStepsByPlanId(String planId) {
        LambdaQueryWrapper<PlanStepEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlanStepEntity::getPlanId, planId)
                .orderByAsc(PlanStepEntity::getStepOrder);
        return stepMapper.selectList(wrapper).stream()
                .map(this::toStepDomain).toList();
    }

    private void deleteStepsByPlanId(String planId) {
        LambdaQueryWrapper<PlanStepEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlanStepEntity::getPlanId, planId);
        stepMapper.delete(wrapper);
    }

    private ExecutionPlanEntity toPlanEntity(ExecutionPlan plan) {
        ExecutionPlanEntity entity = new ExecutionPlanEntity();
        BeanUtil.copyProperties(plan, entity);
        return entity;
    }

    private PlanStepEntity toStepEntity(PlanStep step) {
        PlanStepEntity entity = new PlanStepEntity();
        BeanUtil.copyProperties(step, entity);
        entity.setStepOrder(step.getOrder());
        entity.setStepOutput(step.getOutput());
        return entity;
    }

    private ExecutionPlan toDomain(ExecutionPlanEntity entity, List<PlanStep> steps) {
        ExecutionPlan plan = new ExecutionPlan();
        BeanUtil.copyProperties(entity, plan);
        plan.setSteps(steps != null ? steps : List.of());
        return plan;
    }

    private PlanStep toStepDomain(PlanStepEntity entity) {
        PlanStep step = new PlanStep();
        BeanUtil.copyProperties(entity, step);
        step.setOrder(entity.getStepOrder());
        step.setOutput(entity.getStepOutput());
        return step;
    }
}
