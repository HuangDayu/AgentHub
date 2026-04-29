package com.agenthub.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.agenthub.infrastructure.persistence.entity.WorkspaceEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工作空间数据映射器.
 * <p>
 * 提供对app.workspace表的CRUD操作。
 * </p>
 */
@Mapper
public interface WorkspaceMapper extends BaseMapper<WorkspaceEntity> {
}
