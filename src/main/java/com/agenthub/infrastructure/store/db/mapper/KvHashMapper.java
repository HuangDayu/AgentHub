package com.agenthub.infrastructure.store.db.mapper;

import com.agenthub.infrastructure.store.db.entity.KvHash;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 哈希存储Mapper
 */
@Mapper
public interface KvHashMapper extends BaseMapper<KvHash> {
}
