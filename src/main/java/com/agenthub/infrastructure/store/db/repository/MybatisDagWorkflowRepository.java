package com.agenthub.infrastructure.store.db.repository;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.agenthub.application.port.out.repositories.DagWorkflowRepository;
import com.agenthub.domain.model.workflow.DagWorkflow;
import com.agenthub.infrastructure.store.db.entity.DagWorkflowEntity;
import com.agenthub.infrastructure.store.db.mapper.DagWorkflowMybatisMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Primary
public class MybatisDagWorkflowRepository implements DagWorkflowRepository {
    private final DagWorkflowMybatisMapper mapper;

    public MybatisDagWorkflowRepository(DagWorkflowMybatisMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public DagWorkflow save(DagWorkflow workflow) {
        DagWorkflowEntity entity = toEntity(workflow);
        mapper.insertOrUpdate(entity);
        return toDomain(entity);
    }

    @Override
    public Optional<DagWorkflow> findById(String workflowId) {
        return Optional.ofNullable(mapper.selectById(workflowId)).map(this::toDomain);
    }

    @Override
    public List<DagWorkflow> findAll() {
        return mapper.selectList(new LambdaQueryWrapper<>()).stream().map(this::toDomain).toList();
    }

    @Override
    public List<DagWorkflow> findByTenantIdAndWorkspaceId(String tenantId, String workspaceId) {
        LambdaQueryWrapper<DagWorkflowEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DagWorkflowEntity::getTenantId, tenantId)
               .eq(DagWorkflowEntity::getWorkspaceId, workspaceId);
        return mapper.selectList(wrapper).stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(String workflowId) {
        mapper.deleteById(workflowId);
    }

    private DagWorkflowEntity toEntity(DagWorkflow workflow) {
        return BeanUtil.copyProperties(workflow, DagWorkflowEntity.class);
    }

    private DagWorkflow toDomain(DagWorkflowEntity entity) {
        return BeanUtil.copyProperties(entity, DagWorkflow.class);
    }
}
