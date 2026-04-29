package com.agenthub.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.agenthub.infrastructure.persistence.entity.McpToolEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface McpToolMybatisMapper extends BaseMapper<McpToolEntity> {
    
    @Select("SELECT * FROM app.mcp_tool WHERE workspace_id = #{workspaceId} ORDER BY created_at DESC")
    List<McpToolEntity> selectByWorkspaceId(@Param("workspaceId") String workspaceId);
}
