package com.agenthub.infrastructure.store.db.mapper;

import com.agenthub.infrastructure.store.db.entity.SpanEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * Span Mapper.
 */
@Mapper
public interface SpanMybatisMapper extends BaseMapper<SpanEntity> {
}
