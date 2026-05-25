package com.agenthub.infrastructure.store.db.mapper;

import com.agenthub.infrastructure.store.db.entity.OtlpSpanEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * OTLP Span Mapper接口
 */
@Mapper
public interface OtlpSpanMybatisMapper extends BaseMapper<OtlpSpanEntity> {
}
