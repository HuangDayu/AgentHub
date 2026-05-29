package com.agenthub.infrastructure.store.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.agenthub.infrastructure.store.db.entity.SubagentEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 子智能体数据映射器。
 */
@Mapper
public interface SubagentMybatisMapper extends BaseMapper<SubagentEntity> {
}
