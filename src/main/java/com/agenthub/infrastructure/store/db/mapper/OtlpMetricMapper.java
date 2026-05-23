package com.agenthub.infrastructure.store.db.mapper;

import com.agenthub.infrastructure.store.db.entity.OtlpMetricEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * OTLP Metric Mapper接口
 */
@Mapper
public interface OtlpMetricMapper extends BaseMapper<OtlpMetricEntity> {
}
