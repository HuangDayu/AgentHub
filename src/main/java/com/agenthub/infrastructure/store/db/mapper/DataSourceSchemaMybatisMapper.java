package com.agenthub.infrastructure.store.db.mapper;

import com.agenthub.infrastructure.store.db.entity.DataSourceSchemaEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DataSourceSchemaMybatisMapper extends BaseMapper<DataSourceSchemaEntity> {
}
