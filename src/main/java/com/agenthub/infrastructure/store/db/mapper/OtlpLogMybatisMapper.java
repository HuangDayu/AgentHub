package com.agenthub.infrastructure.store.db.mapper;

import com.agenthub.infrastructure.store.db.entity.OtlpLogEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * OTLP Log Mapper接口
 */
@Mapper
public interface OtlpLogMybatisMapper extends BaseMapper<OtlpLogEntity> {
}
