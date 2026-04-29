package com.agenthub.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.agenthub.infrastructure.persistence.entity.AgentConfigEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface AgentConfigMybatisMapper extends BaseMapper<AgentConfigEntity> {
    @Select("SELECT * FROM app.agent_config WHERE agent_id = #{agentId} ORDER BY priority ASC, created_at ASC")
    List<AgentConfigEntity> selectByAgentId(@Param("agentId") String agentId);

    @Select("SELECT * FROM app.agent_config WHERE agent_id = #{agentId} AND category = #{category} ORDER BY priority ASC")
    List<AgentConfigEntity> selectByAgentIdAndCategory(@Param("agentId") String agentId, @Param("category") String category);

    @Select("SELECT * FROM app.agent_config WHERE agent_id = #{agentId} AND type = #{type}")
    AgentConfigEntity selectByAgentIdAndType(@Param("agentId") String agentId, @Param("type") String type);
}
