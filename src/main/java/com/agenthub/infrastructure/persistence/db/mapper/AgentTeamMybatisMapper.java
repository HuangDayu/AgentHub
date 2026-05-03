package com.agenthub.infrastructure.persistence.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.agenthub.infrastructure.persistence.db.entity.AgentTeamEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AgentTeamMybatisMapper extends BaseMapper<AgentTeamEntity> {
}
