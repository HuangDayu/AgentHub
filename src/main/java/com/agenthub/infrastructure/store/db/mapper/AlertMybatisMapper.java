package com.agenthub.infrastructure.store.db.mapper;

import com.agenthub.infrastructure.store.db.entity.AlertEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * Alert Mapper.
 */
@Mapper
public interface AlertMybatisMapper extends BaseMapper<AlertEntity> {
}
