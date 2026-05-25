package com.agenthub.infrastructure.store.db.mapper;

import com.agenthub.infrastructure.store.db.entity.MetricEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * Metric Mapper.
 */
@Mapper
public interface MetricMybatisMapper extends BaseMapper<MetricEntity> {
}
