package com.agenthub.infrastructure.store.db.mapper;

import com.agenthub.infrastructure.store.db.entity.KvZset;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 有序集合存储Mapper
 */
@Mapper
public interface KvZsetMapper extends BaseMapper<KvZset> {
}
