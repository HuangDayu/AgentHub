package com.agenthub.infrastructure.store.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.agenthub.infrastructure.store.db.entity.DynamicWorkflowEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 动态工作流数据映射器。
 */
@Mapper
public interface DynamicWorkflowMybatisMapper extends BaseMapper<DynamicWorkflowEntity> {
}
