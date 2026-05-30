package com.agenthub.infrastructure.store.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.agenthub.infrastructure.store.db.entity.ExecutionPlanEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 执行计划数据映射器。
 */
@Mapper
public interface ExecutionPlanMybatisMapper extends BaseMapper<ExecutionPlanEntity> {
}
