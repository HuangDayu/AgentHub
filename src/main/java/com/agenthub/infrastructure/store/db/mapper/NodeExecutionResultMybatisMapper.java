package com.agenthub.infrastructure.store.db.mapper;

import com.agenthub.infrastructure.store.db.entity.NodeExecutionResultEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 节点执行结果MyBatis Mapper.
 */
@Mapper
public interface NodeExecutionResultMybatisMapper extends BaseMapper<NodeExecutionResultEntity> {

    // BaseMapper已提供基础CRUD方法
}
