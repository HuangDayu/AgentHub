package com.agenthub.infrastructure.store.db.mapper;

import com.agenthub.infrastructure.store.db.entity.KvSet;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 集合存储Mapper
 */
@Mapper
public interface KvSetMapper extends BaseMapper<KvSet> {
}
