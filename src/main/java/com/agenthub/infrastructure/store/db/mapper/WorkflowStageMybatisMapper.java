package com.agenthub.infrastructure.store.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.agenthub.infrastructure.store.db.entity.WorkflowStageEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工作流阶段数据映射器。
 */
@Mapper
public interface WorkflowStageMybatisMapper extends BaseMapper<WorkflowStageEntity> {
}
