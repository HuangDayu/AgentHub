package com.agenthub.infrastructure.store.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.agenthub.infrastructure.store.db.entity.PlanStepEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 计划步骤数据映射器。
 */
@Mapper
public interface PlanStepMybatisMapper extends BaseMapper<PlanStepEntity> {
}
