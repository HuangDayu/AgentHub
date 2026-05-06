package com.agenthub.infrastructure.store.db.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.agenthub.application.port.out.repositories.WorkflowRepository;
import com.agenthub.domain.model.Workflow;
import com.agenthub.infrastructure.store.db.entity.WorkflowEntity;
import com.agenthub.infrastructure.store.db.mapper.WorkflowMybatisMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Primary
public class MybatisWorkflowRepository implements WorkflowRepository {
    private final WorkflowMybatisMapper mapper;

    public MybatisWorkflowRepository(WorkflowMybatisMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Workflow save(Workflow workflow) {
        WorkflowEntity entity = toEntity(workflow);
        mapper.insertOrUpdate(entity);
        return workflow;
    }

    @Override
    public Optional<Workflow> findById(String workflowId) {
        return Optional.ofNullable(mapper.selectById(workflowId)).map(this::toDomain);
    }

    @Override
    public List<Workflow> findAll() {
        return mapper.selectList(new LambdaQueryWrapper<>()).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Workflow> findByTenantIdAndWorkspaceId(String tenantId, String workspaceId) {
        LambdaQueryWrapper<WorkflowEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkflowEntity::getTenantId, tenantId)
               .eq(WorkflowEntity::getWorkspaceId, workspaceId);
        return mapper.selectList(wrapper).stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(String workflowId) {
        mapper.deleteById(workflowId);
    }

    private WorkflowEntity toEntity(Workflow workflow) {
        WorkflowEntity entity = new WorkflowEntity();
        entity.setId(workflow.getId());
        entity.setTenantId(workflow.getTenantId());
        entity.setWorkspaceId(workflow.getWorkspaceId());
        entity.setWorkflowCode(workflow.getWorkflowCode());
        entity.setName(workflow.getName());
        entity.setDescription(workflow.getDescription());
        entity.setGraphDefinition(workflow.getGraphDefinition());
        entity.setStatus(workflow.getStatus());
        entity.setCreatedAt(workflow.getCreatedAt());
        entity.setUpdatedAt(workflow.getUpdatedAt());
        return entity;
    }

    private Workflow toDomain(WorkflowEntity entity) {
        Workflow workflow = new Workflow();
        workflow.setId(entity.getId());
        workflow.setTenantId(entity.getTenantId());
        workflow.setWorkspaceId(entity.getWorkspaceId());
        workflow.setWorkflowCode(entity.getWorkflowCode());
        workflow.setName(entity.getName());
        workflow.setDescription(entity.getDescription());
        workflow.setGraphDefinition(entity.getGraphDefinition());
        workflow.setStatus(entity.getStatus());
        workflow.setCreatedAt(entity.getCreatedAt());
        workflow.setUpdatedAt(entity.getUpdatedAt());
        return workflow;
    }
}
