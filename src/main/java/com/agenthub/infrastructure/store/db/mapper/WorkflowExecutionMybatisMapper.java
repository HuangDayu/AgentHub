package com.agenthub.infrastructure.store.db.mapper;

import com.agenthub.infrastructure.store.db.entity.WorkflowExecutionEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工作流执行记录MyBatis Mapper.
 */
@Mapper
public interface WorkflowExecutionMybatisMapper extends BaseMapper<WorkflowExecutionEntity> {

    // BaseMapper已提供基础CRUD方法
}
