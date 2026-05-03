package com.agenthub.infrastructure.persistence.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.agenthub.infrastructure.persistence.db.entity.AgentEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 智能体数据映射器。
 */
@Mapper
public interface AgentMybatisMapper extends BaseMapper<AgentEntity> {
}

