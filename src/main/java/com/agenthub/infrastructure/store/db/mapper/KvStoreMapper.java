package com.agenthub.infrastructure.store.db.mapper;

import com.agenthub.infrastructure.store.db.entity.KvStore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 键值存储Mapper
 */
@Mapper
public interface KvStoreMapper extends BaseMapper<KvStore> {
}
