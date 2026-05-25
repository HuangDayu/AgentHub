package com.agenthub.infrastructure.store.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.agenthub.infrastructure.store.db.entity.WorkspaceEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工作空间数据映射器.
 * <p>
 * 提供对workspace表的CRUD操作。
 * </p>
 */
@Mapper
public interface WorkspaceMybatisMapper extends BaseMapper<WorkspaceEntity> {
}
