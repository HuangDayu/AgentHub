package com.agenthub.infrastructure.store.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.agenthub.infrastructure.store.db.entity.AgentTaskEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工作流任务数据映射器。
 */
@Mapper
public interface AgentTaskMybatisMapper extends BaseMapper<AgentTaskEntity> {
}
