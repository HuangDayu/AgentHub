package com.agenthub.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.agenthub.infrastructure.persistence.entity.DocumentChunkEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文档分块MyBatis Mapper接口。
 * <p>
 * 继承BaseMapper提供基础的CRUD操作。
 * </p>
 */
@Mapper
public interface DocumentChunkMapper extends BaseMapper<DocumentChunkEntity> {
}
