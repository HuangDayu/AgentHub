package com.agenthub.infrastructure.store.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.agenthub.infrastructure.store.db.entity.DocumentChunkEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文档分块MyBatis Mapper接口。
 * <p>
 * 继承BaseMapper提供基础的CRUD操作。
 * </p>
 */
@Mapper
public interface DocumentChunkMybatisMapper extends BaseMapper<DocumentChunkEntity> {
}
