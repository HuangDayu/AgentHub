package com.agenthub.infrastructure.store.db.mapper;

import com.agenthub.infrastructure.store.db.entity.TraceEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * Trace Mapper.
 */
@Mapper
public interface TraceMybatisMapper extends BaseMapper<TraceEntity> {
}
