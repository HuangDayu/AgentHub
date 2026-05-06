package com.agenthub.infrastructure.store.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.agenthub.infrastructure.store.db.entity.IngestionDocumentEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 入库文档MyBatis Mapper接口。
 * <p>
 * 继承BaseMapper提供基础的CRUD操作。
 * </p>
 */
@Mapper
public interface IngestionDocumentMapper extends BaseMapper<IngestionDocumentEntity> {
}

