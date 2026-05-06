package com.agenthub.infrastructure.store.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.agenthub.infrastructure.store.db.entity.KnowledgeBaseEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识库MyBatis Mapper接口。
 * <p>
 * 继承BaseMapper提供基础的CRUD操作。
 * </p>
 */
@Mapper
public interface KnowledgeBaseMapper extends BaseMapper<KnowledgeBaseEntity> {
}

