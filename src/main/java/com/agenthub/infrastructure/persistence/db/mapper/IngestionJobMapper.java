package com.agenthub.infrastructure.persistence.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.agenthub.infrastructure.persistence.db.entity.IngestionJobEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 入库任务MyBatis Mapper接口。
 * <p>
 * 继承BaseMapper提供基础的CRUD操作。
 * </p>
 */
@Mapper
public interface IngestionJobMapper extends BaseMapper<IngestionJobEntity> {
}

